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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.core.serialize.java.JavaSerialization;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.exchange.ExchangeChannel;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.ExchangerProvider;
import org.solmix.runtime.Containers;
/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月13日
 */

public class HeartbeatTest
{

    private ExchangeServer server;
    private ExchangeClient client;
    
    @Test
    public void testHeartbeat() throws TransportException, InterruptedException{
        HeartbeatHandler handler = new HeartbeatHandler();
        RemoteInfo info =  RemoteInfo.newBuilder()
            .setHost("localhost")
            .setPort(1314)
            .setHeartbeat(null)
            .setSerialName(JavaSerialization.NAME)
            .build();
        ExchangerProvider t= Containers.get().getExtensionLoader(ExchangerProvider.class).getDefault();
        server= t.bind(info,handler );
        RemoteInfo cinfo =  RemoteInfo.newBuilder(info)
            .setHeartbeat(100)
            .setHeartbeatTimeout(300)
            .setReconnect(true)
            .build();
        
        client =t.connect(cinfo,handler);
        Thread.sleep(1000);
        //心跳一直正常,所有未有重连
        Assert.assertTrue(handler.discount==0);
    }
    /**
     * 这种模式为服务端不发送心跳也不知道接收心跳,客户端发送心跳,如果心跳超时,启动重连
     */
    @Test
    public void testClientHeartbeat() throws TransportException, InterruptedException{
        HeartbeatHandler handler = new HeartbeatHandler();
        RemoteInfo info =  RemoteInfo.newBuilder()
            .setHost("localhost")
            .setPort(1314)
            .setHeartbeat(null)
            .setTransport("noheartbeat")
            .setSerialName(JavaSerialization.NAME)
            .build();
        ExchangerProvider t= Containers.get().getExtensionLoader(ExchangerProvider.class).getDefault();
        server= t.bind(info,handler );
        RemoteInfo cinfo =  RemoteInfo.newBuilder(info)
            .setHeartbeat(100)
            .setHeartbeatTimeout(300)
             .setTransport("noheartbeat")
            .setReconnect(true)
            .build();
        
        client =t.connect(cinfo,handler);
        Thread.sleep(1000);
        //由于服务端不支持心跳,所有客服端发送的心跳失败,到达一定周期后关闭链路重新连接
        Assert.assertTrue(handler.discount>0);
    }
    @After
    public void after() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }

        if (server != null) {
            server.close();
            server = null;
        }
    }
    class HeartbeatHandler implements ExchangeHandler{

        public int count=0;
        public int discount=0;
        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#connected(org.solmix.hola.transport.channel.Channel)
         */
        @Override
        public void connected(Channel channel) throws TransportException {
            count++;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#disconnected(org.solmix.hola.transport.channel.Channel)
         */
        @Override
        public void disconnected(Channel channel) throws TransportException {
            discount++;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#sent(org.solmix.hola.transport.channel.Channel, java.lang.Object)
         */
        @Override
        public void sent(Channel channel, Object message)
            throws TransportException {
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#received(org.solmix.hola.transport.channel.Channel, java.lang.Object)
         */
        @Override
        public void received(Channel channel, Object message)
            throws TransportException {
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#caught(org.solmix.hola.transport.channel.Channel, java.lang.Throwable)
         */
        @Override
        public void caught(Channel channel, Throwable exception)
            throws TransportException {
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.exchange.ExchangeHandler#reply(org.solmix.hola.transport.exchange.ExchangeChannel, java.lang.Object)
         */
        @Override
        public Object reply(ExchangeChannel channel, Object msg) {
            return null;
        }
        
    }
}
