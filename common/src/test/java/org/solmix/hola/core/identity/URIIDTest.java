/*
 * Copyright 2013 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.hola.core.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.common.identity.ID;
import org.solmix.hola.common.identity.IDCreateException;
import org.solmix.hola.common.identity.Namespace;
import org.solmix.hola.common.identity.support.URIID;
import org.solmix.hola.common.identity.support.URINamespace;
import org.solmix.hola.common.internal.DefaultIDFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月9日
 */

public class URIIDTest extends AbstractIDTestCase
{
    public static final String URI="http://www.solmix.org/hola";
    
    @Test
    public void testuri(){
        try {
            
            URI u1 = new URI("http://www.solmix.org/hola");
            assertEquals("http", u1.getScheme());
            assertEquals("/hola", u1.getPath());
            
            URI u2 = new URI("file:///home/solmix/o/pom.xml");
            assertEquals("file", u2.getScheme());
            assertEquals("/home/solmix/o/pom.xml", u2.getPath());
            
            URI u3 = new URI("/context/path?version=1.0.0&application=morgan");
            assertEquals(null, u3.getScheme());
            assertEquals(null, u3.getHost());
            assertEquals(null, u3.getUserInfo());
            assertEquals(null, u3.getScheme());
            assertEquals(-1, u3.getPort());
            
            
            URI u4 = new URI("context/path?version=1.0.0&application=morgan");
            assertEquals(null, u4.getHost());
            
            URI u5 = new URI("http://127.0.0.1");
            assertEquals("127.0.0.1", u5.getHost());
            
            URI u6 = new URI("admin://hello1234@127.0.0.1:1314/context/path?version=1.0.0");
            assertEquals("127.0.0.1", u6.getHost());
            assertEquals("hello1234", u6.getUserInfo());
            
            URI u7 = new URI("admin://aa:hello1234@127.0.0.1:1314/context/path?version=1.0.0");
            assertEquals("127.0.0.1", u7.getHost());
            assertEquals("aa:hello1234", u7.getUserInfo());
            
//            URI u8 = new URI("://127.0.0.1:1314/context/path?version=1.0.0");
//            assertEquals("127.0.0.1", u8.getHost());
            
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testCreate() {
        ID id= createID();
        Assert.assertNotNull(id);
    }
    @Test
    public void testCreatefixed() {
        ID id= createID("discovery:org.solmix.hola.discovery");
        Assert.assertNotNull(id);
    }

    @Test
    public void testGetName() {
        ID id= createID();
        Assert.assertNotNull(id.getName());
    }

    @Test
    public void testToQueryString() {
        ID id= createID();
        Assert.assertNotNull(id.toQueryString());
    }

    @Test
    public void testToString() {
        ID id= createID();
        Assert.assertNotNull(id.toString());
    }
    @Test
    public void testHashCode() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        assertTrue(id1.hashCode() == id2.hashCode());
  }
    @Test
    public void testCompareToNotEqual() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        assertTrue(id1.compareTo(id2) == 0);
        assertTrue(id2.compareTo(id1) == 0);
  }
    @Test
  public void testGetNamespace() throws Exception {
        final ID id = createID();
        final Namespace ns = id.getNamespace();
        assertNotNull(ns);
  }
    @Test
  public void testEqualNamespaces() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        final Namespace ns1 = id1.getNamespace();
        final Namespace ns2 = id2.getNamespace();
        assertTrue(ns1.equals(ns2));
        assertTrue(ns2.equals(ns2));
  }
    @Test
    public void testSerializable() throws Exception {
          final ByteArrayOutputStream buf = new ByteArrayOutputStream();
          final ObjectOutputStream out = new ObjectOutputStream(buf);
          try {
                out.writeObject(createID());
          } catch (final NotSerializableException ex) {
                fail(ex.getLocalizedMessage());
          } finally {
                out.close();
          }
    }
    @Test
    public void testCreateFromQueryString() throws Exception {
          final ID id1 = createID();
          final String externalForm = id1.toQueryString();
          final ID id2 = DefaultIDFactory.getDefault().createID(id1.getNamespace(),
                      externalForm);
          assertTrue(id1.equals(id2));
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.AbstractIDTestCase#createID()
     */
    @Override
    protected ID createID() throws IDCreateException {
        return createID(URI);
    }
    protected ID createID(String url) throws IDCreateException {
        
        URINamespace ns= DefaultIDFactory.getDefault().getNamespaceByName(URIID.class.getName()).adaptTo(URINamespace.class);
       return ns.createID(new String[]{url});
    }

}