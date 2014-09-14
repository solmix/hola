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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Server;
import org.solmix.hola.transport.exchange.DefaultFuture;
import org.solmix.hola.transport.exchange.ExchangeChannel;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.Request;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年7月14日
 */
@ThreadSafe
public class ProtocolExchangeServer implements ExchangeServer
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(
        1, new NamedThreadFactory("Hola-server-heartbeat", true));

    private ScheduledFuture<?> heatbeatTimer;

    private int heartbeat;

    private int heartbeatTimeout;

    private final Server server;

    private volatile boolean closed = false;

    public ProtocolExchangeServer(Server server)
    {
        if (server == null) {
            throw new IllegalArgumentException("server == null");
        }
        this.server = server;
        this.heartbeat = server.getInfo().getHeartbeat(0);
        this.heartbeatTimeout = server.getInfo().getHeartbeatTimeout( heartbeat * 3);
        if (heartbeatTimeout < heartbeat * 2) {
            throw new IllegalStateException(
                "heartbeatTimeout < heartbeatInterval * 2");
        }
        startHeatbeatTimer();
    }

    public Server getServer() {
        return server;
    }

    @Override
    public boolean isClosed() {
        return server.isClosed();
    }

    private boolean isRunning() {
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            if (DefaultFuture.hasFuture(channel)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        doClose();
        server.close();
    }

    @Override
    public void close(final int timeout) {
        if (timeout > 0) {
            final long max = timeout;
            final long start = System.currentTimeMillis();
            /*
             * if
             * (getInfo().getBoolean(HolaConstants.KEY_CHANNEL_SEND_READONLYEVENT
             * , false)){ sendChannelReadOnlyEvent(); }
             */
            while (ProtocolExchangeServer.this.isRunning()
                && System.currentTimeMillis() - start < max) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        doClose();
        server.close(timeout);
    }

    private void sendChannelReadOnlyEvent() {
        Request request = new Request();
        request.setEvent(Request.READONLY_EVENT);
        request.setTwoWay(false);
        request.setVersion(HolaConstants.VERSION);

        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            try {
                if (channel.isConnected())
                    channel.send(request, getInfo().getAwait(true));
            } catch (TransportException e) {
                logger.warn("send connot write messge error.", e);
            }
        }
    }

    private void doClose() {
        if (closed) {
            return;
        }
        closed = true;
        stopHeartbeatTimer();
        try {
            scheduled.shutdown();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    @Override
    public Collection<ExchangeChannel> getExchangeChannels() {
        Collection<ExchangeChannel> exchangeChannels = new ArrayList<ExchangeChannel>();
        Collection<Channel> channels = server.getChannels();
        if (channels != null && channels.size() > 0) {
            for (Channel channel : channels) {
                exchangeChannels.add(ProtocolExchangeChannel.getOrAddChannel(channel));
            }
        }
        return exchangeChannels;
    }

    @Override
    public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
        Channel channel = server.getChannel(remoteAddress);
        return ProtocolExchangeChannel.getOrAddChannel(channel);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<Channel> getChannels() {
        return (Collection) getExchangeChannels();
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return getExchangeChannel(remoteAddress);
    }

    @Override
    public boolean isActive() {
        return server.isActive();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    @Override
    public RemoteInfo getInfo() {
        return server.getInfo();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return server.getChannelHandler();
    }

    @Override
    public void refresh(RemoteInfo info) {
        server.refresh(info);
        try {
            if (info.getHeartbeat() != null
                || info.getHeartbeatTimeout() != null) {
                int h = info.getHeartbeat(heartbeat);
                int t = info.getHeartbeatTimeout(h * 3);
                if (t < h * 2) {
                    throw new IllegalStateException(
                        "heartbeatTimeout < heartbeatInterval * 2");
                }
                if (h != heartbeat || t != heartbeatTimeout) {
                    heartbeat = h;
                    heartbeatTimeout = t;
                    startHeatbeatTimer();
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    @Override
    public void send(Object message) throws TransportException {
        if (closed) {
            throw new TransportException(this.getLocalAddress(), null,
                "Failed to send message " + message + ", cause: The server "
                    + getLocalAddress() + " is closed!");
        }
        server.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws TransportException {
        if (closed) {
            throw new TransportException(this.getLocalAddress(), null,
                "Failed to send message " + message + ", cause: The server "
                    + getLocalAddress() + " is closed!");
        }
        server.send(message, sent);
    }

    private void startHeatbeatTimer() {
        stopHeartbeatTimer();
        if (heartbeat > 0) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(
                new HeartBeatTask(new HeartBeatTask.ChannelProvider() {

                    @Override
                    public Collection<Channel> getChannels() {
                        return Collections.unmodifiableCollection(ProtocolExchangeServer.this.getChannels());
                    }
                }, 
                heartbeat, heartbeatTimeout), 
                heartbeat, heartbeat, TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeatTimer() {
        try {
            ScheduledFuture<?> timer = heatbeatTimer;
            if (timer != null && !timer.isCancelled()) {
                timer.cancel(true);
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        } finally {
            heatbeatTimer = null;
        }
    }

}
