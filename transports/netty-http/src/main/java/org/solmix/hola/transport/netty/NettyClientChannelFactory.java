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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

import org.solmix.hola.transport.codec.Codec;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月15日
 */

public class NettyClientChannelFactory extends ChannelInitializer<Channel> {

    
   private final NettyCodecAdapter codecAdapter;
   
    public NettyClientChannelFactory(Codec codec,int bufferSize) {
        codecAdapter = new NettyCodecAdapter(codec, bufferSize);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", codecAdapter.getDecoder());
        pipeline.addLast("encoder", codecAdapter.getEncoder());
        pipeline.addLast(new NettyClientHandler());
    }

}
