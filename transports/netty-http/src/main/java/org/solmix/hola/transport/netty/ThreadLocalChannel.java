/**
 * Copyright (c) 2015 The Solmix Project
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

import io.netty.channel.Channel;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月19日
 */

public class ThreadLocalChannel {

    public static final ThreadLocal<Channel> CHANNEL_THREAD_LOCAL = new ThreadLocal<Channel>();

    private ThreadLocalChannel() {
        // Utils class
    }

    public static void set(Channel channel) {
        CHANNEL_THREAD_LOCAL.set(channel);
    }

    public static void unset() {
        CHANNEL_THREAD_LOCAL.remove();
    }

    public static Channel get() {
        return CHANNEL_THREAD_LOCAL.get();
    }
}
