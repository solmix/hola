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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.AbstractClient;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月19日
 */
public class NettyClient extends AbstractClient implements ChannelHandler
{

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    EventLoopGroup workerGroup;
   
    private Bootstrap bootstrap;

    private volatile Channel channel; 

    public NettyClient(final RemoteInfo info, final ChannelHandler handler,final Container container)
        throws TransportException
    {
            
        super(info,handler,container);
    }
    @Override
    protected   ChannelHandler wrapChannelHandler(RemoteInfo info,
        ChannelHandler handler) {
        return super.wrapChannelHandler(setThreadName(info, THREAD_POOL_NAME), handler);
    }
    @Override
    protected void doOpen() throws Throwable {
        workerGroup = new NioEventLoopGroup(getInfo().getIoThreads(
            HolaConstants.DEFAULT_IO_THREADS), new NamedThreadFactory(
            "NettyClient"));
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout());

        final NettyChannelHandler nettyChannelHandler = new NettyChannelHandler(
            getInfo(), this);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(),
                    getInfo(), NettyClient.this);
                ch.pipeline().addLast("decoder", adapter.getDecoder());
                ch.pipeline().addLast("encoder", adapter.getEncoder());
                ch.pipeline().addLast("handler", nettyChannelHandler);
            }
        });

    }

    @Override
    protected void doConnect() throws Throwable {
        long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(getConnectAddress()).sync();
        try {
            boolean ret = future.awaitUninterruptibly(getConnectTimeout(),
                TimeUnit.MILLISECONDS);

            if (ret && future.isSuccess()) {
                Channel newChannel = future.channel();
                try {
                    // 关闭旧的连接
                    Channel oldChannel = NettyClient.this.channel;
                    if (oldChannel != null) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close old netty channel "
                                    + oldChannel
                                    + " on create new netty channel "
                                    + newChannel);
                            }
                            oldChannel.close();
                        } finally {
                            NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (NettyClient.this.isClosed()) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close new netty channel "
                                    + newChannel
                                    + ", because the client closed.");
                            }
                            newChannel.close();
                        } finally {
                            NettyClient.this.channel = null;
                            NettyChannel.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        NettyClient.this.channel = newChannel;
                    }
                }
            } else if (future.cause() != null) {
                throw new TransportException(this, "failed to connect to server " + getRemoteAddress()
                    + ", error message is:" + future.cause().getMessage(),
                    future.cause());
            } else {
                throw new TransportException(this, "failed to connect to server " + getRemoteAddress()
                    + " client-side timeout " + getConnectTimeout()
                    + "ms (elapsed: " + (System.currentTimeMillis() - start)
                    + "ms) from netty client " + NetUtils.getLocalHost());
            }
        } finally {
            if (!isConnected()) {
                future.cancel(true);
            }
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            NettyChannel.removeChannelIfDisconnected(channel);
        } catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws Throwable {
        if(workerGroup!=null)
            workerGroup.shutdownGracefully();
    }

    @Override
    protected org.solmix.hola.transport.channel.Channel getChannel() {
        Channel c = channel;
        if (c == null || !c.isActive())
            return null;
        return NettyChannel.getOrAddChannel(c, getInfo(), this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.ChannelHandler#sent(org.solmix.hola.transport.channel.Channel, java.lang.Object)
     */
    @Override
    public void sent(org.solmix.hola.transport.channel.Channel channel,
        Object message) throws TransportException {
       if(isClosed())
           return;
       getChannelHandler().sent(channel, message);
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.ChannelHandler#received(org.solmix.hola.transport.channel.Channel, java.lang.Object)
     */
    @Override
    public void received(org.solmix.hola.transport.channel.Channel channel,
        Object message) throws TransportException {
        if(isClosed())
            return;
        getChannelHandler().received(channel, message);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.ChannelHandler#caught(org.solmix.hola.transport.channel.Channel, java.lang.Throwable)
     */
    @Override
    public void caught(org.solmix.hola.transport.channel.Channel channel,
        Throwable exception) throws TransportException {
        if(isClosed())
            return;
        getChannelHandler().caught(channel, exception);
    }

}