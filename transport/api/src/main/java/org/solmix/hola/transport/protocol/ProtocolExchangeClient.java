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

package org.solmix.hola.transport.protocol;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.common.HolaConstants;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.exchange.ExchangeChannel;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ResponseFuture;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年7月14日
 */
@ThreadSafe
public class ProtocolExchangeClient implements ExchangeClient
{

    private static final Logger logger = LoggerFactory.getLogger(ProtocolExchangeClient.class);

    private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(
        2, new NamedThreadFactory("Hola-client-heartbeat", true));

    private ScheduledFuture<?> heatbeatTimer;

    private final int heartbeat;

    private final int heartbeatTimeout;

    private final Client client;

    private final ExchangeChannel channel;

    public ProtocolExchangeClient(Client client)
    {
        if (client == null) {
            throw new IllegalArgumentException("client is null");
        }
        this.client = client;
        this.channel = new ProtocolExchangeChannel(client);
        this.heartbeat = client.getInfo().getHeartbeat(HolaConstants.DEFAULT_HEARTBEAT);
        this.heartbeatTimeout = client.getInfo().getHeartbeatTimeout(heartbeat * 3);
        if (heartbeatTimeout < heartbeat * 2) {
            throw new IllegalStateException(
                "heartbeatTimeout < heartbeatInterval * 2");
        }
        startHeatbeatTimer();
    }

    @Override
    public ResponseFuture request(Object request) throws TransportException {
        return channel.request(request);
    }

    @Override
    public RemoteInfo getInfo() {
        return channel.getInfo();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    @Override
    public ResponseFuture request(Object request, int timeout)
        throws TransportException {
        return channel.request(request, timeout);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channel.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return channel.getLocalAddress();
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return channel.getExchangeHandler();
    }

    @Override
    public void send(Object message) throws TransportException {
        channel.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws TransportException {
        channel.send(message, sent);
    }

    @Override
    public boolean isClosed() {
        return channel.isClosed();
    }

    @Override
    public void close() {
        doClose();
        channel.close();
    }

    @Override
    public void close(int timeout) {
        doClose();
        channel.close(timeout);
    }

    @Override
    public void refresh(RemoteInfo param) {
        client.refresh(param);
    }

    @Override
    public void reconnect() throws TransportException {
        client.reconnect();
    }

    @Override
    public Object getAttribute(String key) {
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        channel.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        channel.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        return channel.hasAttribute(key);
    }

    private void startHeatbeatTimer() {
        stopHeartbeatTimer();
        if (heartbeat > 0) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(
                new HeartBeatTask(new HeartBeatTask.ChannelProvider() {

                    @Override
                    public Collection<Channel> getChannels() {
                        return Collections.<Channel> singletonList(ProtocolExchangeClient.this);
                    }
                }, heartbeat, heartbeatTimeout),
                heartbeat, heartbeat, TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeatTimer() {
        if (heatbeatTimer != null && !heatbeatTimer.isCancelled()) {
            try {
                heatbeatTimer.cancel(true);
                scheduled.purge();
            } catch (Throwable e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        heatbeatTimer = null;
    }

    private void doClose() {
        stopHeartbeatTimer();
    }

    @Override
    public String toString() {
        return "HeaderExchangeClient [channel=" + channel + "]";
    }

}
