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
import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.common.config.RemoteServiceConfig;
import org.solmix.hola.rm.RemoteException;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月5日
 */

public class GenericRemoteManagerTest {

    private Container container;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        container= ContainerFactory.getDefaultContainer(true);
    }

    @Test
    public void test() {
        GenericRemoteManager grm = new GenericRemoteManager();
        RemoteServiceConfig rsc = new RemoteServiceConfig();
        rsc.setAddress("hola://localhost:12312");
        try {
            grm.registerService(HelloService.class, new HelloServiceImpl(), rsc);
        } catch (RemoteException e) {
            e.printStackTrace();
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
