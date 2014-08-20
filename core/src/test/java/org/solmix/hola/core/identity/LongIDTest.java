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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月9日
 */

public class LongIDTest extends AbstractIDTestCase
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.AbstractIDTestCase#createID()
     */
    @Override
    protected ID createID() throws IDCreateException {
        return createID(Long.MAX_VALUE);
    }

    /**
     * @param maxValue
     * @return
     */
    private ID createID(long value) {
        return IDFactory.getDefault().createLongID(value);
    }
    
    @Test
    public void testCreate(){
        ID id=createID();
        Assert.assertNotNull(id);
    }
    @Test
    public void testMinCreate(){
        ID id=createID(Long.MIN_VALUE);
        Assert.assertNotNull(id);
    }
    @Test
    public void testMaxCreate(){
        ID id=createID(Long.MAX_VALUE);
        Assert.assertNotNull(id);
    }
    @Test
    public void testGetName() throws Exception {
        final ID id1 = createID();
        final ID id2 = createID();
        Assert.assertTrue(id1.getName().equals(id2.getName()));
  }
    @Test
    public void testToQueryString(){
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
          final String queryString = id1.toQueryString();
          final ID id2 = IDFactory.getDefault().createID(id1.getNamespace(),
              queryString);
          assertTrue(id1.equals(id2));
    }
}
