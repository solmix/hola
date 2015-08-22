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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.transport.AbstractTCPTransporter;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.runtime.interceptor.Fault;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerHandler extends ChannelHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private final ChannelGroup allChannels;

    private final NettyServerChannelFactory channelFactory;

    private final int timeout;

    private final boolean waitSucess;

    public NettyServerHandler(NettyServerChannelFactory factory, int timeout,
        boolean waitSucess) {
        allChannels = factory.getAllChannels();
        this.channelFactory = factory;
        this.timeout = timeout;
        this.waitSucess = waitSucess;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("open new channel {}", ctx.channel());
        }
        allChannels.add(ctx.channel());
        //启动心跳定时器
        //TODO
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
        throws Exception {
        if (msg instanceof Message) {
            Message inMsg = (Message) msg;
            NettyMessageHandler handler = channelFactory.getNettyBuffedHandler(MessageUtils.getString(
                inMsg, Message.PATH_INFO));

            ByteBuf response = ctx.alloc().buffer(
                channelFactory.getBufferSize());
            Message outMsg = createResponseMessage(response);
            ThreadLocalChannel.set(ctx.channel());
            try {
                handler.handle(inMsg, outMsg);
            } finally {
                ThreadLocalChannel.unset();
            }
            handleResponse(ctx, response);

        } else {
            super.channelRead(ctx, msg);
        }
    }

    private Message createResponseMessage(ByteBuf response) {
        DefaultMessage msg = new DefaultMessage();
        msg.put(AbstractTCPTransporter.RESPONSE_BYTEBUF, response);
        return msg;
    }

    private void handleResponse(ChannelHandlerContext ctx, ByteBuf response) {
        boolean success = true;
        try {
            ChannelFuture future = ctx.write(response);
            if (waitSucess) {
                success = future.await(timeout);
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new Fault("Failed to write response to "
                + ctx.channel().remoteAddress());
        }
        if (success) {
            throw new Fault("Failed to write response to "
                + ctx.channel().remoteAddress() + " time out (" + timeout
                + "ms) limit ");
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Netty exception on netty handler");
        }
        ThreadLocalChannel.unset();
        Channel ch = ctx.channel();
        if (cause instanceof IllegalArgumentException) {
            ch.close();
        } else {
            if (ch.isActive()) {
                sendError(cause);
            }
        }
        ctx.close();
    }

    protected void sendError(Throwable cause) {
        // TODO Auto-generated method stub

    }
}
