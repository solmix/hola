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
package org.solmix.hola.rs.generic;

import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.solmix.commons.io.Bytes;
import org.solmix.commons.util.ClassDescUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.core.serialize.Serialization;
import org.solmix.hola.rs.RemoteRequest;
import org.solmix.hola.rs.support.RemoteRequestImpl;
import org.solmix.hola.rs.support.RemoteResponseImpl;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.codec.Codec;
import org.solmix.hola.transport.codec.ExchangeCodec;
import org.solmix.hola.transport.exchange.Request;
import org.solmix.hola.transport.exchange.Response;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年7月14日
 */
@Extension(name=HolaCodec.CODEC_NAME)
public class HolaCodec extends ExchangeCodec implements   Codec
{
    public static final String CODEC_NAME="hola";
    public static final byte RESPONSE_WITH_EXCEPTION = 0;

    public static final byte RESPONSE_VALUE = 1;

    public static final byte RESPONSE_NULL_VALUE = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    @Override
    protected Object decodeBody(Channel channel, ByteBufInputStream input,
        byte[] header) throws IOException {
        byte flag = header[2], proto = (byte) (flag & SERIALIZATION_MASK);
        Serialization ser= serializationManager.getSerialization(channel.getInfo(), proto);
        
        ObjectInput in = ser.deserialize(channel.getInfo(), input);
        long id = Bytes.bytes2long(header, 4);
        if ((flag & FLAG_REQUEST) == 0) {
            Response res = new Response(id);
            if ((flag & FLAG_EVENT) != 0) {
                res.setEvent(Response.HEARTBEAT_EVENT);
            }
            byte status = header[3];
            res.setStatus(status);
            if (status == Response.OK) {
                try {
                    Object data;
                    if (res.isHeartbeat()) {
                        data = decodeHeartbeatData(channel, in);
                    } else if (res.isEvent()) {
                        data = decodeEventData(channel, in);
                    } else {
                        
                        HolaRemoteResponse response = new HolaRemoteResponse(channel,res,in,getRequest(id));
                        response.decode();
                        data=response;
                    }
                    res.setResult(data);
                } catch (Throwable t) {
                    res.setStatus(Response.CLIENT_ERROR);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            }else {
                res.setErrorMessage(in.readUTF());
            }
            return res;
        }else{
            Request req = new Request(id);
            req.setVersion("0.1.1");
            req.setTwoWay((flag & FLAG_TWOWAY) != 0);
            if ((flag & FLAG_EVENT) != 0) {
                req.setEvent(Request.HEARTBEAT_EVENT);
            }
            try {
                Object data;
                if (req.isHeartbeat()) {
                    data = decodeHeartbeatData(channel, in);
                } else if (req.isEvent()) {
                    data = decodeEventData(channel, in);
                } else {
                    HolaRemoteRequest response = new HolaRemoteRequest(channel,req,in);
                    response.decode();
                    data=response;
                }
                req.setData(data);
            } catch (Throwable t) {
                // bad request
                req.setBroken(true);
                req.setData(t);
            }
            return req;
        }
    }
    protected RemoteRequest getRequest(long id) {
       Object o=getRequestData(id);
        return (RemoteRequest)o;
    }
    @Override
    protected void encodeRequestData(Channel channel, ObjectOutput out,
        Object data) throws IOException {
        RemoteRequestImpl req = (RemoteRequestImpl) data;
        out.writeUTF(req.getProperty(RemoteInfo.PATH));
        out.writeUTF(req.getProperty(RemoteInfo.VERSION));
        out.writeUTF(req.getMethod());
        out.writeUTF(ClassDescUtils.getTypeDesc(req.getParameterTypes()));
        
        Object[] params = req.getParameters();
        if(params!=null){
            for(Object param:params){
                //FIXME 处理callback回调函数.
                out.writeObject(param);
            }
        }
        out.writeObject(req.getProperties());
    }

    @Override
    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException {
        RemoteResponseImpl result = (RemoteResponseImpl) data;

        Throwable th = result.getException();
        if (th == null) {
            Object ret = result.getValue();
            if (ret == null) {
                out.writeByte(RESPONSE_NULL_VALUE);
            } else {
                out.writeByte(RESPONSE_VALUE);
                out.writeObject(ret);
            }
        } else {
            out.writeByte(RESPONSE_WITH_EXCEPTION);
            out.writeObject(th);
        }
    }
}
