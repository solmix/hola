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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.AbstractServer;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Server;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月15日
 */

public class NettyServer extends AbstractServer implements Server,
    org.solmix.hola.transport.channel.ChannelHandler
{

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Map<String, Channel> channels;

    private ServerBootstrap bootstrap;

    private io.netty.channel.Channel channel;
    EventLoopGroup bossGroup ;
    EventLoopGroup workerGroup;
    /**
     * @param endpointInfo
     * @param handler
     * @throws TransportException
     */
    public NettyServer(ChannelInfo info, ChannelHandler handler,final Container container)
        throws TransportException
    {
        super(info,handler,container);
    }
    @Override
    protected   ChannelHandler wrapChannelHandler(ChannelInfo info,
        ChannelHandler handler) {
        return super.wrapChannelHandler(setThreadName(info, SERVER_THREAD_POOL_NAME), handler);
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.AbstractServer#doOpen()
     */
    @Override
    protected void doOpen() throws Throwable {
         bossGroup = new NioEventLoopGroup(8,
                new NamedThreadFactory("NettyServerBoss", true));
         workerGroup = new NioEventLoopGroup(
                getInfo().getIoThreads( HolaConstants.DEFAULT_IO_THREADS), 
                new NamedThreadFactory("NettyServerWorker",true));
       
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(
            NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128).childOption(
            ChannelOption.SO_KEEPALIVE, true);

        final NettyChannelHandler nettyChannelHandler = new NettyChannelHandler(
            getInfo(), this);
        channels=nettyChannelHandler.getChannels();
        bootstrap.handler(new LoggingHandler(LogLevel.DEBUG))
                 .childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(),
                    getInfo(), NettyServer.this);
                ch.pipeline().addLast("decoder", adapter.getDecoder());
                ch.pipeline().addLast("encoder", adapter.getEncoder());
                ch.pipeline().addLast("handler", nettyChannelHandler);
            }
        });
        ChannelFuture f = bootstrap.bind(getBindAddress()).sync();
        // bind
        channel = f.channel();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.AbstractServer#doClose()
     */
    @Override
    protected void doClose() throws Throwable {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<org.solmix.hola.transport.channel.Channel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (org.solmix.hola.transport.channel.Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        if (bossGroup != null)
            bossGroup.shutdownGracefully();
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Server#getChannels()
     */
    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Server#getChannel(java.net.InetSocketAddress)
     */
    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Server#isActive()
     */
    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.ChannelHandler#sent(org.solmix.hola.transport.channel.Channel,
     *      java.lang.Object)
     */
    @Override
    public void sent(org.solmix.hola.transport.channel.Channel channel,
        Object message) throws TransportException {
        if (isClosed())
            return;
        getChannelHandler().sent(channel, message);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.ChannelHandler#received(org.solmix.hola.transport.channel.Channel,
     *      java.lang.Object)
     */
    @Override
    public void received(org.solmix.hola.transport.channel.Channel channel,
        Object message) throws TransportException {
        if (isClosed())
            return;
        getChannelHandler().received(channel, message);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.ChannelHandler#caught(org.solmix.hola.transport.channel.Channel,
     *      java.lang.Throwable)
     */
    @Override
    public void caught(org.solmix.hola.transport.channel.Channel channel,
        Throwable exception) throws TransportException {
        if (isClosed())
            return;
        getChannelHandler().caught(channel, exception);
    }

}
