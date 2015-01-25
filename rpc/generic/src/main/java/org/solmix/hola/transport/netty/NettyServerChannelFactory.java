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
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;

import org.solmix.hola.transport.TransportServerInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerChannelFactory extends ChannelInitializer<Channel> {

    private final EventExecutorGroup applicationExecutor;

    private final ChannelGroup allChannels = new DefaultChannelGroup( GlobalEventExecutor.INSTANCE);;

    private final Map<String,NettyMessageHandler> handlerMap;
    private final TransportServerInfo info;

    public NettyServerChannelFactory(TransportServerInfo info,
        Map<String,NettyMessageHandler> handlerMap) {
        applicationExecutor = new DefaultEventExecutorGroup(info.getThreadPoolSize());
        this.handlerMap = handlerMap;
        this.info=info;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(applicationExecutor, new NettyServerHandler(this,info.getWriteTimeout(),info.isWaiteSuccess()));

    }

    public NettyMessageHandler getNettyBuffedHandler(String path) {
        return handlerMap.get(path);
    }

    public ChannelGroup getAllChannels() {
        return allChannels;
    }

    public void shutdown() {
        allChannels.close();
    }
    
    public int getBufferSize(){
       return info.getBufferSize();
    }
    public ByteBuf getResponeBuffer(){
        return Unpooled.buffer(info.getBufferSize());
    }

}
