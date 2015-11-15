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
package org.solmix.hola.transport.netty;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.exchange.TransporterFactory;
import org.solmix.exchange.TransporterFactoryManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月16日
 */

public class NettyServerEngineFactoryTest extends Assert
{

    Container container;
    
    @Test
    public void testGetEngine(){
        container=ContainerFactory.getDefaultContainer(true);
        TransporterFactoryManager tfm = container.getExtension(TransporterFactoryManager.class);
        assertNotNull(tfm);
        //是否可以被检测
        TransporterFactory tf= tfm.getFactory("netty");
        assertNotNull(tf);
        assertTrue(NettyTransportFactory.class.isInstance(tf));
        
        NettyServerEngineFactory nsef = container.getExtension(NettyServerEngineFactory.class);
        assertNotNull(nsef);
    }
    
    @BeforeClass
    public static void setUp(){
        ContainerFactory.setDefaultContainer(null);
    }
    
    @AfterClass
    public static void tearDown(){
        ContainerFactory.setDefaultContainer(null);
    }
}