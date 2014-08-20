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

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.handler.ChannelHandlerDelegate;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月15日
 */

public abstract class AbstractChannel implements Channel
{
    private volatile RemoteInfo info;
    
    private volatile boolean     closed;

    private final ChannelHandler handler;
    
    public AbstractChannel(RemoteInfo info,ChannelHandler handler){
        Assert.isNotNull(info);
        Assert.isNotNull(handler);
        this.info=info;
        this.handler=handler;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#isClosed()
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#close(int)
     */
    @Override
    public void close(int timeout) {
        close();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#close()
     */
    @Override
    public void close() {
        closed = true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#send(java.lang.Object)
     */
    @Override
    public void send(Object res) throws TransportException {
        send(res,info.getAwait(false));
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getInfo()
     */
    @Override
    public RemoteInfo getInfo() {
        return info;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#send(java.lang.Object, boolean)
     */
    @Override
    public void send(Object message, boolean wait) throws TransportException {
        if (isClosed()) {
            throw new TransportException(this, "Failed to send message "
                                              + (message == null ? "" : message.getClass().getName()) + ":" + message
                                              + ", cause: Channel closed. channel: " + getLocalAddress() + " -> " + getRemoteAddress());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getChannelHandler()
     */
    @Override
    public ChannelHandler getChannelHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getHandler();
        } else {
            return handler;
        }
    }
    
    @Override
    public String toString() {
        return getLocalAddress() + " -> " + getRemoteAddress();
    }
}
