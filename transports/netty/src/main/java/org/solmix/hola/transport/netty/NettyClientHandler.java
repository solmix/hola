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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.transport.ResponseCallback;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */
@Sharable
public class NettyClientHandler extends ChannelHandlerAdapter
{
    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    public NettyClientHandler(){
        
    }
    public NettyClientHandler(NettyConfiguration config)
    {
    }
    
  
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            Message response = (Message) msg;
            if(MessageUtils.getBoolean(response, Message.EVENT_MESSAGE)){
                handleEvent(ctx,response);
            }else{
                ResponseCallback callback = response.getExchange().get(ResponseCallback.class);
                callback.process(response);
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
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);

    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Netty exception on client handler,case :{}", cause.getMessage());
        }
        ctx.close();
    }

}
