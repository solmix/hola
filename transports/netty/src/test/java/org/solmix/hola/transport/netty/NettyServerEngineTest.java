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

import java.io.IOException;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.exchange.Message;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.transport.RemoteAddress;
import org.solmix.hola.transport.RemoteProtocol;
import org.solmix.runtime.Container;
import org.solmix.runtime.cm.Configurer;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月16日
 */

public class NettyServerEngineTest extends Assert
{

    private static final int PORT1= NetUtils.getAvailablePort();
    
    private static final int PORT2= NetUtils.getAvailablePort();
    
    private Container container;

    private IMocksControl control;

    private NettyServerEngineFactory factory;
    
    private RemoteAddress remoteAddress;

    @Before
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
        container = control.createMock(Container.class);
        
        Configurer configurer = control.createMock(Configurer.class);
        container.getExtension(Configurer.class);
        EasyMock.expectLastCall().andReturn(configurer).anyTimes();
        
        control.replay();

        factory = new NettyServerEngineFactory();
        factory.setContainer(container);
        remoteAddress=new RemoteAddress("hola",HOLA.LOCALHOST_VALUE,PORT1);
    }
    

    @Test
    public void testEngineRetrieval() throws Exception {
        NettyServerEngine engine =  factory.createEngine(remoteAddress);
        assertTrue(engine == factory.retrieveEngine( remoteAddress));
        NettyServerEngineFactory.destroy(PORT1);
    }
    
    @Test
    public void testAddHandler() throws Exception {
        String urlStr = "http://localhost:" + PORT1 + "/test";
        NettyServerEngine nse = factory.createEngine(remoteAddress);
        nse.setNettyConfiguration(new NettyConfiguration());
        RemoteProtocol protocol = new RemoteProtocol(null, container, null);
        nse.start(protocol);
        NettyMessageHandler handler =new TestHandler("hello");
        nse.addHandler(urlStr, handler);
        NettyMessageHandler handler2 =nse.getHandler(urlStr);
        Assert.assertSame(handler, handler2);
        nse.removeHandler(urlStr);
        Assert.assertNull(nse.getHandler(urlStr));
    }
    
    class TestHandler extends NettyMessageHandler{

        private String response;
        public TestHandler(String response)
        {
            super(null);
            this.response=response;
        }
        @Override
        public void handle( Message inMsg, Message outMsg)
            throws IOException {
                System.out.println(response);

        }
    }
}
