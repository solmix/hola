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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.solmix.commons.io.Bytes;
import org.solmix.commons.util.StringUtils;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageList;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.data.ObjectInput;
import org.solmix.exchange.data.Serialization;
import org.solmix.exchange.data.SerializationManager;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.model.SerializationInfo;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.common.HOLA;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月29日
 */

public class RemoteMessage extends DefaultMessage implements Decodeable
{
    public static final int HEADER_LENGTH = 16;

    public static final short HEADER = 0x101a;

    public static final byte HEADER_H = Bytes.short1(HEADER);

    public static final byte HEADER_L = Bytes.short0(HEADER);

    public static final byte FLAG_REQUEST = (byte) 0x80;

    public static final byte FLAG_ONEWAY = (byte) 0x40;

    public static final byte FLAG_EVENT = (byte) 0x20;

    public static final int SERIALIZATION_MASK = 0x1f;
    private static final long serialVersionUID = 4232439786149399628L;

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    private boolean decode;

    protected SerializationManager serializationManager;

    protected SerializationInfo info;
    

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
        decode(buffer, readable, header);
    }

    protected void decode(ByteBuf buffer, int readable, byte[] header) throws IOException {
        if (readable > 0 && header[0] != HEADER_H 
            || readable > 1 && header[1] != HEADER_L) {
            int length = header.length;
            if (header.length < readable) {
                header = Bytes.copyOf(header, readable);
                buffer.readBytes(header, length, readable - length);
            }
            for (int i = 1; i < header.length - 1; i ++) {
                if (header[i] == HEADER_H && header[i + 1] == HEADER_L) {
                    buffer.readerIndex(buffer.readerIndex() - header.length + i);
                    header = Bytes.copyOf(header, i);
                    break;
                }
            }
        }
        if (readable < HEADER_LENGTH) {
            put(HOLA.NEED_MORE_DATA, true);
            return;
        }
        int length = Bytes.bytes2int(header, 12);
        Integer limit = info.getPalyload();
        if (limit == null) {
            limit = HOLA.DEFAULT_PALYLOAD;
        }
        if (limit != null && length > limit) {
            throw new Fault("Data length too large: " + length + ", limit: " + limit);
        }
        int total = length + HEADER_LENGTH;
        if( readable < total ) {
            put(HOLA.NEED_MORE_DATA, true);
            return;
        }
        if(MessageUtils.getBoolean(this,HOLA.NEED_MORE_DATA)){
            put(HOLA.NEED_MORE_DATA, false);
        }
        InputStream inStream=    getContent(InputStream.class);
        if(inStream==null){
            inStream=new   ByteBufInputStream(buffer);
            setContent(InputStream.class, inStream);
        }
        decodeBody(inStream,header);
    }

    protected void decodeBody(InputStream inStream, byte[] header) throws IOException {
        byte flag = header[2], proto = (byte) (flag & SERIALIZATION_MASK);
        Serialization serialization = serializationManager.getSerializationById(proto);
        ObjectInput input = serialization.createObjectInput(info, inStream);

        long id = Bytes.bytes2long(header, 4);
        setId(id);
        // response
        if ((flag & FLAG_REQUEST) == 0) {
            // TODO
        } else {// request
            put(HOLA.HOLA_VERSION_KEY, HOLA.VERSION);
            put(Message.ONEWAY, (flag & FLAG_ONEWAY) != 0);
            if ((flag & FLAG_EVENT) != 0) {
                put(Message.EVENT_MESSAGE, true);
            }
            try {
                Object data = decodeRequestData(input);
                setContent(MessageList.class, new MessageList(data));
            } catch (Throwable e) {
                setContent(Exception.class, e);
            }
        }
    }

    protected Object decodeRequestData(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    @Override
    public boolean isDecoded() {
        return decode;
    }

    public void setSerializationManager(SerializationManager serializationManager) {
        this.serializationManager = serializationManager;
    }
  
    public void setSerializationInfo(SerializationInfo extension) {
        this.info=extension;
        
    }
}
