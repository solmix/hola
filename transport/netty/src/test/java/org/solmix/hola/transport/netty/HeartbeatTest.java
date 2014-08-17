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
import org.junit.Test;
import org.solmix.hola.core.model.ChannelInfo;
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
    public void testHeartbeat() throws TransportException{
        HeartbeatHandler handler = new HeartbeatHandler();
        ChannelInfo info =  ChannelInfo.newBuilder()
            .setHost("localhost")
            .setPort(1314)
            .setHeartbeat(1000)
            .build();
        ExchangerProvider t= Containers.get().getExtensionLoader(ExchangerProvider.class).getDefault();
        server= t.bind(info,handler );
        ChannelInfo cinfo =  ChannelInfo.newBuilder(info).setHeartbeat(null).build();
        
        client =t.connect(cinfo,handler);
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

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#connected(org.solmix.hola.transport.channel.Channel)
         */
        @Override
        public void connected(Channel channel) throws TransportException {
            // TODO Auto-generated method stub
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#disconnected(org.solmix.hola.transport.channel.Channel)
         */
        @Override
        public void disconnected(Channel channel) throws TransportException {
            // TODO Auto-generated method stub
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#sent(org.solmix.hola.transport.channel.Channel, java.lang.Object)
         */
        @Override
        public void sent(Channel channel, Object message)
            throws TransportException {
            // TODO Auto-generated method stub
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#received(org.solmix.hola.transport.channel.Channel, java.lang.Object)
         */
        @Override
        public void received(Channel channel, Object message)
            throws TransportException {
            // TODO Auto-generated method stub
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#caught(org.solmix.hola.transport.channel.Channel, java.lang.Throwable)
         */
        @Override
        public void caught(Channel channel, Throwable exception)
            throws TransportException {
            // TODO Auto-generated method stub
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.exchange.ExchangeHandler#reply(org.solmix.hola.transport.exchange.ExchangeChannel, java.lang.Object)
         */
        @Override
        public Object reply(ExchangeChannel channel, Object msg) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
