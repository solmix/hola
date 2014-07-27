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


import org.solmix.hola.core.Parameters;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;
import org.solmix.hola.transport.handler.ChannelHandlerAdapter;
import org.solmix.hola.transport.handler.ChannelHandlerDispatcher;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月14日
 */

public final class Transporters
{

    /**
     * @param parameter
     * @param handler
     * @return
     * @throws TransportException 
     */
    public static Server newServer(Parameters parameter, ChannelHandler... handlers) throws TransportException {
        ChannelHandler handler;
        if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
//        SystemContextFactory.getThreadDefaultSystemContext().getBean(beanType)
        return new NettyTransporter().newServer(handler, parameter);
    }

    /**
     * @param parameter
     * @param decodeHandler
     * @return
     * @throws TransportException 
     */
    public static Client newClient(Parameters parameter, ChannelHandler... handlers) throws TransportException {
        ChannelHandler handler;
        if (handlers == null || handlers.length == 0) {
            handler = new ChannelHandlerAdapter();
        } else if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return new NettyTransporter().newClient(handler, parameter);
    }

}
