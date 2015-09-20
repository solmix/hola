/*
 * Copyright 2015 The Solmix Project
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
package org.solmix.hola.rs.generic.codec;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.solmix.exchange.Message;
import org.solmix.hola.rs.generic.HolaRemoteServiceFactory;
import org.solmix.hola.transport.codec.Codec;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月18日
 */
@Extension(name=HolaRemoteServiceFactory.PROVIDER_ID)
public class HolaCodec implements Codec
{
    

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Codec#encode(io.netty.buffer.ByteBuf, org.solmix.exchange.Message)
     */
    @Override
    public void encode(ByteBuf buffer, Message outMsg) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Codec#decode(io.netty.buffer.ByteBuf)
     */
    @Override
    public Object decode(ByteBuf buffer) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
