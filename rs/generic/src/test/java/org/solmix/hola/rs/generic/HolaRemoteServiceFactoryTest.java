/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.rs.generic;

import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.common.Constants;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月17日
 */

public class HolaRemoteServiceFactoryTest extends Assert
{

    public static final int PORT =  3333/*NetUtils.getAvailablePort()*/;
    @Test
    public void test() throws RemoteException {
        RemoteServiceManager rm = container.getExtension(RemoteServiceManager.class);
        assertNotNull(rm);
        RemoteServiceFactory rsf = rm.getRemoteServiceFactory("hola");
        assertNotNull(rsf);
        HelloServiceImpl hs = new HelloServiceImpl();
        RemoteRegistration<HelloService> reg=  rsf.register(HelloService.class, hs, mockConfig());
        HelloService hello=rsf.getService(reg.getReference());
        assertSame(hs, hello);
        Dictionary<String, Object> properties=mockConfig();
        properties.put(Constants.HOST_KEY, Constants.LOCALHOST_VALUE);
        RemoteReference<HelloService> reference=rsf.getReference(HelloService.class, properties);
        assertNotNull(reference);
        HelloService remote = rsf.getService(reference);
        assertNotNull(remote);
        remote.say("testuser");
        

    }
    private Dictionary<String, Object> mockConfig(){
        Hashtable<String, Object> table = new Hashtable<String, Object>();
        table.put(Constants.PATH_KEY, "/hola");
        table.put(Constants.PORT_KEY,PORT);
        table.put(Constants.RECEIVE_TIMEOUT_KEY,1000*60);
        return table;
    }

    static Container container;

    @BeforeClass
    public static void setup() {
        ContainerFactory.setThreadDefaultContainer(null);
        container = ContainerFactory.getThreadDefaultContainer(true);
    }

    @AfterClass
    public static void tearDown() {
        if (container != null) {
            container.close();
        }
    }
}
