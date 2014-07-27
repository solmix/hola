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
import org.solmix.hola.core.Parameters;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.codec.Codec;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月15日
 */

public class AbstractPeer
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractPeer.class);

    private final ChannelHandler handler;

    private  Codec codec;

    private volatile Parameters parameters;
    private  int                   timeout;

    private  int                   connectTimeout;
    private volatile boolean     closed;
    public AbstractPeer(Parameters param, ChannelHandler handler)
    {
        this.parameters = param;
        this.handler = handler;
        this.codec = getCodec(param);
        this.timeout=param.getInt(HolaConstants.KEY_TIMEOUT, 0,true);
        this.connectTimeout=param.getInt(HolaConstants.KEY_CONNECT_TIMEOUT, 0,true);
    }

    /**
     * @param param
     * @return
     */
    private Codec getCodec(Parameters param) {
        return null;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public ChannelHandler getChannelHandler() {
        return handler;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
    
    public boolean isClosed() {
        return closed;
    }
    public void refresh(Parameters param) {
        if (isClosed()) {
            throw new IllegalStateException("Failed to reset parameters "
                                        + param + ", cause: Channel closed.");
        }
        try {
            if (param.hasParameter(HolaConstants.KEY_HEARTBEAT)) {
                int t = param.getInt(HolaConstants.KEY_TIMEOUT, 0);
                if (t > 0) {
                    this.timeout = t;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (param.hasParameter(HolaConstants.KEY_CONNECT_TIMEOUT)) {
                int t = param.getInt(HolaConstants.KEY_CONNECT_TIMEOUT, 0);
                if (t > 0) {
                    this.connectTimeout = t;
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (param.hasParameter(HolaConstants.KEY_CODEC)) {
                this.codec = getCodec(param);
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
    
}
