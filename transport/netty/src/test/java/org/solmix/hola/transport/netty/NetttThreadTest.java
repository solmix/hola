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

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.core.serialize.java.JavaSerialization;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.TransporterProvider;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;
import org.solmix.runtime.Containers;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月9日
 */

public class NetttThreadTest
{

    private Client client;
    private Server server;
    private TestHandler serverhandler;
    private TestHandler clienthandler;
  private  CountDownLatch allMessages ;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
      
        RemoteInfo info = new RemoteInfo();
        info.setHost("localhost");
        info.setPort(1314);
        info.setSerialName(JavaSerialization.NAME);
        info.setAwait(true);
        info.setTimeout(6000);
        serverhandler= new TestHandler("1314", false);
        clienthandler= new TestHandler("1314", true);
        TransporterProvider t= Containers.get().getExtensionLoader(TransporterProvider.class).getExtension(NettyTransporter.NAME);
        server=t.bind(info,serverhandler);
        client= t.connect(info,clienthandler);
       
       
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
            allMessages.countDown();
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
           output("received-"+message);
            
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
         allMessages = new CountDownLatch(10000);
         for(int i=0;i<10000;i++)
        client.send("hello");
        if (!serverhandler.isSuccess() || !clienthandler.isSuccess()) {
            Assert.fail();
        }
    }

}
