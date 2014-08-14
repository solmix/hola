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
package org.solmix.hola.transport.codec;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.solmix.hola.transport.channel.Channel;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月14日
 */
@Extension(name="hola")
public interface Codec
{

    /**
     * @param channel
     * @param buffer
     * @param msg
     */
    void encode(Channel channel, ByteBuf buffer, Object msg)throws IOException;
    /**
     * @param channel
     * @param buffer
     * @return
     * @throws IOException
     */
    Object decode(Channel channel, ByteBuf buffer) throws IOException;
    
    enum DecodeResult {
        NEED_MORE_INPUT, SKIP_SOME_INPUT
    }
}