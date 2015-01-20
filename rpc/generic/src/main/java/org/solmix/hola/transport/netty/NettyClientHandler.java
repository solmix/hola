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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.solmix.runtime.exchange.Message;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */
@Sharable
public class NettyClientHandler extends ChannelHandlerAdapter {

    private final BlockingQueue<Message> sendedQueue = new LinkedBlockingDeque<Message>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
        throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf response = (ByteBuf)msg;
            Message request = sendedQueue.poll();
            ResponseCallBack callback= request.get(ResponseCallBack.class);
            callback.responseReceived(response);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg,
        ChannelPromise promise) throws Exception {
        if (msg instanceof Message) {
            Message m = (Message) msg;

            ByteBuf bb = m.getContent(ByteBuf.class);
            if (bb == null) {
                OutputStream out = m.getContent(OutputStream.class);
                if (out instanceof ByteBufOutputStream) {
                    bb = ((ByteBufOutputStream) out).buffer();
                }
            }
            if (bb != null) {
                sendedQueue.put(m);
                ctx.writeAndFlush(bb);
            } else {
                throw new IOException(
                    "write a message without bytebuf out ByteBufOutputStream");
            }
        } else {
            super.write(ctx, msg, promise);
        }
    }
}
