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

import static org.solmix.exchange.MessageUtils.getString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.io.Bytes;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.FaultType;
import org.solmix.exchange.model.MessageInfo;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.generic.HolaRemoteServiceFactory;
import org.solmix.hola.serial.ObjectInput;
import org.solmix.hola.serial.ObjectOutput;
import org.solmix.hola.serial.Serialization;
import org.solmix.hola.transport.codec.RemoteCodec;
import org.solmix.runtime.Extension;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月18日
 */
@Extension(name=HolaRemoteServiceFactory.PROVIDER_ID)
public class HolaCodec extends RemoteCodec
{
    private static final Logger LOG = LoggerFactory.getLogger(HolaCodec.class);
    public static final  byte RESPONSE_NO_NULL=2;
    
    public static final  byte RESPONSE_NULL=1;
    @Override
    protected Object decodeBody(ByteBufInputStream is, byte[] header, Message inMsg) throws IOException {
        byte flag = header[2];
        byte ser = (byte) (flag & SERIALIZATION_MASK);
        Serialization serial = serializationManager.getSerializationById(ser);
        inMsg.put(Serialization.class, serial);
       
        long id = Bytes.bytes2long(header, 4);
        inMsg.setId(id);
        boolean isEvent = (flag & FLAG_EVENT) != 0;
        ObjectInput input =null;
        if ((flag & FLAG_REQUEST) == 0) {
            if (isEvent) {
                inMsg.put(Message.EVENT_MESSAGE, true);
            }
            byte status = header[3];
            if (status == OK) {
                try {
                    if (isEvent) {
                        input = serial.createObjectInput(serialConfiguration, is);
                        Object   data = decodeEventBody(input);
                        inMsg.setContent(Object.class, data);
                    } else {
                        if(serialConfiguration.isDecodeInIo()){
                            input = serial.createObjectInput(serialConfiguration, is);
                            DecodeableMessage.decodeResponse(inMsg,input,getExchange(id));
                        }else{
                            input = serial.createObjectInput(serialConfiguration, new ByteArrayInputStream(readByte(is)));
                            inMsg= new DecodeableMessage(inMsg,input ,getExchange(id));
                        }
                    }
                } catch (Throwable t) {
                    if(LOG.isWarnEnabled()){
                        LOG.warn("decode response failed",t);
                    }
                    inMsg.put(FaultType.class, FaultType.RUNTIME_FAULT);
                    inMsg.setContent(Exception.class, t);
                    inMsg.setExchange(getExchange(id));
                }
            } else {
                input = serial.createObjectInput(serialConfiguration, is);
                String errorString = input.readUTF();
                Fault fault = new Fault("ServerException");
                fault.setDetail(errorString);
                FaultType type= formStatusByte(status);
                inMsg.setContent(Exception.class, fault);
                inMsg.put(FaultType.class,type);
                inMsg.setExchange(getExchange(id));
            }
        } else {// request
            inMsg.setRequest(true);
            boolean oneWay = (flag & FLAG_ONEWAY) != 0;
            inMsg.put(Message.ONEWAY, Boolean.valueOf(oneWay));
            inMsg.put(HOLA.HOLA_VERSION_KEY, HOLA.VERSION);
            inMsg.put(Message.ONEWAY, (flag & FLAG_ONEWAY) != 0);
            if (isEvent) {
                inMsg.put(Message.EVENT_MESSAGE, true);
            }
            try {
                if (isEvent) {
                    input = serial.createObjectInput(serialConfiguration, is);
                    Object   data = decodeEventBody(input);
                    inMsg.setContent(Object.class, data);
                } else {
                    if(serialConfiguration.isDecodeInIo()){
                        input = serial.createObjectInput(serialConfiguration, is);
                        DecodeableMessage.decodeRequest(inMsg,input);
                    }else{
                        input = serial.createObjectInput(serialConfiguration, new ByteArrayInputStream(readByte(is)));
                        inMsg= new DecodeableMessage(inMsg,input);
                    }
                }
            } catch (Throwable t) {
                inMsg.put(FaultType.class, FaultType.RUNTIME_FAULT);
                inMsg.setContent(Exception.class, t);
            }
        }
        return inMsg;
        
    }
  
    private byte[] readByte(InputStream is) throws IOException {
        if (is.available() > 0) {
            byte[] result = new byte[is.available()];
            is.read(result);
            return result;
        }
        return new byte[]{};
    }
    @Override
    protected void encodeRequestBody(ByteBuf buffer, ObjectOutput out, Message outMsg) throws IOException {
        out.writeUTF(getString(outMsg, HOLA.HOLA_VERSION_KEY, HOLA.DEFAULT_HOLA_VERSION));
        out.writeUTF(getString(outMsg, Message.PATH_INFO));
        out.writeUTF(getString(outMsg, HOLA.VERSION));
        
        MessageInfo msi = outMsg.get(MessageInfo.class);
        out.writeUTF(msi.getOperationInfo().getName().toIdentityString());
        @SuppressWarnings("unchecked")
        List<Object> msgs= outMsg.getContent(List.class);
        if(msgs!=null){
            for(Object msg:msgs){
                out.writeObject(msg);
            }
        }else{
            out.writeObject(outMsg.getContent(Object.class));
        }
    }
    
    @Override
    protected void encodeResponseBody(ByteBuf buffer, ObjectOutput out, Message outMsg) throws IOException {
        Object obj = outMsg.getContent(Object.class);
        if (obj == null) {
            @SuppressWarnings("rawtypes")
            List objs = outMsg.getContent(List.class);
            if (objs.size() == 0) {
                obj = objs.get(0);
            }
        }
        if (obj != null) {
            out.writeByte(RESPONSE_NO_NULL);
            out.writeObject(obj);
        } else {
            out.writeByte(RESPONSE_NULL);
        }
    }
}
