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

package org.solmix.hola.transport.handler;

import org.solmix.commons.util.Assert;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年7月14日
 */

public abstract class AbstractChannelHandlerDelegate implements
    ChannelHandlerDelegate
{
    protected ChannelHandler handler;

    protected AbstractChannelHandlerDelegate(ChannelHandler handler) {
        Assert.isNotNull(handler, "handler == null");
        this.handler = handler;
    }

    public ChannelHandler getHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate)handler).getHandler();
        }
        return handler;
    }

    @Override
    public void connected(Channel channel) throws TransportException {
        handler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws TransportException {
        handler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws TransportException {
        handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws TransportException {
        handler.received(channel, message);
    }

    public void caught(Channel channel, Throwable exception) throws TransportException {
        handler.caught(channel, exception);
    }

}
