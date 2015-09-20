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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.solmix.exchange.interceptor.Fault;
import org.solmix.hola.transport.model.TransportServerInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerEngine implements NettyEngine {

    private volatile Channel channel;

    EventLoopGroup bossGroup;

    EventLoopGroup workerGroup;

    private NettyServerChannelFactory channelFactory;

    private TransportServerInfo info;

    private final String host;

    private final int port;

    private final List<String> registedPaths = new CopyOnWriteArrayList<String>();

    private final Map<String, NettyMessageHandler> handlerMap = new ConcurrentHashMap<String, NettyMessageHandler>();

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

   
    @Override
    public void addHandler(String path, NettyMessageHandler handler) {
        checkRegistedPath(path);
        if (channel == null) {
            channel = startChannel();
        }
        handlerMap.put(path, handler);
        registedPaths.add(path);
    }

    @Override
    public void removeHandler(String path) {
        handlerMap.remove(path);
        registedPaths.remove(path);
    }

  
    @Override
    public NettyMessageHandler getHandler(String path) {
        return handlerMap.get(path);
    }

    protected void checkRegistedPath(String path) {
        for (String registedPath : registedPaths) {
            if (path.equals(registedPath)) {
                throw new Fault("path already in use");
            }
        }
    }

    protected Channel startChannel() {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup( 0/*,new NamedThreadFactory("Netty-Server-Boss", true)*/);
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup).channel(
            NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR,
            true);
        channelFactory = new NettyServerChannelFactory(info, handlerMap);
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

    /**
     * Server配置信息
     * 
     * @param transportServerInfo
     */
    public void setTransportServerInfo(TransportServerInfo transportServerInfo) {
        this.info = transportServerInfo;
    }


    @Override
    public void shutdown() {
        handlerMap.clear();
        registedPaths.clear();
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
