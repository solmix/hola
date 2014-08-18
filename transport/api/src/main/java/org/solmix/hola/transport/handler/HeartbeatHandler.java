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
package org.solmix.hola.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.exchange.Request;
import org.solmix.hola.transport.exchange.Response;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月17日
 */

public class HeartbeatHandler extends AbstractChannelHandlerDelegate
{
    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatHandler.class);
    
    public static String KEY_READ_TIMESTAMP = "_read_ts";

    public static String KEY_WRITE_TIMESTAMP = "_write_ts";
    /**
     * @param handler
     */
    public HeartbeatHandler(ChannelHandler handler)
    {
        super(handler);
    }
    @Override
    public void connected(Channel channel) throws TransportException {
        setReadTimestamp(channel);
        setWriteTimestamp(channel);
        handler.connected(channel);
    }

    /**
     * @param channel
     */
    private void setWriteTimestamp(Channel channel) {
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
    }
    /**
     * @param channel
     */
    private void setReadTimestamp(Channel channel) {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
    }
    @Override
    public void disconnected(Channel channel) throws TransportException {
        clearReadTimestamp(channel);
        clearWriteTimestamp(channel);
        handler.disconnected(channel);
    }

    /**
     * @param channel
     */
    private void clearWriteTimestamp(Channel channel) {
        channel.removeAttribute(KEY_WRITE_TIMESTAMP);
    }
    /**
     * @param channel
     */
    private void clearReadTimestamp(Channel channel) {
        channel.removeAttribute(KEY_READ_TIMESTAMP);
    }
    @Override
    public void sent(Channel channel, Object message) throws TransportException {
        setWriteTimestamp(channel);
        handler.sent(channel, message);
    }
    private boolean isHeartbeatRequest(Object message) {
        return message instanceof Request && ((Request) message).isHeartbeat();
    }

    private boolean isHeartbeatResponse(Object message) {
        return message instanceof Response && ((Response)message).isHeartbeat();
    }
    @Override
    public void received(Channel channel, Object message) throws TransportException {
        setReadTimestamp(channel);
        if (isHeartbeatRequest(message)) {
            Request req = (Request) message;
            if (req.isTwoWay()) {
                Response res = new Response(req.getId(), req.getVersion());
                res.setEvent(Response.HEARTBEAT_EVENT);
                channel.send(res);
                if (LOG.isInfoEnabled()) {
                    int heartbeat = channel.getInfo().getHeartbeat(0);
                    if(LOG.isTraceEnabled()) {
                        LOG.trace("Received heartbeat from remote channel " + channel.getRemoteAddress()
                                        + ", cause: The channel has no data-transmission exceeds a heartbeat period"
                                        + (heartbeat > 0 ? ": " + heartbeat + "ms" : ""));
                    }
                  }
            }
            return;
        }
        if (isHeartbeatResponse(message)) {
            if (LOG.isTraceEnabled()) {
                  LOG.trace(
                    new StringBuilder(32)
                        .append("Receive heartbeat response in thread ")
                        .append(Thread.currentThread().getName())
                        .toString());
            }
            return;
        }
        handler.received(channel, message);
    }
}
