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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ResponseFuture;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年8月21日
 */

public class LazyConnectExchangeClient implements ExchangeClient
{

    private final static Logger logger = LoggerFactory.getLogger(LazyConnectExchangeClient.class);

    private final RemoteInfo                     info;
    private final ExchangeHandler         requestHandler;
    private volatile ExchangeClient       client;
    private final Lock                    connectLock = new ReentrantLock();
    //lazy connect 如果没有初始化时的连接状态
    private final boolean                 initialState ;
    
    protected final  boolean requestWithWarning;
    
    //当调用时warning，出现这个warning，表示程序可能存在bug.
    static final  String REQUEST_WITH_WARNING_KEY = "lazyclient_request_with_warning";
    
    private final AtomicLong warningcount = new AtomicLong(0);
    private final HolaRemoteServiceManager container;
    /**
     * @param lazyInfo
     * @param exchangeHandler
     */
    public LazyConnectExchangeClient(RemoteInfo lazyInfo,
        ExchangeHandler exchangeHandler,HolaRemoteServiceManager container)
    {
        lazyInfo=  lazyInfo.addProperty(RemoteInfo.RECONNECT, false);
        this.container=container;
        this.info = lazyInfo;
        this.requestHandler = exchangeHandler;
        this.initialState = true;
        this.requestWithWarning = info.getBoolean(REQUEST_WITH_WARNING_KEY, false);
    
    }

    private void initClient() throws TransportException {
        if (client != null )
            return;
        if (logger.isInfoEnabled()) {
            logger.info("Lazy connect to " + info);
        }
        connectLock.lock();
        try {
            if (client != null)
                return;
            this.client = container.createClient(info, requestHandler);
        } finally {
            connectLock.unlock();
        }
    }

    @Override
    public ResponseFuture request(Object request) throws TransportException {
        warning(request);
        initClient();
        return client.request(request);
    }

    @Override
    public RemoteInfo getInfo() {
        return info;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        if (client == null){
            return InetSocketAddress.createUnresolved(info.getHost(), info.getPort());
        } else {
            return client.getRemoteAddress();
        }
    }
    @Override
    public ResponseFuture request(Object request, int timeout) throws TransportException {
        warning(request);
        initClient();
        return client.request(request, timeout);
    }
    
    /**
     * 如果配置了调用warning，则每调用5000次warning一次.
     * @param request
     */
    private void warning(Object request){
        if (requestWithWarning ){
            if (warningcount.get() % 5000 == 0){
                logger.warn("safe guard client , should not be called ,must have a bug.");
            }
            warningcount.incrementAndGet() ;
        }
    }
    
    @Override
    public ChannelHandler getChannelHandler() {
        checkClient();
        return client.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        if (client == null) {
            return initialState;
        } else {
            return client.isConnected();
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        if (client == null){
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(), 0);
        } else {
            return client.getLocalAddress();
        }
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return requestHandler;
    }

    @Override
    public void send(Object message) throws TransportException {
        initClient();
        client.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws TransportException {
        initClient();
        client.send(message, sent);
    }

    @Override
    public boolean isClosed() {
        if (client != null)
            return client.isClosed();
        else
            return true;
    }

    @Override
    public void close() {
        if (client != null)
            client.close();
    }

    @Override
    public void close(int timeout) {
        if (client != null)
            client.close(timeout);
    }

    @Override
    public void refresh(RemoteInfo info) {
        checkClient();
        client.refresh(info);
    }
    
 
    @Override
    public void reconnect() throws TransportException {
        checkClient();
        client.reconnect();
    }

    @Override
    public Object getAttribute(String key) {
        if (client == null){
            return null;
        } else {
            return client.getAttribute(key);
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        checkClient();
        client.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        checkClient();
        client.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        if (client == null){
            return false;
        } else {
            return client.hasAttribute(key);
        }
    }

    private void checkClient() {
        if (client == null) {
            throw new IllegalStateException(
                    "LazyConnectExchangeClient state error. the client has not be init .url:" + info);
        }
    }

}
