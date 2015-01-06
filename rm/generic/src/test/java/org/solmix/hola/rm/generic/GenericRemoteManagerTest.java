/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.rm.generic;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.common.config.RemoteServiceConfig;
import org.solmix.hola.common.config.ServerConfig;
import org.solmix.hola.rm.RemoteException;
import org.solmix.hola.rm.RemoteReference;
import org.solmix.hola.rm.RemoteRegistration;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月5日
 */

public class GenericRemoteManagerTest extends Assert {

    private Container container;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        container= ContainerFactory.getDefaultContainer(true);
    }

    @Test
    public void testLocalReference() {
        GenericRemoteManager grm = new GenericRemoteManager();
        RemoteServiceConfig rsc = new RemoteServiceConfig();
        ServerConfig sc = new ServerConfig();
        sc.setTransporter("local");
        rsc.setServer(sc);
        rsc.setAddress("hola://localhost:12312");
        HelloServiceImpl hsimpl= new HelloServiceImpl();
        RemoteRegistration<HelloService> registration=null;
        try {
            registration =  grm.registerService(HelloService.class, hsimpl, rsc);
            HelloService  hs=  grm.getService(registration.getReference());
            assertNotNull(hs);
            assertEquals(hsimpl.sayHelloTo("solmix"), hs.sayHelloTo("solmix"));
//            ServiceReference<HelloService> refer = grm.getServiceReference(HelloService.class);
//            HelloService hs = grm.getService(refer);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (registration != null) {
                registration.unregister();
            }
        }
    }
    
    @Test
    public void testRemoteReference() {
        GenericRemoteManager grm = new GenericRemoteManager();
        RemoteServiceConfig rsc = new RemoteServiceConfig();
        ServerConfig sc = new ServerConfig();
        sc.setTransporter("local");
        rsc.setServer(sc);
        rsc.setAddress("hola://localhost:12312");
        HelloServiceImpl hsimpl= new HelloServiceImpl();
        RemoteRegistration<HelloService> registration=null;
        try {
            registration =  grm.registerService(HelloService.class, hsimpl, rsc);
            RemoteReference<HelloService> refer = grm.getReference(HelloService.class);
            HelloService hs = grm.getService(refer);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (registration != null) {
                registration.unregister();
            }
        }
    }
    
    @After
    public void shutdownBus() {       
        if (container != null) {
            container.close();
            container = null;
        } 
        ContainerFactory.setDefaultContainer(null);
    }

}
