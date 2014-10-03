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
package org.solmix.hola.discovery.jmdns;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.identity.support.DefaultIDFactory;
import org.solmix.hola.discovery.ServiceProperties;
import org.solmix.hola.discovery.identity.DefaultServiceTypeFactory;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.jmdns.identity.JmDNSNamespace;
import org.solmix.hola.discovery.support.ServiceInfoImpl;
import org.solmix.hola.discovery.support.ServicePropertiesImpl;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月9日
 */

public class ServiceMetadataTest 
{
    private ServiceInfoImpl meta;
    
    protected String serviceName="test-service";

    protected ServiceType type;

    protected int priority=41;

    protected int weight=42;

    protected ServiceProperties properties;

    protected long timeToLive=2000;
    
    protected URI uri;
    protected String[] services=new String[]{"hola","junit","test"};
    protected String[] protocols=new String[]{"someproto"};
    @Before
    public void setup(){
        
        uri=URI.create("test://hola:hola@192.168.1.1:9099/service?a=b&c=d#fragment");
        properties=new ServicePropertiesImpl();
        properties.setProperty("testKey", "value");
        DefaultIDFactory.getDefault().addNamespace(new JmDNSNamespace("jmdns"));
        Namespace ns= DefaultIDFactory.getDefault().getNamespaceByName(JmDNSNamespace.NAME);
        type=DefaultServiceTypeFactory.getDefault().create(ns,services,protocols);
        meta=new ServiceInfoImpl(uri,serviceName,type,priority,weight,properties,timeToLive);
        
    }

    /**
     * Test method for {@link org.solmix.hola.discovery.support.ServiceInfoImpl#getPriority()}.
     */
    @Test
    public void testGetPriority() {
       Assert.assertTrue(priority==meta.getPriority());
    }

    /**
     * Test method for {@link org.solmix.hola.discovery.support.ServiceInfoImpl#getWeight()}.
     */
    @Test
    public void testGetWeight() {
        Assert.assertTrue(weight==meta.getWeight());
    }

    /**
     * Test method for {@link org.solmix.hola.discovery.support.ServiceInfoImpl#getTTL()}.
     */
    @Test
    public void testGetTTL() {
        Assert.assertTrue(timeToLive==meta.getTTL());
    }

    /**
     * Test method for {@link org.solmix.hola.discovery.support.ServiceInfoImpl#getServiceProperties()}.
     */
    @Test
    public void testGetServiceProperties() {
        Assert.assertSame(properties, meta.getServiceProperties());
    }

    /**
     * Test method for {@link org.solmix.hola.discovery.support.ServiceInfoImpl#getServiceName()}.
     */
    @Test
    public void testGetServiceName() {
        Assert.assertNotNull(meta.getServiceName());
    }
    @Test
    public void testTypeEquals(){
        Assert.assertEquals(meta.getServiceID().getServiceType(), type);
    }

}
