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

package org.solmix.hola.rs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.concurrent.atomic.AtomicLong;

import org.solmix.commons.io.Bytes;
import org.solmix.exchange.Message;
import org.solmix.exchange.data.SerializationManager;
import org.solmix.exchange.support.DefaultMessage;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月29日
 */

public class RemoteMessage extends DefaultMessage implements Decodeable
{
    public static final int HEADER_LENGTH = 16;

    public static final short HEADER = 0x1314;

    public static final byte HEADER_H = Bytes.short1(HEADER);

    public static final byte HEADER_L = Bytes.short0(HEADER);

    public static final byte FLAG_REQUEST = (byte) 0x80;

    public static final byte FLAG_ONEWAY = (byte) 0x40;

    public static final byte FLAG_EVENT = (byte) 0x20;

    public static final int SERIALIZATION_MASK = 0x1f;
    private static final long serialVersionUID = 4232439786149399628L;

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    private boolean decode;

    private SerializationManager serializationManager;

    public RemoteMessage()
    {
        super();
        setId(newId());
    }

    public RemoteMessage(Message m)
    {
        super(m);
    }

    private static long newId() {
        return INVOKE_ID.getAndIncrement();
    }

    @Override
    public void decode() throws Exception {
        decode = true;
        ByteBuf buffer = getContent(ByteBuf.class);
        int readable = buffer.readableBytes();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        System.out.println("@@"+ByteBufUtil.hexDump(buffer));
        buffer.readBytes(header);
    }

    @Override
    public boolean isDecoded() {
        return decode;
    }

    public void setSerializationManager(SerializationManager serializationManager) {
        this.serializationManager = serializationManager;

    }
}
