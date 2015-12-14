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

package org.solmix.hola.transport.netty;

import java.io.IOException;
import java.util.List;

import org.solmix.exchange.Message;
import org.solmix.exchange.Protocol;
import org.solmix.hola.transport.RemoteProtocol;
import org.solmix.hola.transport.codec.Codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月23日
 */

public class NettyCodecAdapter
{

    private final ChannelHandler encoder = new NettyEncoder();


    private final Codec codec;

    private final NettyConfiguration config;

    private final Protocol protocol;

    public NettyCodecAdapter(NettyConfiguration config, RemoteProtocol protocol)
    {
        this.codec = protocol.getCodec();
        this.config = config;
        this.protocol = protocol;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return new NettyDecoder();
    }
    @Sharable
    private class NettyEncoder extends MessageToByteEncoder<Message>
    {

        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            codec.encode(out, msg);
            ByteBuf content = msg.getContent(ByteBuf.class);
            if (content != null) {
                int contentLength = content.readableBytes();
                if (contentLength > 0) {
                        out.writeBytes(content);
                }
            }
        }

    }
    private class NettyDecoder extends ByteToMessageDecoder
    {


        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
            
            if (input.readableBytes() <= 0) {
                return;
            }
            ByteBuf message;
            message = input;
            Object inMsg = null;
            try {
                do {
                   int readerIndex= message.markReaderIndex().readerIndex();
                    try {
                        Message msg = protocol.createMessage();
                        inMsg = codec.decode(message, msg);
                    } catch (Exception e) {
                        throw e;
                    }
                    // 数据报文不够
                    if (inMsg == Codec.DecodeResult.NEED_MORE_INPUT) {
                        message.resetReaderIndex();
                        break;
                    } else {
                        if (readerIndex==message.readerIndex()) {
                            throw new IOException("Decode without read data.");
                        }
                        if (inMsg != null) {
                            out.add(inMsg);
                        }
                    }
                } while (message.isReadable());
            } finally {
                if (message.isReadable()) {
                    message.discardReadBytes();
                } 
            }
        }
    }
}
