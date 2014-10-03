/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.hola.rt;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.rt.config.ApplicationConfig;
import org.solmix.hola.rt.config.DiscoveryConfig;
import org.solmix.hola.rt.config.ModuleConfig;
import org.solmix.hola.rt.config.ServerConfig;
import org.solmix.hola.rt.config.ServiceConfig;
import org.solmix.hola.rt.service.DemoService;
import org.solmix.hola.rt.service.DemoServiceImpl;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月28日
 */

public class ServiceConfigTest extends Assert
{
    private static Container container;
    ServiceConfig<DemoService> service;

    @BeforeClass
    public static void setup(){
        container=Containers.get();
      
    }
    @Before
    public void init(){
        service = new ServiceConfig<DemoService>(container);
    }
    //必选配置interface
    @Test(expected=IllegalStateException.class)
    public void testInterface() throws Exception{
        try {
             service.getEndpointInfo();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("<hola:service interface"));
            throw e;
        }
        fail("must throw validation exception!");
    }
    //interface必需是接口类型
    @Test(expected=IllegalStateException.class)
    public void testInterfaceType() throws Exception {
        service.setInterface(DemoServiceImpl.class);
        service.getEndpointInfo();
        fail("must throw validation exception!");
    }
    //必选配置ref
    @Test(expected=IllegalStateException.class)
    public void testRef() throws Exception{
        try {
            service.setInterface(DemoService.class);
             service.getEndpointInfo();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("<hola:service ref="));
            throw e;
        }
        fail("must throw validation exception!");
    }
    
    @Test
    public void testEndpointInfo()  {
        service.setInterface(DemoService.class);
        service.setRef(new DemoServiceImpl());
        service.setAsync(true);
       List<EndpointInfo> infos= service.getEndpointInfo();
       EndpointInfo info=infos.get(0);
       assertNull(info.getProperty("id"));
       assertEquals(DemoService.class.getName(), info.getString("interface"));
       assertEquals(DemoService.class.getName(), info.getString("path"));
       assertEquals(Boolean.TRUE, info.getBoolean("async"));
    }
    @Test
    public void testMerge()  {
        service.setInterface(DemoService.class);
        service.setRef(new DemoServiceImpl());
        
        ApplicationConfig app= service.createApplication();
        app.setName("app-1");
        app.setEnvironment("develop");
        service.setApplication(app);
       
        List<EndpointInfo> infos= service.getEndpointInfo();
        EndpointInfo info=infos.get(0);
        assertEquals("app-1", info.getString("application.name"));
        assertEquals("develop", info.getString("environment"));
        
        ModuleConfig module= service.createModule();
        module.setName("module-1");
        module.setEnvironment("test");
        service.setModule(module);
        
        EndpointInfo info2=service.getEndpointInfo().get(0);
        assertEquals("module-1", info2.getString("module.name"));
        assertEquals("test", info2.getString("environment"));
        
        DiscoveryConfig discovery= service.createDiscovery();
        discovery.setProtocol("zk");
        discovery.setAdvertise(true);
        discovery.setAddress("localhost:2181");
        service.setDiscovery(discovery);
        
        EndpointInfo info3=service.getEndpointInfo().get(0);
        assertEquals("zk", info3.getString("discovery.protocol"));
        assertEquals("hola", info3.getString("protocol"));
        assertEquals(Boolean.TRUE, info3.getBoolean("advertise"));
        
        ServerConfig server= service.createServer();
        server.setProtocol("hola");
        server.setAdvertise(true);
        server.setPort(1314);
        service.setServer(server);
        
        DiscoveryConfig discovery2= service.createDiscovery();
        discovery2.setProtocol("jmdns");
        discovery2.setAdvertise(true);
        List<DiscoveryConfig> dises= new ArrayList<DiscoveryConfig>();
        dises.add(discovery2);
        dises.add(discovery);
        service.setDiscoveries(dises);
        List<EndpointInfo> dds=service.getEndpointInfo();
        assertEquals(2, dds.size());
    }

}
