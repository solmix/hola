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
package org.solmix.hola.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Resource;

import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.core.serialize.Serialization;
import org.solmix.hola.core.serialize.SerializationManager;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月11日
 */
@Extension(name="serialize")
public class SerializeCodec implements Codec
{
    @Resource
    protected SerializationManager serializationManager;
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Codec#encode(org.solmix.hola.transport.channel.Channel, io.netty.buffer.ByteBuf, java.lang.Object)
     */
    @Override
    public void encode(Channel channel, ByteBuf buffer, Object msg)
        throws IOException {
      ByteBufOutputStream output= new ByteBufOutputStream(buffer);
      ObjectOutput objectOutput= getSerialization(channel.getInfo()).serialize(channel.getInfo(), output);
      encodeData(channel, objectOutput, msg);
      objectOutput.flush();
    }

    /**
     * @param channel
     * @param objectOutput
     * @param msg
     * @throws IOException 
     */
    private void encodeData(Channel channel, ObjectOutput objectOutput,
        Object msg) throws IOException {
        encodeData(objectOutput, msg);
    }
    protected void encodeData(ObjectOutput output, Object message) throws IOException {
        output.writeObject(message);
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Codec#decode(org.solmix.hola.transport.channel.Channel, io.netty.buffer.ByteBuf)
     */
    @Override
    public Object decode(Channel channel, ByteBuf buffer) throws IOException {
        ByteBufInputStream input= new ByteBufInputStream(buffer);
        ObjectInput objectinput= getSerialization(channel.getInfo()).deserialize(channel.getInfo(), input);
        return decodeData(channel, objectinput);
    }
    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
        return decodeData(input);
    }

    protected Object decodeData(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + e.getMessage());
        }
    }
     Serialization getSerialization(ChannelInfo info){
        return serializationManager.getSerialization(info);
    }
}
