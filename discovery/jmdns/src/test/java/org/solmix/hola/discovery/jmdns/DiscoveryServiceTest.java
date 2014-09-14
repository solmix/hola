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

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.core.ConnectContext;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.identity.support.DefaultIDFactory;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.ServiceProperties;
import org.solmix.hola.discovery.identity.DefaultServiceTypeFactory;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.jmdns.identity.JmDNSNamespace;
import org.solmix.hola.discovery.support.ServiceMetadataImpl;
import org.solmix.hola.discovery.support.ServicePropertiesImpl;
import org.solmix.runtime.SystemContext;
import org.solmix.runtime.SystemContextFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月10日
 */

public class DiscoveryServiceTest
{

    private JmDNSProvider service;

    @Before
    public void setup() {
        SystemContext sc = SystemContextFactory.getThreadDefaultSystemContext();
        service = sc.getExtension(Discovery.class).adaptTo(JmDNSProvider.class);
        try {
            service.connect(null, null);
        } catch (ConnectException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
        ConnectContext cc = service.adaptTo(ConnectContext.class);
        cc.disconnect();
    }

//    @Test
    public void testRegisterService() {
        registerService();
        ServiceMetadata[]  services=  service.getServices();
        Assert.assertTrue(services.length>=1);
    }

    public void registerService() {
        try {
            service.registerService(createServiceMetadata());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUnRegisterService() {

        System.out.println("ssdfS");
    }

    private ServiceMetadata createServiceMetadata() {
        if (meta == null) {
            String[] services = new String[] { "hola", "junit", "test" };
            String[] protocols = new String[] { "someproto" };
            URI uri = URI.create("test://hola:hola@192.168.1.1:9099/service?a=b&c=d#fragment");
            ServiceProperties properties = new ServicePropertiesImpl();
            properties.setProperty("testKey", "value");
            properties.setPropertyString("identifiale",
                Long.toString((new Random()).nextLong()));
            String serviceName = "jmdns-test";
            int priority = 100;
            int weight = 80;
            int timeToLive = 5000;
            DefaultIDFactory.getDefault().addNamespace(
                new JmDNSNamespace("jmdns"));
            Namespace ns = DefaultIDFactory.getDefault().getNamespaceByName(
                JmDNSNamespace.NAME);
            ServiceType type = DefaultServiceTypeFactory.getDefault().create(
                ns, services, protocols);
            meta = new ServiceMetadataImpl(uri, serviceName, type, priority,
                weight, properties, timeToLive);
        }
        return meta;

    }

    private ServiceMetadata meta;
}
