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
package org.solmix.hola.rs.interceptor;

import static org.solmix.hola.rs.RemoteMessage.FLAG_EVENT;
import static org.solmix.hola.rs.RemoteMessage.FLAG_ONEWAY;
import static org.solmix.hola.rs.RemoteMessage.FLAG_REQUEST;
import static org.solmix.hola.rs.RemoteMessage.HEADER;
import static org.solmix.hola.rs.RemoteMessage.HEADER_LENGTH;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;

import org.solmix.commons.io.Bytes;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.data.ObjectOutput;
import org.solmix.exchange.data.Serialization;
import org.solmix.exchange.data.SerializationManager;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.exchange.model.SerializationInfo;
import org.solmix.hola.common.HOLA;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月24日
 */

public class SerializationOutInterceptor extends PhaseInterceptorSupport<Message>
{
    public static final String WRITE_INDEX_MARK="_writeIndexMark_";
  
    protected SerializationInfo info;
    private SerializationManager serializationManager;
    /**
     * @param phase
     */
    public SerializationOutInterceptor(SerializationInfo info)
    {
        super(Phase.PRE_ENCODE);
        this.info=info;
       
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        OutputStream out=  message.getContent(OutputStream.class);
        Serialization serializtion;
        if(isRequest(message)){
            serializtion=serializationManager.getSerializationByName(info.getSerialization());
            message.put(SerializationManager.SERIALIZATION_BYTE, serializtion.getContentTypeId());
            ByteBuf bb=   message.getContent(ByteBuf.class);
            encodeRequest(bb,message,serializtion);
        }else{
            Byte sbyte = (Byte)message.get(SerializationManager.SERIALIZATION_BYTE);
            serializtion=  serializationManager.getSerializationById(sbyte);
        }
        
        try {
            ObjectOutput oo=serializtion.createObjectOutput(info, out);
            if(oo!=null){
                message.setContent(ObjectOutput.class, oo);
            }
        } catch (IOException e) {
            throw new Fault(e);
        }
        message.getInterceptorChain().add(new SerializationOutEndingInterceptor(info));
    }

    /**
     * @param message
     */
    protected void encodeRequest(ByteBuf bf,Message message, Serialization serializtion) {
       
        int writeIndexMark = bf.writerIndex();
        bf.writerIndex(writeIndexMark + HEADER_LENGTH);
        message.put(WRITE_INDEX_MARK, writeIndexMark);
       
    }

    public void setSerializationManager(SerializationManager sm) {
      this.serializationManager=sm;
    }

    public static class SerializationOutEndingInterceptor extends PhaseInterceptorSupport<Message>
    {

        private SerializationInfo info;

        public SerializationOutEndingInterceptor(SerializationInfo info)
        {
            super(Phase.POST_ENCODE);
            this.info = info;
        }

        @Override
        public void handleMessage(Message message) throws Fault {
            try {
                ObjectOutput objectOutput = message.getContent(ObjectOutput.class);
                if (objectOutput != null) {
                    objectOutput.flushBuffer();
                }

                int writeIndexMark = (Integer) message.get(WRITE_INDEX_MARK);
                ByteBuf buffer = message.getContent(ByteBuf.class);
                Byte sbyte = (Byte) message.get(SerializationManager.SERIALIZATION_BYTE);
                // header
                byte[] header = new byte[HEADER_LENGTH];
                // set magic number.
                Bytes.short2bytes(HEADER, header);
                // serialization id
                header[2] = (byte) (FLAG_REQUEST | sbyte);
                if (MessageUtils.getBoolean(message, Message.EVENT_MESSAGE)) {
                    header[2] |= FLAG_EVENT;
                }
                if (MessageUtils.getBoolean(message, Message.ONEWAY)) {
                    header[2] |= FLAG_ONEWAY;
                }
                Bytes.long2bytes(message.getId(), header, 4);
                int length = buffer.writerIndex() - HEADER_LENGTH - writeIndexMark;
                Integer limit = info.getPalyload();
                if (limit == null) {
                    limit = HOLA.DEFAULT_PALYLOAD;
                }
                if (limit != null && length > limit) {
                    throw new Fault("Data length too large: " + length + ", limit: " + limit);
                }
                Bytes.int2bytes(length, header, 12);
                buffer.writerIndex(writeIndexMark);
                buffer.writeBytes(header); // write header.
                buffer.writerIndex(writeIndexMark + HEADER_LENGTH + length);
            } catch (IOException e) {
                throw new Fault(e);
            }
        }
    }
}
