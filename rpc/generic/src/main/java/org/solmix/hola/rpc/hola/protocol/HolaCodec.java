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

package org.solmix.hola.rpc.hola.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.io.Bytes;
import org.solmix.hola.transport.codec.Codec;
import org.solmix.hola.transport.codec.TransportCodec;
import org.solmix.runtime.Container;
import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.MessageUtils;
import org.solmix.runtime.exchange.dataformat.ObjectInput;
import org.solmix.runtime.exchange.dataformat.Serialization;
import org.solmix.runtime.exchange.dataformat.SerializationManager;
import org.solmix.runtime.exchange.support.DefaultMessage;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月25日
 */

public class HolaCodec extends TransportCodec implements Codec {

    private static final Logger LOG = LoggerFactory.getLogger(HolaCodec.class);
    public static final String NAME = "exchange";

    private static final int HEADER_LENGTH = 16;

    protected static final short HEADER = 0x1311;

    protected static final byte HEADER_H = Bytes.short1(HEADER);

    protected static final byte HEADER_L = Bytes.short0(HEADER);

    protected static final byte FLAG_REQUEST = (byte) 0x80;

    protected static final byte FLAG_TWOWAY = (byte) 0x40;

    protected static final byte FLAG_EVENT = (byte) 0x20;

    protected static final int SERIALIZATION_MASK = 0x1f;
    
    public static class Response{
        
    public static final byte OK                = 20;

    /**
     * clien side timeout.
     */
    public static final byte CLIENT_TIMEOUT    = 30;

    /**
     * server side timeout.
     */
    public static final byte SERVER_TIMEOUT    = 31;

    /**
     * request format error.
     */
    public static final byte BAD_REQUEST       = 40;

    /**
     * response format error.
     */
    public static final byte BAD_RESPONSE      = 50;

    /**
     * service not found.
     */
    public static final byte SERVICE_NOT_FOUND = 60;

    /**
     * service error.
     */
    public static final byte SERVICE_ERROR     = 70;

    /**
     * internal server error.
     */
    public static final byte SERVER_ERROR      = 80;

    /**
     * internal server error.
     */
    public static final byte CLIENT_ERROR      = 90;

}
    
    private Container container;
    
    public Container getContainer() {
        return container;
    }

    @Resource
    public void setContainer(Container container) {
        this.container = container;
    }


    @Override
    protected void encode(ByteBuf headerBuf, ByteBuf contentBuf, Message outMsg) {
        // 设置header
        byte[] header = new byte[HEADER_LENGTH];
        // 前导符
        Bytes.short2bytes(HEADER, header);
        // 序列化
        byte serial = MessageUtils.getByte(outMsg, Message.SERIALIZATON_ID);
        // request
        if (MessageUtils.getBoolean(outMsg, Message.REQUEST_MESSAGE)) {
            header[2] = (byte) (FLAG_REQUEST | serial);
            if (!MessageUtils.getBoolean(outMsg, Message.ONEWAY)) {
                header[2] |= FLAG_TWOWAY;
            }
            if (!MessageUtils.getBoolean(outMsg, Message.EVENT_MESSAGE)) {
                header[2] |= FLAG_EVENT;
            }
        } else {
            header[2] = serial;
            if (!MessageUtils.getBoolean(outMsg, Message.EVENT_MESSAGE)) {
                header[2] |= FLAG_EVENT;
            }
            header[3] = MessageUtils.getByte(outMsg, Message.RESPONSE_CODE);
        }

        Bytes.long2bytes(outMsg.getId(), header, 4);
        int length = contentBuf.readableBytes();
        // checkPlayload(length)
        Bytes.int2bytes(length, header, 12);
        headerBuf.writeBytes(header);
    }

    
    @Override
    public Object decode(ByteBuf buffer) throws IOException {
        int readable = buffer.readableBytes();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        buffer.readBytes(header);
        return decode(buffer, readable, header);
    }

    protected Object decode(ByteBuf buffer, int readable, byte[] header) throws IOException {
        //检查前导符号
        if (readable > 0 && header[0] != HEADER_H 
            || readable > 1 && header[1] != HEADER_L) {
            return null;
        }
        if (readable < HEADER_LENGTH) {
            return DecodeResult.NEED_MORE_INPUT;
        }
        int len = Bytes.bytes2int(header, 12);
//        checkPayload(len);
        int tt = len + HEADER_LENGTH;
        if( readable < tt ) {
            return DecodeResult.NEED_MORE_INPUT;
        }
        return decodeMessage(buffer,len,header);
    }


    
    protected Object decodeMessage(ByteBuf buffer, int contentLength, byte[] header) throws IOException {
        Message inMsg = new DefaultMessage();
        inMsg.put(Message.INBOUND_MESSAGE, Boolean.TRUE);
        inMsg.put(Message.CONTENT_LENGTH, contentLength);
        inMsg.setContent(ByteBuf.class, buffer);
        byte flag = header[2], serial = (byte) (flag & SERIALIZATION_MASK);
        SerializationManager sm =container.getExtension(SerializationManager.class);
        Serialization serializaton= sm.getSerializationById(serial);
        long id = Bytes.bytes2long(header, 4);
        inMsg.setId(id);
        //client decode response return by server. 
        ByteBufInputStream input = new ByteBufInputStream(buffer, contentLength);
        if((flag & FLAG_REQUEST) == 0){
            inMsg.put(Message.REQUEST_MESSAGE, Boolean.FALSE);
            if ((flag & FLAG_EVENT) != 0) {
                inMsg.put(Message.EVENT_MESSAGE, Boolean.TRUE);
            }
            byte status = header[3];
            inMsg.put(Message.RESPONSE_CODE, new Byte(status));
            if(status==Response.OK){
                inMsg.setContent(InputStream.class, input);
            }else{
               inMsg.setContent(Exception.class,new Exception( deSerialization(serializaton,input).readUTF()));
            }
        }else{
            //server decode request from client
            inMsg.put(Message.REQUEST_MESSAGE, Boolean.TRUE);
            inMsg.put(Message.ONEWAY, (flag & FLAG_TWOWAY) != 1);
            if((flag & FLAG_EVENT) != 0){
                inMsg.put(Message.EVENT_MESSAGE, Boolean.TRUE);
            }
            try{
            ObjectInput oi = deSerialization(serializaton, input);
            //path用于分配handler,在其他内容之前解码.
            inMsg.put(Message.PATH_INFO, oi.readUTF());
            inMsg.setContent(InputStream.class, input);
            inMsg.setContent(ObjectInput.class, oi);
            } catch (Throwable t) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Decode request failed: " + t.getMessage(), t);
                }
                inMsg.setContent(Exception.class, t);
            }
        }
        return inMsg;
    }

   
    private ObjectInput deSerialization(Serialization sm, ByteBufInputStream input) throws IOException {
        return sm.createObjectInput(null, input);

    }

}
