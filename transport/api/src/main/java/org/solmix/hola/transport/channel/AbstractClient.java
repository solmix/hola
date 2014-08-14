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

package org.solmix.hola.transport.channel;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.handler.MultiMessageHandler;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月19日
 */

public abstract class AbstractClient extends AbstractPeer implements Client
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);

    public static final String THREAD_POOL_NAME = "ChannelHandler";

    private final Lock connectLock = new ReentrantLock();

    private final long shutdownTimeout;

    private final int reconnectWarningPeriod;

    private final boolean sendReconnect;
    public static boolean KEY_CHECK=true;
    
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(
        2, new NamedThreadFactory("ClientReconnectTimer", true));

    private final AtomicInteger reconnect_count = new AtomicInteger(0);

    private final AtomicBoolean reconnect_error_log_flag = new AtomicBoolean(
        false);

    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;

    private long lastConnectedTime = System.currentTimeMillis();

    /**
     * @param param
     * @param handler
     * @throws TransportException
     */
    public AbstractClient(ChannelInfo info, ChannelHandler handler)
        throws TransportException
    {
        super(info, handler);
        sendReconnect =info.getReconnect(false);
        shutdownTimeout = info.getShutdownTimeout(HolaConstants.DEFAULT_SHUTDOWN_TIMEOUT);
        reconnectWarningPeriod = info.getReconnectWarningPeriod(HolaConstants.DEFAULT_RECONNECT_WARNING_PERIOD);
        try {
            doOpen();
        } catch (Throwable t) {
            close();
            throw new TransportException(info.toInetSocketAddress(), null,
                "Failed to start " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalAddress() + " connect to the server "
                    + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
        try {
            // connect.
            connect();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalAddress() + " connect to the server "
                    + getRemoteAddress());
            }
        } catch (TransportException t) {
            if (KEY_CHECK) {
                close();
                throw t;
            } else {
                logger.warn(
                    "Failed to start " + getClass().getSimpleName() + " "
                        + NetUtils.getLocalAddress()
                        + " connect to the server " + getRemoteAddress()
                        + " (check == false, ignore and retry later!), cause: "
                        + t.getMessage(), t);
            }
        } catch (Throwable t) {
            close();
            throw new TransportException(info.toInetSocketAddress(), null,
                "Failed to start " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalAddress() + " connect to the server "
                    + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
    }

    protected static ChannelHandler wrapChannelHandler(ChannelInfo info,
        ChannelHandler handler) {
        info.setThreadName(THREAD_POOL_NAME);
        info.setThreadPool(HolaConstants.DEFAULT_THREADPOOL);
        return new MultiMessageHandler(handler);
    }

    protected void connect() throws TransportException {
        connectLock.lock();
        try {
            if (isConnected()) {
                return;
            }
            initConnectStatusCheckCommand();
            doConnect();
            if (!isConnected()) {
                throw new TransportException(this, "Failed connect to server "
                    + getRemoteAddress() + " from "
                    + getClass().getSimpleName() + " "
                    + NetUtils.getLocalHost()
                    + ", cause: Connect wait timeout: " + getTimeout() + "ms.");
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Successed connect to server "
                        + getRemoteAddress() + " from "
                        + getClass().getSimpleName() + " "
                        + NetUtils.getLocalHost() + ", channel is "
                        + this.getChannel());
                }
            }
            reconnect_count.set(0);
            reconnect_error_log_flag.set(false);
        } catch (TransportException e) {
            throw e;
        } catch (Throwable e) {
            throw new TransportException(this, "Failed connect to server "
                + getRemoteAddress() + " from " + getClass().getSimpleName()
                + " " + NetUtils.getLocalHost() + ", cause: " + e.getMessage(),
                e);
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * 
     */
    private void initConnectStatusCheckCommand() {
        int reconnect = getReconnectParam(getInfo());
        if (reconnect > 0
            && (reconnectExecutorFuture == null || reconnectExecutorFuture.isCancelled())) {
            Runnable connectStatusCheckCommand = new Runnable() {

                @Override
                public void run() {
                    try {
                        if (!isConnected()) {
                            connect();
                        } else {
                            lastConnectedTime = System.currentTimeMillis();
                        }
                    } catch (Throwable t) {
                        String errorMsg = "client reconnect to "
                            + getInfo().getAddress();
                        // wait registry sync provider list
                        if (System.currentTimeMillis() - lastConnectedTime > shutdownTimeout) {
                            if (!reconnect_error_log_flag.get()) {
                                reconnect_error_log_flag.set(true);
                                logger.error(errorMsg, t);
                                return;
                            }
                        }
                        if (reconnect_count.getAndIncrement()
                            % reconnectWarningPeriod == 0) {
                            logger.warn(errorMsg, t);
                        }
                    }
                }
            };
            reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(
                connectStatusCheckCommand, reconnect, reconnect,
                TimeUnit.MILLISECONDS);
        }
    }

    /**
     * @param parameters
     * @return
     */
    private int getReconnectParam(ChannelInfo info) {
        int reconnect;
        Boolean re=info.getReconnect();
        if (re!=null&&re.booleanValue()) {
            reconnect =info.getReconnectPeriod( HolaConstants.DEFAULT_RECONNECT_PERIOD);
        } else {
            reconnect=0;
        }
        return reconnect;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#isConnected()
     */
    @Override
    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null)
            return false;
        return channel.isConnected();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null)
            return false;
        return channel.hasAttribute(key);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null)
            return null;
        return channel.getAttribute(key);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void setAttribute(String key, Object value) {
        Channel channel = getChannel();
        if (channel == null)
            return;
        channel.setAttribute(key, value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null)
            return;
        channel.removeAttribute(key);

    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        if (channel == null)
            return getInfo().toInetSocketAddress();
        return channel.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        Channel channel = getChannel();
        if (channel == null)
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(),
                0);
        return channel.getLocalAddress();
    }

    @Override
    public void send(Object message) throws TransportException {
        send(message, getInfo().getAwait(false));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#send(java.lang.Object,
     *      boolean)
     */
    @Override
    public void send(Object message, boolean sent) throws TransportException {
        if (sendReconnect && !isConnected()) {
            connect();
        }
        Channel channel = getChannel();
        if (channel == null || !channel.isConnected()) {
            throw new TransportException(this,
                "message can not send, because channel is closed . url:");
        }
        channel.send(message, sent);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Client#reconnect()
     */
    @Override
    public void reconnect() throws TransportException {
        disconnect();
        connect();
    }

    public void disconnect() {
        connectLock.lock();
        try {
            destroyConnectStatusCheckCommand();
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                doDisConnect();
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * 
     */
    private void destroyConnectStatusCheckCommand() {
        try {
            if (reconnectExecutorFuture != null
                && !reconnectExecutorFuture.isDone()) {
                reconnectExecutorFuture.cancel(true);
                reconnectExecutorService.purge();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    protected InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(
            NetUtils.filterLocalHost(getInfo().getHost()),
            getInfo().getPort());
    }

    /**
     * Open client.
     * 
     * @throws Throwable
     */
    protected abstract void doOpen() throws Throwable;

    /**
     * Close client.
     * 
     * @throws Throwable
     */
    protected abstract void doClose() throws Throwable;

    /**
     * Connect to server.
     * 
     * @throws Throwable
     */
    protected abstract void doConnect() throws Throwable;

    /**
     * disConnect to server.
     * 
     * @throws Throwable
     */
    protected abstract void doDisConnect() throws Throwable;

    /**
     * Get the connected channel.
     * 
     * @return channel
     */
    protected abstract Channel getChannel();
}
