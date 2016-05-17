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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Protocol;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.FaultType;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.common.HolaRuntimeException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerHandler extends ChannelHandlerAdapter
{

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private final ChannelGroup allChannels;

    private final NettyServerChannelFactory channelFactory;

    private final Protocol protocol;

    private NettyConfiguration info;

    public NettyServerHandler(NettyServerChannelFactory factory, NettyConfiguration info, Protocol protocol)
    {
        allChannels = factory.getAllChannels();
        this.channelFactory = factory;
        this.info = info;
        this.protocol = protocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int accepts = info.getAccepts();
        if(accepts>0&&allChannels.size()>accepts){
            LOG.error("Close channel " + ctx.channel() + ", cause: The server " + ctx.channel().localAddress() + " connections greater than max config " + accepts);
            ctx.channel().close();
            return;
        }else{
            allChannels.add(ctx.channel());
            ctx.fireChannelActive();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        allChannels.remove(ctx.channel());
        ctx.fireChannelInactive();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            Message response = (Message) msg;
            try{
                if(MessageUtils.getBoolean(response, Message.EVENT_MESSAGE)){
                    handleEvent(ctx,response);
                }else{
                    handleMessage(ctx, response);
                }
            }catch(Throwable e){
                throw new NettyServerException(response.getId(), e);
            }
            
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void handleEvent(ChannelHandlerContext ctx, Message response) {
        Object content = response.getContent(Object.class);
        if(content==HeartbeatHandler.HEARTBEAT_EVENT){
            //返回心跳
            if(!MessageUtils.getBoolean(response, Message.ONEWAY)){
                Message msg = new DefaultMessage();
                msg.put(Message.EVENT_MESSAGE, Boolean.TRUE);
                msg.put(Message.ONEWAY, Boolean.TRUE);
                //新心跳
                msg.setRequest(true);
                msg.setInbound(false);
                msg.setContent(Object.class, HeartbeatHandler.HEARTBEAT_EVENT);
                ctx.writeAndFlush(msg);
            }else{
                if(LOG.isDebugEnabled()){
                    LOG.debug(new StringBuilder(32).append("Receive heartbeat response ").append(ctx.channel().toString()).toString());
                }
            }
        }
        
    }

    private void handleMessage(ChannelHandlerContext ctx, Message inMsg) throws IOException {
        String path = MessageUtils.getString(inMsg, Message.PATH_INFO);
        NettyMessageHandler handler = channelFactory.getNettyBuffedHandler(path);
        if(handler==null){
            throw new IllegalArgumentException("No Server found Handler for path:"+path);
        }
        Message outMsg = protocol.createMessage();
        outMsg.setRequest(false);
        outMsg.setInbound(false);
        outMsg.put(Message.ONEWAY, MessageUtils.getBoolean(inMsg, Message.ONEWAY));
        ThreadLocalChannel.set(ctx.channel());
        try {
            handler.handle(inMsg, outMsg);
        } finally {
            ThreadLocalChannel.unset();
        }
        boolean isOneWay = outMsg.getExchange().isOneWay() && MessageUtils.getBoolean(outMsg, Message.ONEWAY);
        if (!isOneWay) {
            handleResponse(ctx, outMsg);
        }

    }

    private void handleResponse(ChannelHandlerContext ctx, Message response) {
        boolean success = true;
        try {
            ChannelFuture future = ctx.writeAndFlush(response);
            if (info.isWaiteSuccess()) {
                success = future.await(info.getWriteTimeout());
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new Fault("Failed to write response to " + ctx.channel().remoteAddress());
        }
        if (!success) {
            throw new Fault("Failed to write response to " + ctx.channel().remoteAddress() + " time out (" + info.getWriteTimeout() + "ms) limit ");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Netty exception on server handler", cause);
        }
        ThreadLocalChannel.unset();
        Channel ch = ctx.channel();
        if (cause instanceof IllegalArgumentException) {
            ch.close();
        } else {
            if (ch.isActive()&&cause instanceof NettyServerException) {
                sendError(ctx, (NettyServerException)cause);
            }
        }
        ctx.close();
    }

    protected void sendError(ChannelHandlerContext ctx, NettyServerException cause) {
        Message msg = protocol.createMessage();
        msg.setId(cause.getId());
        msg.put(FaultType.class, FaultType.RUNTIME_FAULT);
        msg.setContent(Exception.class,cause.getCause());
        ctx.writeAndFlush(msg);

    }
    //标记服务端出现的异常，并将异常返回，避免客户端等待直到超时。
    private class NettyServerException extends HolaRuntimeException
    {
        private long id;
      
        private static final long serialVersionUID = -1347804624802182657L;

         NettyServerException(long id,Throwable e){
            super(e);
            this.id=id;
        }
        
        public long getId(){
            return id;
        }
    }
}
