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

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;

import org.solmix.exchange.Message;
import org.solmix.hola.transport.codec.Codec;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月23日
 */

public class NettyCodecAdapter {

    private final ChannelHandler encoder = new NettyEncoder();

    private final ChannelHandler decoder = new NettyDecoder();

    private final Codec codec;

    private final int bufferSize;

    public NettyCodecAdapter(Codec codec, int bufferSize) {
        this.codec = codec;
        this.bufferSize = bufferSize;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    @Sharable
    private class NettyEncoder extends MessageToMessageEncoder<Message> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg,
            List<Object> out) throws Exception {
            ByteBufAllocator alloc = ctx.alloc();
            ByteBuf buffer = alloc.buffer(bufferSize);
            codec.encode(buffer, msg);

            ByteBuf content = msg.getContent(ByteBuf.class);
            if (content != null) {
                int contentLength = content.readableBytes();
                if (contentLength > 0) {
                    if (buffer != null
                        && buffer.writableBytes() >= contentLength) {
                        buffer.writeBytes(content);
                        out.add(buffer);
                    } else {
                        if (buffer != null) {
                            out.add(buffer);
                        }
                        out.add(content.retain());
                    }
                } else {
                    if (buffer != null) {
                        out.add(buffer);
                    } else {
                        out.add(EMPTY_BUFFER);
                    }
                }
            }
        }

    }

    private class NettyDecoder extends ByteToMessageDecoder {

        private ByteBuf buffer;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {
            int readable = in.readableBytes();
            if (readable <= 0) {
                return;
            }
            if (buffer == null) {
                buffer = ctx.alloc().buffer();
            }
            if (buffer.isReadable()) {
                buffer.writeBytes(in);
            } else {
                buffer = in;
            }
            Object inMsg;
            int saveReaderIndex;
            try {
                // decode object.
                do {
                    saveReaderIndex = buffer.readerIndex();
                    try {
                        System.out.println("@@@>接受"
                            + ByteBufUtil.hexDump(buffer));
                        inMsg = codec.decode(buffer);
                    } catch (IOException e) {
                        buffer = buffer.clear();
                        throw e;
                    }
                    if (inMsg == Codec.DecodeResult.NEED_MORE_INPUT) {
                        buffer.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == buffer.readerIndex()) {
                            buffer.clear();
                            throw new IOException("Decode without read data.");
                        }
                        if (inMsg != null) {
                            out.add(inMsg);
                        }
                    }
                } while (buffer.isReadable());
            } finally {
                if (buffer.isReadable()) {
                    buffer.discardReadBytes();
                } else {
                    buffer.clear();
                }
            }
        }
    }
}
