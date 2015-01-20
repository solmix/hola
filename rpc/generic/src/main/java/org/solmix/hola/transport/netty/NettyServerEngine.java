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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.solmix.hola.transport.TransportServerInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerEngine implements NettyEngine {


    private NettyBuffedHandler handler;

    private volatile Channel channel;

    EventLoopGroup bossGroup;

    EventLoopGroup workerGroup;

    private NettyServerChannelFactory channelFactory;

    private TransportServerInfo info;

    private final String host;

    private final int port;

    public NettyServerEngine(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getServerKey() {
        String host = null;
        if (info.getHost() == null) {
            host = "localhost";
        }
        return new StringBuilder().append(host).append(":").append(
            info.getPort()).toString();
    }

    protected Channel startChannel() {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup).channel(
            NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR,
            true);
        channelFactory = new NettyServerChannelFactory(info, handler);
        bootstrap.childHandler(channelFactory);
        InetSocketAddress address = null;
        if (info.getHost() == null) {
            address = new InetSocketAddress(port);
        } else {
            address = new InetSocketAddress(host, port);
        }
        try {
            return bootstrap.bind(address).sync().channel();
        } catch (InterruptedException ex) {
            return null;
        }
    }

    public TransportServerInfo getTransportServerInfo() {
        return info;
    }

    public void setTransportServerInfo(TransportServerInfo transportServerInfo) {
        this.info = transportServerInfo;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.netty.NettyEngine#start()
     */
    @Override
    public void start() {
        assert handler != null;
        if (channel == null) {
            channel = startChannel();
        }
    }

    public void setNettyBuffedHandler(NettyBuffedHandler handler) {
        this.handler = handler;
    }

    @Override
    public void shutdown() {
        handler = null;
        if (channelFactory != null) {
            channelFactory.shutdown();
        }
        if (channel != null) {
            channel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public void finalizeConfig() {

    }
}
