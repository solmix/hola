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
package org.solmix.hola.shared.transport;

import org.solmix.hola.core.HolaException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年6月25日
 */

public interface ChannelHandler
{
    /**
     * on channel connected.
     * 
     * @param channel channel.
     */
    void connected(Channel channel) throws HolaException;

    /**
     * on channel disconnected.
     * 
     * @param channel channel.
     */
    void disconnected(Channel channel) throws HolaException;

    /**
     * on message sent.
     * 
     * @param channel channel.
     * @param message message.
     */
    void sent(Channel channel, Object message) throws HolaException;

    /**
     * on message received.
     * 
     * @param channel channel.
     * @param message message.
     */
    void received(Channel channel, Object message) throws HolaException;

    /**
     * on exception caught.
     * 
     * @param channel channel.
     * @param exception exception.
     */
    void caught(Channel channel, Throwable exception) throws HolaException;

}
