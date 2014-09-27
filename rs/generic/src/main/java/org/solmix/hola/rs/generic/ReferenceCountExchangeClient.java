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

package org.solmix.hola.rs.generic;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ResponseFuture;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月21日
 */

public class ReferenceCountExchangeClient implements ExchangeClient
{

    private ExchangeClient client;

    private final RemoteInfo info;

    // private final ExchangeHandler handler;

    private final AtomicInteger refenceCount = new AtomicInteger(0);

    private final ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap;

    private final HolaRemoteManager manager;
    public ReferenceCountExchangeClient(ExchangeClient client,
        ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap,HolaRemoteManager manager)
    {
        this.client = client;
        this.manager =manager;
        refenceCount.incrementAndGet();
        this.info = client.getInfo();
        if (ghostClientMap == null) {
            throw new IllegalStateException(
                "ghostClientMap can not be null, url: " + info);
        }
        this.ghostClientMap = ghostClientMap;
    }

    @Override
    public void refresh(RemoteInfo info) {
        client.refresh(info);
    }

    @Override
    public ResponseFuture request(Object request) throws TransportException {
        return client.request(request);
    }

    @Override
    public RemoteInfo getInfo() {
        return client.getInfo();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return client.getRemoteAddress();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return client.getChannelHandler();
    }

    @Override
    public ResponseFuture request(Object request, int timeout)
        throws TransportException {
        return client.request(request, timeout);
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void reconnect() throws TransportException {
        client.reconnect();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return client.getLocalAddress();
    }

    @Override
    public boolean hasAttribute(String key) {
        return client.hasAttribute(key);
    }


    @Override
    public void send(Object message) throws TransportException {
        client.send(message);
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return client.getExchangeHandler();
    }

    @Override
    public Object getAttribute(String key) {
        return client.getAttribute(key);
    }

    @Override
    public void send(Object message, boolean sent) throws TransportException {
        client.send(message, sent);
    }

    @Override
    public void setAttribute(String key, Object value) {
        client.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        client.removeAttribute(key);
    }

    /*
     * close方法将不再幂等,调用需要注意.
     */
    @Override
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
        if (refenceCount.decrementAndGet() <= 0) {
            if (timeout == 0) {
                client.close();
            } else {
                client.close(timeout);
            }
            client = replaceWithLazyClient();
        }
    }

    // 幽灵client,
    private LazyConnectExchangeClient replaceWithLazyClient() {
        // 这个操作只为了防止程序bug错误关闭client做的防御措施，初始client必须为false状态
        RemoteInfo lazyInfo = info.addProperty(RemoteInfo.RECONNECT, false)
            .addProperty("warning", Boolean.TRUE.toString())
            .addProperty( LazyConnectExchangeClient.REQUEST_WITH_WARNING_KEY, true)
            .addProperty( "_client_memo", "referencecounthandler.replacewithlazyclient");

        String key = info.getAddress();
        // 最差情况下只有一个幽灵连接
        LazyConnectExchangeClient gclient = ghostClientMap.get(key);
        if (gclient == null || gclient.isClosed()) {
            gclient = new LazyConnectExchangeClient(new RemoteInfo(),
                client.getExchangeHandler(),manager);
            ghostClientMap.put(key, gclient);
        }
        return gclient;
    }

    @Override
    public boolean isClosed() {
        return client.isClosed();
    }

    public void incrementAndGetCount() {
        refenceCount.incrementAndGet();
    }

}
