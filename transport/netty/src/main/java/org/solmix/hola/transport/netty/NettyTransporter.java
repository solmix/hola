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
package org.solmix.hola.transport.netty;

import org.solmix.hola.core.Parameters;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.Transporter;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月15日
 */

public class NettyTransporter implements Transporter
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.Transporter#newServer(org.solmix.hola.transport.channel.ChannelHandler, org.solmix.hola.core.Parameters)
     */
    @Override
    public Server newServer(ChannelHandler handler, Parameters parameter)
        throws TransportException {
        return new NettyServer(parameter, handler);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.Transporter#newClient(org.solmix.hola.transport.channel.ChannelHandler, org.solmix.hola.core.Parameters)
     */
    @Override
    public Client newClient(ChannelHandler handler, Parameters parameter)
        throws TransportException {
        return new NettyClient(parameter, handler);
    }

}
