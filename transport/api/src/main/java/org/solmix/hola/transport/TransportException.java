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

import java.net.InetSocketAddress;

import org.solmix.hola.transport.channel.Channel;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年7月14日
 */

public class TransportException extends Exception
{
    private static final long serialVersionUID = -3160452149606778709L;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    public TransportException(Channel channel, String msg){
        this(channel == null ? null : channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),
             msg);
    }

    public TransportException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message){
        super(message);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public TransportException(Channel channel, Throwable cause){
        this(channel == null ? null : channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),
             cause);
    }

    public TransportException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, Throwable cause){
        super(cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public TransportException(Channel channel, String message, Throwable cause){
        this(channel == null ? null : channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),
             message, cause);
    }

    public TransportException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message,
                             Throwable cause){
        super(message, cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
