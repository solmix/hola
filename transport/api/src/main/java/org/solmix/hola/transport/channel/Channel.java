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

import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月14日
 */

public interface Channel
{
    /**
     * is connected.
     * 
     * @return connected
     */
    boolean isConnected();

    /**
     * has attribute.
     * 
     * @param key key.
     * @return has or has not.
     */
    boolean hasAttribute(String key);

    /**
     * get attribute.
     * 
     * @param key key.
     * @return value.
     */
    Object getAttribute(String key);

    /**
     * set attribute.
     * 
     * @param key key.
     * @param value value.
     */
    void setAttribute(String key,Object value);
    
    /**
     * remove attribute.
     * 
     * @param key key.
     */
    void removeAttribute(String key);

    /**
     * @return
     */
    boolean isClosed();

    /**
     * @param timeout
     */
    void close(int timeout);
    /**
     * 
     */
    void close();

    /**
     * @param res
     * @throws TransportException 
     */
    void send(Object res) throws TransportException;

    /**
     * @return
     */
    ChannelInfo getInfo();

    /**
     * @return
     */
    InetSocketAddress getRemoteAddress();

    /**
     * @return
     */
    InetSocketAddress getLocalAddress();

    /**
     * @param message
     * @param sent
     * @throws TransportException 
     */
    void send(Object message, boolean wait) throws TransportException;

    /**
     * @return
     */
    ChannelHandler getChannelHandler();
}
