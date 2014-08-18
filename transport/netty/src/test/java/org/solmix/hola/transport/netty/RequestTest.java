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
package org.solmix.hola.transport.netty;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.core.serialize.java.JavaSerialization;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.exchange.ExchangeChannel;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.ExchangerProvider;
import org.solmix.hola.transport.exchange.Replier;
import org.solmix.hola.transport.exchange.ResponseFuture;
import org.solmix.hola.transport.handler.ChannelHandlerAdapter;
import org.solmix.runtime.Containers;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月18日
 */

public class RequestTest extends TestCase
{

    private ExchangeServer server;
    
    private ExchangeClient client;
    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        int port = NetUtils.getAvailablePort();
        ChannelInfo info = ChannelInfo.newBuilder().setHost("localhost")
            .setPort(port)
            .setReconnect(true)
            .setReconnectPeriod(500)
            .setSerialName(JavaSerialization.NAME)
            .setCheck(false).build();
        ExchangerProvider ex = Containers.get().getExtensionLoader(
            ExchangerProvider.class).getDefault();
       

        server = ex.bind(info, new ChannelHandlerAdapter(),  new Replier<RequestData>() {

            @Override
            public Object reply(ExchangeChannel channel, RequestData request)
                throws TransportException {
                return new RequestData(request.getMessage()+"serverReturn");
            }
        });
        client = ex.connect(info, new ChannelHandlerAdapter(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    @After
    protected void tearDown() throws Exception {
        super.tearDown();
        server.close();
        client.close();
    }

    @Test
    public void test() throws TransportException {
        ResponseFuture future = client.request(new RequestData("msg"));
        RequestData result = (RequestData)future.get();
        Assert.assertNotNull(result);
        Assert.assertEquals("msg"+"serverReturn",result.getMessage());
    }

}
