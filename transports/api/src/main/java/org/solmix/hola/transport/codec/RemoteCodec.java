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
package org.solmix.hola.transport.codec;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.io.Bytes;
import org.solmix.commons.util.StringUtils;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.FaultType;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.serial.ObjectInput;
import org.solmix.hola.common.serial.ObjectOutput;
import org.solmix.hola.common.serial.Serialization;
import org.solmix.hola.transport.support.RemoteResponses;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年10月15日
 */

public class RemoteCodec extends TransportCodec
{

    public static final int HEADER_LENGTH = 16;

    public static final short HEADER = 0x101a;

    public static final byte HEADER_H = Bytes.short1(HEADER);

    public static final byte HEADER_L = Bytes.short0(HEADER);

    public static final byte FLAG_REQUEST = (byte) 0x80;

    public static final byte FLAG_ONEWAY = (byte) 0x40;

    public static final byte FLAG_EVENT = (byte) 0x20;

    public static final int SERIALIZATION_MASK = 0x1f;

    public static final byte OK = 10;

    public static final byte RUNTIME_FAULT = 20;

    public static final byte LOGICAL_RUNTIME_FAULT = 30;
    
    public static final byte UNCHECKED_APPLICATION_FAULT = 40;
    
    public static final byte CHECKED_APPLICATION_FAULT = 50;
    
    private static final Logger LOG = LoggerFactory.getLogger(RemoteCodec.class);
    @Override
    public void encode(ByteBuf buffer, Message outMsg) throws IOException {
        if(outMsg.isRequest()){
            encodeRequest(buffer,outMsg);
        }else{
            encodeResponse(buffer,outMsg);
        }
    }
    
    @Override
    public Object decode(ByteBuf buffer, Message inMsg) throws IOException {
        int readable = buffer.readableBytes();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        buffer.readBytes(header);
       return  decode(buffer, readable, header,inMsg);
    }

    public Object decode(ByteBuf buffer, int readable, byte[] header, Message inMsg) throws IOException {
        if (readable > 0 && header[0] != HEADER_H || readable > 1 && header[1] != HEADER_L) {
            int length = header.length;
            if (header.length < readable) {
                header = Bytes.copyOf(header, readable);
                buffer.readBytes(header, length, readable - length);
            }
            for (int i = 1; i < header.length - 1; i++) {
                if (header[i] == HEADER_H && header[i + 1] == HEADER_L) {
                    buffer.readerIndex(buffer.readerIndex() - header.length + i);
                    header = Bytes.copyOf(header, i);
                    break;
                }
            }
        }
        if (readable < HEADER_LENGTH) {
            return DecodeResult.NEED_MORE_INPUT;
        }
        int length = Bytes.bytes2int(header, 12);
        checkPayload(length);

        int total = length + HEADER_LENGTH;
        if (readable < total) {
            return DecodeResult.NEED_MORE_INPUT;
        }

        ByteBufInputStream is = new ByteBufInputStream(buffer);
        try {
            return decodeBody(is, header, inMsg);
        } finally {
            if (is.available() > 0) {
                try {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Skip input stream " + is.available());
                    }
                    is.skip(is.available());
                } catch (IOException e) {
                    LOG.warn(e.getMessage(), e);
                }
            }
        }
    }
 
    protected Object decodeBody(ByteBufInputStream is, byte[] header, Message inMsg) throws IOException {
        byte flag = header[2];
        byte ser = (byte) (flag & SERIALIZATION_MASK);
        Serialization serial = serializationManager.getSerializationById(ser);
        inMsg.put(Serialization.class, serial);
        ObjectInput input = serial.createObjectInput(serialConfiguration, is);
        long id = Bytes.bytes2long(header, 4);
        inMsg.setId(id);
        boolean isEvent = (flag & FLAG_EVENT) != 0;
        //response
        if ((flag & FLAG_REQUEST) == 0) {
            if (isEvent) {
                inMsg.put(Message.EVENT_MESSAGE, true);
            }
            byte status = header[3];
            if (status == OK) {
                Object data;
                try {
                    if (isEvent) {
                        data = decodeEventBody(input);
                    } else {
                        data = decodeResponseBody(input);
                    }
                    inMsg.setContent(Object.class, data);
                } catch (Throwable t) {
                    inMsg.put(FaultType.class, FaultType.RUNTIME_FAULT);
                    inMsg.setContent(Exception.class, t);
                }
            } else {
                String errorString = input.readUTF();
                Fault fault = new Fault("ServerException");
                fault.setDetail(errorString);
                inMsg.setContent(Exception.class, fault);
                inMsg.put(FaultType.class, formStatusByte(status));
            }
        } else {// request
            inMsg.setRequest(true);
            inMsg.put(HOLA.HOLA_VERSION_KEY, HOLA.VERSION);
            inMsg.put(Message.ONEWAY, (flag & FLAG_ONEWAY) != 0);
            if (isEvent) {
                inMsg.put(Message.EVENT_MESSAGE, true);
            }
            try {
                Object data;
                if (isEvent) {
                    data = decodeEventBody(input);
                } else {
                    data = decodeRequestBody(input);
                }
                inMsg.setContent(Object.class, data);
            } catch (Throwable t) {
                inMsg.put(FaultType.class, FaultType.RUNTIME_FAULT);
                inMsg.setContent(Exception.class, t);
            }
        }
        return inMsg;
    }

   
    protected Object decodeResponseBody(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeRequestBody(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeEventBody(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected void encodeRequest(ByteBuf buffer, Message outMsg) throws IOException {
        Serialization serial = getSerialization(outMsg);
        Byte sbyte =serial.getContentTypeId();
        //   header
        byte[] header = new byte[HEADER_LENGTH];
        // set magic number.
        Bytes.short2bytes(HEADER, header);
        header[2] = (byte) (FLAG_REQUEST | sbyte);
        boolean isEvent=MessageUtils.getBoolean(outMsg, Message.EVENT_MESSAGE);
        if (isEvent) {
            header[2] |= FLAG_EVENT;
        }
        if (MessageUtils.getBoolean(outMsg, Message.ONEWAY) || (outMsg.getExchange() != null && outMsg.getExchange().isOneWay())) {
            header[2] |= FLAG_ONEWAY;
        }
        Bytes.long2bytes(outMsg.getId(), header, 4);
        int writeIndexMark = buffer.writerIndex();
        buffer.writerIndex(writeIndexMark + HEADER_LENGTH);
        ByteBufOutputStream output = new ByteBufOutputStream(buffer);
        ObjectOutput objectOut = serial.createObjectOutput(serialConfiguration, output);
        if(isEvent){
            encodeEventBody(buffer,objectOut,outMsg);
        }else{
            encodeRequestBody(buffer,objectOut,outMsg);
        }
        objectOut.flushBuffer();
        output.flush();
        output.close();
        int length = buffer.writerIndex() - HEADER_LENGTH - writeIndexMark;
        checkPayload(length);
        Bytes.int2bytes(length, header, 12);
        buffer.writerIndex(writeIndexMark);
        buffer.writeBytes(header); // write header.
        buffer.writerIndex(writeIndexMark + HEADER_LENGTH + length);
    }
    
    

    protected void encodeResponse(ByteBuf buffer, Message outMsg) throws IOException {
        try {
            // response 的message已经通过exchange设定Serialization
            Serialization serial=null;
            if(outMsg.getExchange()!=null){
                serial=outMsg.getExchange().get(Serialization.class);
            }
            if(serial==null){
                serial= getSerialization(outMsg);
            }
            Byte sbyte = serial.getContentTypeId();
            // header
            byte[] header = new byte[HEADER_LENGTH];
            // set magic number.
            Bytes.short2bytes(HEADER, header);
            header[2] = sbyte;
            boolean isEvent = MessageUtils.getBoolean(outMsg, Message.EVENT_MESSAGE);
            if (isEvent) {
                header[2] |= FLAG_EVENT;
            }
            
            FaultType fault  =outMsg.get(FaultType.class);
            byte status =OK;
            if(fault!=null){
                status=formFaultType(fault);
            }
            header[3] = status;
            Bytes.long2bytes(outMsg.getId(), header, 4);

            int writeIndexMark = buffer.writerIndex();
            buffer.writerIndex(writeIndexMark + HEADER_LENGTH);
            ByteBufOutputStream output = new ByteBufOutputStream(buffer);
            ObjectOutput objectOut = serial.createObjectOutput(serialConfiguration, output);
            if(status==OK){
                if (isEvent) {
                    encodeEventBody(buffer, objectOut, outMsg);
                } else {
                    encodeResponseBody(buffer, objectOut, outMsg);
                }
            }else{
                encodeResponseException(buffer,objectOut,outMsg);
            }
            
            objectOut.flushBuffer();
            output.flush();
            output.close();
            int length = buffer.writerIndex() - HEADER_LENGTH - writeIndexMark;
            checkPayload(length);

            Bytes.int2bytes(length, header, 12);
            buffer.writerIndex(writeIndexMark);
            buffer.writeBytes(header); // write header.
            buffer.writerIndex(writeIndexMark + HEADER_LENGTH + length);
        } catch (Throwable t) {
            t.printStackTrace();
            //TODO codec抛错处理办法
        }

    }
 
    
    protected void encodeResponseBody(ByteBuf buffer, ObjectOutput objectOut, Message outMsg) throws IOException {
        Object data = outMsg.getContent(Object.class);
        objectOut.writeObject(data);

    }

    protected void encodeResponseException(ByteBuf buffer, ObjectOutput objectOut, Message outMsg) throws IOException {
        Exception cause = outMsg.getContent(Exception.class);
        if(cause!=null){
            objectOut.writeUTF(StringUtils.toString(cause));
        }
    }
    /**
     * @throws IOException 
     */
    protected void encodeRequestBody(ByteBuf buffer, ObjectOutput objectOut, Message outMsg) throws IOException {
        Object data = outMsg.getContent(Object.class);
        objectOut.writeObject(data);
    }

    /**
     * 编码事项
     * @throws IOException 
     */
    protected void encodeEventBody(ByteBuf buffer, ObjectOutput objectOut, Message outMsg) throws IOException {
        //只支持getContent(Object.class)，不支持getContent(List.class)
        Object data = outMsg.getContent(Object.class);
        objectOut.writeObject(data);
    }
    
    protected static byte formFaultType(FaultType type){
        switch (type) {
            case CHECKED_APPLICATION_FAULT:
               return CHECKED_APPLICATION_FAULT;
            case LOGICAL_RUNTIME_FAULT:
                return LOGICAL_RUNTIME_FAULT;
            case RUNTIME_FAULT:
                return RUNTIME_FAULT;
            case UNCHECKED_APPLICATION_FAULT:
                return UNCHECKED_APPLICATION_FAULT;
            default:
                return OK;

        }
    }
    
    protected static FaultType formStatusByte(byte b){
        switch(b){
            case CHECKED_APPLICATION_FAULT:
                return FaultType.CHECKED_APPLICATION_FAULT;
             case LOGICAL_RUNTIME_FAULT:
                 return FaultType.LOGICAL_RUNTIME_FAULT;
             case RUNTIME_FAULT:
                 return FaultType.RUNTIME_FAULT;
             case UNCHECKED_APPLICATION_FAULT:
                 return FaultType.UNCHECKED_APPLICATION_FAULT;
             default:
                 return null;
        }
    }
    
    /**根据id找到返回消息对应的exchange*/
    protected Exchange getExchange(long id) {
        Message msg =RemoteResponses.getOutMessage(id);
        if(msg!=null){
            return msg.getExchange();
        }
        return null;
    }

}
