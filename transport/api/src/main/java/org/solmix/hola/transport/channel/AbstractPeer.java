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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.codec.Codec;
import org.solmix.hola.transport.codec.ExchangeCodec;
import org.solmix.hola.transport.dispatch.Dispatcher;
import org.solmix.hola.transport.handler.HeartbeatHandler;
import org.solmix.hola.transport.handler.MultiMessageHandler;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年7月15日
 */

public class AbstractPeer
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractPeer.class);

    private final ChannelHandler handler;

    private Codec codec;

    private volatile RemoteInfo info;

    private int timeout;

    private int connectTimeout;

    private volatile boolean closed;
    
    private final Container container;

    public AbstractPeer(RemoteInfo info, ChannelHandler handler,Container container)
    {
        this.container=container;
        this.info = info;
        this.handler = wrapChannelHandler(info,handler);
        this.codec = getAdaptorCodec(info);
        this.timeout = info.getTimeout(HolaConstants.DEFAULT_TIMEOUT);
        this.connectTimeout = info.getConnectTimeout(HolaConstants.DEFAULT_TIMEOUT);
    }

    protected Container getContainer(){
        if(container==null)
            throw new IllegalStateException("container is null");
        return container;
    }
    /**
     * @endpointInfo endpointInfo
     * @return
     */
    protected  Codec getAdaptorCodec(RemoteInfo info) {
       String codec=info.getCodec(ExchangeCodec.NAME);
       if(codec==null){
           return getContainer().getExtensionLoader(Codec.class).getDefault();
       }else{
           return  getContainer().getExtensionLoader(Codec.class).getExtension(codec);
       }
    }

    public RemoteInfo getInfo() {
        return info;
    }

    public ChannelHandler getChannelHandler() {
        return handler;
    }

    /**
     * @endpointInfo endpointInfo the endpointInfo to set
     */
    public void setInfo(RemoteInfo endpointInfo) {
        this.info = info;
    }

    public boolean isClosed() {
        return closed;
    }

    public void refresh(RemoteInfo info) {
        if (isClosed()) {
            throw new IllegalStateException("Failed to reset endpointInfo "
                + info + ", cause: Channel closed.");
        }
        try {
            if (info.getHeartbeat()!=null) {
                int t = info.getHeartbeatTimeout();
                if (t > 0) {
                    this.timeout = t;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (info.getConnectTimeout()!=null) {
                int t = info.getConnectTimeout(0);
                if (t > 0) {
                    this.connectTimeout = t;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (info.getCodec()!=null) {
                this.codec = getAdaptorCodec(info);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    public void close() {
        closed = true;
    }

    /**
     * @return the codec
     */
    public Codec getCodec() {
        return codec;
    }

    public void close(int timeout) {
        close();
    }

    public void connected(Channel ch) throws TransportException {
        if (closed) {
            return;
        }
        handler.connected(ch);
    }

    public void disconnected(Channel ch) throws TransportException {
        handler.disconnected(ch);
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @return the connectTimeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    /**
     * 扩展channelHandler功能
     * @param info
     * @param handler
     * @return
     */
    protected ChannelHandler wrapChannelHandler(RemoteInfo info,
        ChannelHandler handler) {
        String dispatch = info.getDispather(HolaConstants.DEFAULT_DISPATHER);
        ChannelHandler dis = getContainer().getExtensionLoader(Dispatcher.class)
            .getExtension(dispatch)
            .dispatch(handler, info);
        return new MultiMessageHandler(new HeartbeatHandler(dis));
    }
    /**
     * @param info
     * @param defaultName
     * @return
     */
    protected  RemoteInfo setThreadName(RemoteInfo info ,String defaultName){
           String name = info.getThreadName(defaultName);
           name=new StringBuilder(32).append(name).append("-").append(info.getAddress()).toString();
           info=  info.addProperty(RemoteInfo.THREAD_NAME, name);
           return info;
       }

}
