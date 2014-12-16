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
package org.solmix.hola.transport.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.commons.util.NetUtils;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;

/**
 * 将Netty {@link io.netty.channel.ChannelHandler channelHandler} 转换为内部
 * {@link org.solmix.hola.transport.channel.ChannelHandler ChannelHandler}
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月10日
 */
@Sharable
public class NettyChannelHandler extends ChannelHandlerAdapter {

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); // <ip:port, channel>
    
    private final RemoteInfo param;
    
    private final ChannelHandler handler;
    
    public NettyChannelHandler(RemoteInfo param, org.solmix.hola.transport.channel.ChannelHandler handler){
        if (param == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.param = param;
        this.handler = handler;
    }

    public Map<String, Channel> getChannels() {
        return channels;
        
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       super.channelActive(ctx);
       NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), param, handler);
       try {
           if (channel != null) {
               channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
           }
           handler.connected(channel);
       } finally {
           NettyChannel.removeChannelIfDisconnected(ctx.channel());
       }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), param, handler);
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            }
            handler.disconnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       super.channelRead(ctx, msg);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), param, handler);
        try {
            handler.received(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }
    
    @Override
    public void write(ChannelHandlerContext ctx, final Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
       final NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), param, handler);
        try {
            /*  EventExecutor executor= ctx.executor();
            EventExecutorGroup group=   executor.parent();
            group.execute(new Runnable() {
                
                @Override
                public void run() {
                    try {*/
                        handler.sent(channel, msg);
                   /* } catch (TransportException e) {
                        e.printStackTrace();
                    }
                }
            });*/
           
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }
   
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), param, handler);
        try {
            handler.caught(channel, cause);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

}