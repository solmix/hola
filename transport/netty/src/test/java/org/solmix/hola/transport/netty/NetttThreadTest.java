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

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.core.serialize.java.JavaSerialization;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月9日
 */

public class NetttThreadTest
{

    private NettyClient client;
    private NettyServer server;
    private TestHandler serverhandler;
    private TestHandler clienthandler;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Map<String, Object> parameters = new HashMap<String,Object>();
        parameters.put(HolaConstants.KEY_HOST, "localhost");
        parameters.put(HolaConstants.KEY_PORT, "1314");
        parameters.put(HolaConstants.KEY_SERIALIZATION, JavaSerialization.NAME);
//        parameters.put(HolaConstants.KEY_SENT, true);
//        parameters.put(HolaConstants.KEY_TIMEOUT, 60000);
        EndpointInfo info = new EndpointInfo(parameters);
        serverhandler= new TestHandler("1314", false);
        clienthandler= new TestHandler("1314", true);
        server=new NettyServer(info, serverhandler);
        client= new NettyClient(info, clienthandler);
       
    }
    class TestHandler implements ChannelHandler{

        private final String message;
        private boolean success;
        private final boolean client;

        public boolean isSuccess() {
            return success;
        }

        TestHandler(String msg, boolean client) {
            message = msg;
            this.client = client;
        }
        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#connected(org.solmix.hola.transport.channel.Channel)
         */
        @Override
        public void connected(Channel channel) throws TransportException {
            checkThreadName();
            output("connected");
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#disconnected(org.solmix.hola.transport.channel.Channel)
         */
        @Override
        public void disconnected(Channel channel) throws TransportException {
            checkThreadName();
            output("disconnected");
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#sent(org.solmix.hola.transport.channel.Channel, java.lang.Object)
         */
        @Override
        public void sent(Channel channel, Object message)
            throws TransportException {
            checkThreadName();
            output("sent");
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#received(org.solmix.hola.transport.channel.Channel, java.lang.Object)
         */
        @Override
        public void received(Channel channel, Object message)
            throws TransportException {
            checkThreadName();
           output("received");
            
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.transport.channel.ChannelHandler#caught(org.solmix.hola.transport.channel.Channel, java.lang.Throwable)
         */
        @Override
        public void caught(Channel channel, Throwable exception)
            throws TransportException {
            checkThreadName();
            output("caught");
            
        }
        private void checkThreadName() {
            if (!success) {
                success = Thread.currentThread().getName().contains(message);
            }
        }
        private void output(String method) {
            System.out.println(Thread.currentThread().getName()
                                   + " " + (client ? "client " + method : "server " + method));
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }

        if (server != null) {
            server.close();
            server = null;
        }
    }

    @Test
    public void test() throws Exception {
        client.send("hello");
        Thread.sleep(1000L * 5L);
        if (!serverhandler.isSuccess() || !clienthandler.isSuccess()) {
            Assert.fail();
        }
    }

}
