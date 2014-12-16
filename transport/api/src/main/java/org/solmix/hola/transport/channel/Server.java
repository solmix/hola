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

import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.transport.TransportException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年7月14日
 */

public interface Server 
{
    RemoteInfo getInfo();
    
    void refresh(RemoteInfo param);
    /**
     * @return
     */
    Collection<Channel> getChannels();
    /**
     * @param remoteAddress
     * @return
     */
    Channel getChannel(InetSocketAddress remoteAddress);
    /**
     * @return
     */
    
    boolean isActive();
    
    ChannelHandler getChannelHandler();

    /**
     * get local address.
     * 
     * @return local address.
     */
    InetSocketAddress getLocalAddress();
    
    /**
     * send message.
     * 
     * @param message
     * @throws RemotingException
     */
    void send(Object message) throws TransportException;

    /**
     * send message.
     * 
     * @param message
     * @param sent 是否已发送完成
     */
    void send(Object message, boolean sent) throws TransportException;

    /**
     * close the channel.
     */
    void close();
    
    /**
     * Graceful close the channel.
     */
    void close(int timeout);
    
    /**
     * is closed.
     * 
     * @return closed
     */
    boolean isClosed();
   
}
