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
import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.codec.Codec;
import org.solmix.hola.transport.codec.ExchangeCodec;
import org.solmix.hola.transport.handler.MultiMessageHandler;
import org.solmix.runtime.Containers;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月15日
 */

public class AbstractPeer
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractPeer.class);

    private final ChannelHandler handler;

    private Codec codec;

    private volatile ChannelInfo info;

    private int timeout;

    private int connectTimeout;

    private volatile boolean closed;

    public AbstractPeer(ChannelInfo info, ChannelHandler handler)
    {
        this.info = info;
        this.handler = handler;
        this.codec = getAdaptorCodec(info);
        this.timeout = info.getTimeout();
        this.connectTimeout = info.getConnectTimeout(HolaConstants.DEFAULT_TIMEOUT);
    }

    /**
     * @endpointInfo endpointInfo
     * @return
     */
    protected static Codec getAdaptorCodec(ChannelInfo info) {
       String codec=info.getCodec(ExchangeCodec.NAME);
       if(codec==null){
           return Containers.get().getExtensionLoader(Codec.class).getDefault();
       }else{
           return  Containers.get().getExtensionLoader(Codec.class).getExtension(codec);
       }
    }

    public ChannelInfo getInfo() {
        return info;
    }

    public ChannelHandler getChannelHandler() {
        return handler;
    }

    /**
     * @endpointInfo endpointInfo the endpointInfo to set
     */
    public void setInfo(ChannelInfo endpointInfo) {
        this.info = info;
    }

    public boolean isClosed() {
        return closed;
    }

    public void refresh(ChannelInfo info) {
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
    
   protected static ChannelHandler wrapChannelHandler(ChannelHandler hander, ChannelInfo endpointInfo2) {
        // TODO Dispatcher.dispatch();
        return new MultiMessageHandler(hander);

    }
   /**
 * @param info
 * @param defaultName
 * @return
 */
protected static ChannelInfo setThreadName(ChannelInfo info ,String defaultName){
       String name = info.getThreadName(defaultName);
       name=new StringBuilder(32).append(name).append("-").append(info.getAddress()).toString();
        info.setThreadName(name);
       return info;
   }

}
