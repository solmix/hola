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

import java.io.IOException;
import java.util.List;

import org.solmix.commons.util.StringUtils;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.serial.ObjectInput;
import org.solmix.hola.common.serial.ObjectOutput;
import org.solmix.hola.common.serial.SerialConfiguration;
import org.solmix.hola.common.serial.Serialization;
import org.solmix.hola.common.serial.SerializationManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月25日
 */

public class TransportCodec implements Codec
{

    protected SerializationManager serializationManager;

    protected SerialConfiguration serialConfiguration;

    @Override
    public void encode(ByteBuf buffer, Message outMsg) throws IOException {
        ByteBufOutputStream output = new ByteBufOutputStream(buffer);
        Serialization serialization = getSerialization(outMsg);
        ObjectOutput objectOut = serialization.createObjectOutput(serialConfiguration, output);
        encodeData(objectOut, outMsg);
        objectOut.flushBuffer();
    }

    protected void encodeData(ObjectOutput objectOut, Message outMsg) throws IOException {
        @SuppressWarnings("unchecked")
        List<Object> datas = outMsg.getContent(List.class);
        if (datas != null) {
            for (Object data : datas) {
                objectOut.writeObject(data);
            }
        } else {
            Object data = outMsg.getContent(Object.class);
            if (data != null) {
                objectOut.writeObject(data);
            }
        }

    }
    
    public Serialization getSerialization(Message msg){
        String seril=MessageUtils.getString(msg, Serialization.SERIALIZATION_ID);
        if(seril==null){
            seril=serialConfiguration.getSerial();
        }
        return serializationManager.getSerializationByName(seril);
    }
    public SerializationManager getSerializationManager() {
        return serializationManager;
    }
    public void setSerializationManager(SerializationManager serializationManager) {
        this.serializationManager = serializationManager;
    }
    public SerialConfiguration getSerialConfiguration() {
        return serialConfiguration;
    }
    public void setSerialConfiguration(SerialConfiguration serialConfiguration) {
        this.serialConfiguration = serialConfiguration;
    }

    @Override
    public Object decode(ByteBuf buffer, Message inMsg) throws IOException {
        ByteBufInputStream input = new ByteBufInputStream(buffer);
        Serialization serialization = getSerialization(inMsg);
        ObjectInput objectInput =serialization.createObjectInput(serialConfiguration, input);
        Object obj   =decodeData(objectInput);
        if(obj!=null){
            inMsg.setContent(Object.class, obj);
            return obj;
        }
        return null;
    }
    
    protected Object decodeData(ObjectInput objectInput) throws IOException {
        try {
            return objectInput.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
        }
    }

    protected void checkPayload(long length) throws IOException{
        Integer limit = getLimit();
        if (limit != null && length > limit) {
            throw new IOException("Data length too large: " + length + ", limit: " + limit);
        }
    }
    
    protected int getLimit(){
        Integer limit = serialConfiguration.getPalyload();
        if (limit == null) {
            limit = HOLA.DEFAULT_PALYLOAD;
        }
        return limit;
    }
}
