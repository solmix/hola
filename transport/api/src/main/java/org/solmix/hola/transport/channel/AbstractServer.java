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
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ExecutorUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.Parameters;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.handler.WrappedChannelHandler;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月15日
 */

public abstract class AbstractServer extends AbstractPeer implements Server
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    private InetSocketAddress              localAddress;

    private InetSocketAddress              bindAddress;
 
    private int                  accepts;

    private int idleTimeout         = 600;
    ExecutorService executor;
    public AbstractServer(Parameters param,ChannelHandler handler) throws TransportException{
        super(param,handler);
        this.accepts =param.getInt(HolaConstants.KEY_CHANNEL_ACCEPTS, HolaConstants.DEFAULT_CHANNEL_ACCEPTS);
        this.idleTimeout = param.getInt(HolaConstants.KEY_IDLE_TIMEOUT, HolaConstants.DEFAULT_IDLE_TIMEOUT);
        try {
            doOpen();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " bind " + getBindAddress() + ", export " + getLocalAddress());
            }
        } catch (Throwable t) {
            throw new TransportException(localAddress, null, "Failed to bind " + getClass().getSimpleName() 
                                        + " on " + getLocalAddress() + ", cause: " + t.getMessage(), t);
        }
        if (handler instanceof WrappedChannelHandler ){
            executor = ((WrappedChannelHandler)handler).getExecutor();
        }
    }
  
    
    @Override
    public void refresh(Parameters param) {
        if(param==null)
            return ;
        try {
            if (param.hasParameter(HolaConstants.KEY_CHANNEL_ACCEPTS)) {
                int a = param.getInt(HolaConstants.KEY_CHANNEL_ACCEPTS, 0);
                if (a > 0) {
                    this.accepts = a;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (param.hasParameter(HolaConstants.KEY_IDLE_TIMEOUT)) {
                int t = param.getInt(HolaConstants.KEY_IDLE_TIMEOUT, 0);
                if (t > 0) {
                    this.idleTimeout = t;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (param.hasParameter(HolaConstants.KEY_THREADS) 
                    && executor instanceof ThreadPoolExecutor && !executor.isShutdown()) {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                int threads = param.getInt(HolaConstants.KEY_THREADS, 0);
                int max = threadPoolExecutor.getMaximumPoolSize();
                int core = threadPoolExecutor.getCorePoolSize();
                if (threads > 0 && (threads != max || threads != core)) {
                    if (threads < core) {
                        threadPoolExecutor.setCorePoolSize(threads);
                        if (core == max) {
                            threadPoolExecutor.setMaximumPoolSize(threads);
                        }
                    } else {
                        threadPoolExecutor.setMaximumPoolSize(threads);
                        if (core == max) {
                            threadPoolExecutor.setCorePoolSize(threads);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        setParameters(getParameters().addParameters(param.getParameters()));
    }
    @Override
    public void send(Object message, boolean sent) throws TransportException {
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            if (channel.isConnected()) {
                channel.send(message, sent);
            }
        }
    }
    @Override
    public void send(Object message) throws TransportException {
        send(message, getParameters().getBoolean(HolaConstants.KEY_SENT, false));
    }
    @Override
    public void close(int timeout) {
        ExecutorUtils.gracefulShutdown(executor ,timeout);
        close();
    }

    @Override
    public void close() {
        if (logger.isInfoEnabled()) {
            logger.info("Close " + getClass().getSimpleName() + " bind " + getBindAddress() + ", export " + getLocalAddress());
        }
        ExecutorUtils.shutdownNow(executor ,100);
        try {
            super.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }
    
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }
    public int getAccepts() {
        return accepts;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }
    @Override
    public void connected(Channel ch) throws TransportException {
        Collection<Channel> channels = getChannels();
        if (accepts > 0 && channels.size() > accepts) {
            logger.error("Close channel " + ch + ", cause: The server " + ch.getLocalAddress() + " connections greater than max config " + accepts);
            ch.close();
            return;
        }
        super.connected(ch);
    }
    
    @Override
    public void disconnected(Channel ch) throws TransportException {
        Collection<Channel> channels = getChannels();
        if (channels.size() == 0){
            logger.warn("All clients has discontected from " + ch.getLocalAddress() + ". You can graceful shutdown now.");
        }
        super.disconnected(ch);
    }
   
    protected abstract void doOpen() throws Throwable;
    
    protected abstract void doClose() throws Throwable;
}
