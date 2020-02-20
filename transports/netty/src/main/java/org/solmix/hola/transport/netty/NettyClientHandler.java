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
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.transport.RemoteProtocol;
import org.solmix.hola.transport.ResponseCallback;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */
@Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);
	private final Protocol protocol;
	private final NettyClientChannelFactory channelFactory;
	private NettyConfiguration info;

	public NettyClientHandler(RemoteProtocol protocol, NettyClientChannelFactory factory, NettyConfiguration info) {
		this.protocol = protocol;
		this.channelFactory = factory;
		this.info = info;
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
			Message inMsg = (Message) msg;
			if (MessageUtils.getBoolean(inMsg, Message.EVENT_MESSAGE)) {
				handleEvent(ctx, inMsg);
			} else {
				if (inMsg.isRequest()) {
					handleMessage(ctx,inMsg);
				} else {
					ResponseCallback callback = inMsg.getExchange().get(ResponseCallback.class);
					callback.process(inMsg);
				}
			}
		} else {
			super.channelRead(ctx, msg);
		}
	}

	private void handleMessage(ChannelHandlerContext ctx, Message inMsg) throws IOException {
		NettyTransporter transporter = channelFactory.getTransporter();
		if (transporter == null) {
			throw new IllegalArgumentException("No Server found duplex transporter");
		}
		Message outMsg = protocol.createMessage();
		outMsg.setRequest(false);
		outMsg.setInbound(false);
		outMsg.put(Message.ONEWAY, MessageUtils.getBoolean(inMsg, Message.ONEWAY));
		ThreadLocalChannel.set(ctx.channel());
		try {
			transporter.doService(inMsg, outMsg);
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
			throw new Fault("Failed to write response to " + ctx.channel().remoteAddress() + " time out ("
					+ info.getWriteTimeout() + "ms) limit ");
		}
	}

	private void handleEvent(ChannelHandlerContext ctx, Message response) {
		Object content = response.getContent(Object.class);
		if (content == HeartbeatHandler.HEARTBEAT_EVENT) {
			// 返回心跳
			if (!MessageUtils.getBoolean(response, Message.ONEWAY)) {
				Message msg = new DefaultMessage();
				msg.put(Message.EVENT_MESSAGE, Boolean.TRUE);
				msg.put(Message.ONEWAY, Boolean.TRUE);
				// 新心跳
				msg.setRequest(true);
				msg.setInbound(false);
				msg.setContent(Object.class, HeartbeatHandler.HEARTBEAT_EVENT);
				ctx.writeAndFlush(msg);
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(32).append("Receive heartbeat response ")
							.append(ctx.channel().toString()).toString());
				}
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (LOG.isWarnEnabled()) {
			LOG.warn("Netty exception on client handler,case :{}", cause.getMessage());
		}
		ctx.close();
	}

}
