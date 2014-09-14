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

import org.junit.Assert;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.ExchangerProvider;
import org.solmix.hola.transport.handler.ChannelHandlerAdapter;
import org.solmix.runtime.Containers;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月17日
 */

public class NettyConnectionTest extends Assert
{

     @Test
    public void testReconnection() throws TransportException,
        InterruptedException {
        int port = NetUtils.getAvailablePort();
        RemoteInfo info = RemoteInfo.newBuilder().setHost("localhost").setPort(
            port).setReconnect(true).setReconnectPeriod(500).setCheck(false).build();
        ExchangerProvider ex = Containers.get().getExtensionLoader(
            ExchangerProvider.class).getDefault();
        ExchangeClient c = ex.connect(info, new ChannelHandlerAdapter(), null);
        assertTrue(!c.isConnected());

        ExchangeServer s = ex.bind(info, new ChannelHandlerAdapter(), null);
        for (int i = 0; i < 100 && !c.isConnected(); i++) {
            Thread.sleep(10);
        }
        assertTrue(c.isConnected());
        c.close(2000);
        s.close(2000);

    }

     @Test
    public void testReconnectionWarn() throws TransportException,
        InterruptedException {
        int port = NetUtils.getAvailablePort();
        RemoteInfo info = RemoteInfo.newBuilder().setHost("localhost").setPort(
            port).setReconnect(true).setReconnectPeriod(10).setReconnectWarningPeriod(
            10).setCheck(false).build();
        ExchangerProvider ex = Containers.get().getExtensionLoader(
            ExchangerProvider.class).getDefault();
        ExchangeClient c = ex.connect(info, new ChannelHandlerAdapter(), null);
        Thread.sleep(400);
    }

    @Test
    public void testmanualReconnect() throws TransportException,
        InterruptedException {
        int port = NetUtils.getAvailablePort();
        RemoteInfo info = RemoteInfo.newBuilder().setHost("localhost").setPort(
            port).setReconnect(true).setReconnectPeriod(10).setCheck(false).build();
        ExchangerProvider ex = Containers.get().getExtensionLoader(
            ExchangerProvider.class).getDefault();
        ExchangeClient c = ex.connect(info, new ChannelHandlerAdapter(), null);
        try {
            c.reconnect();
        } catch (Exception e) {
        }
        Thread.sleep(400);
    }
}
