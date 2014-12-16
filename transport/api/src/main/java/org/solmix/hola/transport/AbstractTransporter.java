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
package org.solmix.hola.transport;

import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;
import org.solmix.hola.transport.handler.ChannelHandlerDispatcher;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月17日
 */

public abstract class AbstractTransporter implements TransporterProvider
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.TransporterProvider#bind(org.solmix.hola.common.config.RemoteInfo, org.solmix.hola.transport.channel.ChannelHandler[])
     */
    @Override
    public Server bind(RemoteInfo info, ChannelHandler... handlers)
        throws TransportException {
        if (info == null) {
            throw new IllegalArgumentException("RemoteInfo is null");
        }
        if (handlers == null || handlers.length == 0) {
            throw new IllegalArgumentException("handlers == null");
        }
        ChannelHandler handler;
        if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return newServer(info, handler);
    }
    protected abstract Server newServer(RemoteInfo info, ChannelHandler handler)throws TransportException ;
    protected abstract Client newClient(RemoteInfo info, ChannelHandler handler) throws TransportException;
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.TransporterProvider#connect(org.solmix.hola.common.config.RemoteInfo, org.solmix.hola.transport.channel.ChannelHandler[])
     */
    @Override
    public Client connect(RemoteInfo info, ChannelHandler... handlers)
        throws TransportException {
        if (info == null) {
            throw new IllegalArgumentException("RemoteInfo is null");
        }
        if (handlers == null || handlers.length == 0) {
            throw new IllegalArgumentException("handlers == null");
        }
        ChannelHandler handler;
        if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return newClient(info, handler);
    }

}
