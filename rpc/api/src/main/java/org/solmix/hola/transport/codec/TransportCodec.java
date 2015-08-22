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

package org.solmix.hola.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.solmix.exchange.Message;
import org.solmix.runtime.io.AbstractWrappedOutputStream;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月25日
 */

public class TransportCodec implements Codec {

    @Override
    public void encode(ByteBuf buffer, Message outMsg) throws IOException {
        OutputStream os = outMsg.get(OutputStream.class);
        if (os instanceof AbstractWrappedOutputStream) {
            os = ((AbstractWrappedOutputStream) os).getWrappedStream();
        }
        if (os instanceof ByteBufOutputStream) {
            ByteBufOutputStream byteOut = (ByteBufOutputStream) os;
            ByteBuf bf = byteOut.buffer();
           encode(buffer,bf,outMsg);
        } else {
            throw new UnsupportedEncodingException(
                "Unsupported outputstream type:" + os.getClass().toString());
        }
    }

    protected void  encode(ByteBuf headerBuf,ByteBuf contentBuf,Message outMsg){

    }

    @Override
    public Object decode(ByteBuf buffer) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
