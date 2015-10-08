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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Protocol;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.transport.AbstractRemoteTransporter;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerHandler extends ChannelHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private final ChannelGroup allChannels;

    private final NettyServerChannelFactory channelFactory;
    
    private final Protocol  protocol;
    
    private NettyConfiguration info;
    
    private ByteBuf buffer=Unpooled.EMPTY_BUFFER;

    public NettyServerHandler(NettyServerChannelFactory factory, NettyConfiguration info,Protocol protocol) {
        allChannels = factory.getAllChannels();
        this.channelFactory = factory;
        this.info = info;
        this.protocol=protocol;
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf input = (ByteBuf) msg;
        if(input.readableBytes()<=0){
            return;
        }
        ByteBuf message ;
        if(buffer.isReadable()){
            buffer.writeBytes(input);
            message=buffer;
        }else{
            message=input;
        }
        
        Message original = protocol.createMessage();
        ByteBufInputStream inStream = new ByteBufInputStream(message);
        original.setContent(InputStream.class, inStream);
        original.setContent(ByteBuf.class, message);
        int readerIndex;
        //根据协议初始化
        Message inMsg =null;
        try{
            do{
                readerIndex=buffer.readerIndex();
                try{
                    //根据协议初始化
                     inMsg = protocol.createMessage(original);
                }catch(Exception e){
                    buffer = Unpooled.EMPTY_BUFFER;
                    throw e;
                }
                //数据报文不够
                if(MessageUtils.getBoolean(inMsg, HOLA.NEED_MORE_DATA, false)){
                    buffer.readerIndex(readerIndex);
                    break;
                }else{
                    if (readerIndex == message.readerIndex()) {
                        buffer = Unpooled.EMPTY_BUFFER;
                        throw new IOException("Decode without read data.");
                    }
                    if(inMsg!=null){
                        handleMessage(ctx,inMsg);
                    }
                }
                
            }while(message.isReadable());
        }finally{
            if(message.isReadable()){
                buffer.discardReadBytes();
                buffer=message;
            }else{
                buffer = Unpooled.EMPTY_BUFFER;
            }
        }
    }
    
    private void handleMessage(ChannelHandlerContext ctx,Message inMsg) throws IOException{
        
        NettyMessageHandler handler = channelFactory.getNettyBuffedHandler(MessageUtils.getString(inMsg, Message.PATH_INFO));

        ByteBuf response = ctx.alloc().buffer(info.getBufferSize());
        
        Message outMsg = createResponseMessage(response);
        ThreadLocalChannel.set(ctx.channel());
        try {
            handler.handle(inMsg, outMsg);
        } finally {
            ThreadLocalChannel.unset();
        }
        handleResponse(ctx, response);
    }

    private Message createResponseMessage(ByteBuf response) {
        DefaultMessage msg = new DefaultMessage();
        msg.put(AbstractRemoteTransporter.RESPONSE_BYTEBUF, response);
        return msg;
    }

    private void handleResponse(ChannelHandlerContext ctx, ByteBuf response) {
        boolean success = true;
        try {
            ChannelFuture future = ctx.write(response);
            if (info.isWaiteSuccess()) {
                success = future.await(info.getWriteTimeout());
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
                + ctx.channel().remoteAddress() + " time out (" + info.getWriteTimeout()
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
            LOG.trace("Netty exception on netty handler",cause);
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
