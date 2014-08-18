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

package org.solmix.hola.transport.protocol;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.exchange.Request;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月14日
 */

public class HeartBeatTask implements Runnable
{

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);

    private final ChannelProvider channelProvider;

    private final int heartbeat;

    private final int heartbeatTimeout;

    HeartBeatTask(ChannelProvider provider, int heartbeat, int heartbeatTimeout)
    {
        this.channelProvider = provider;
        this.heartbeat = heartbeat;
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public void run() {
        try {
            long now = System.currentTimeMillis();
            for (Channel channel : channelProvider.getChannels()) {
                if (channel.isClosed()) {
                    continue;
                }
                try {
                    Long lastRead = (Long) channel.getAttribute(ProtocolExchangeHandler.KEY_READ_TIMESTAMP);
                    Long lastWrite = (Long) channel.getAttribute(ProtocolExchangeHandler.KEY_WRITE_TIMESTAMP);
                    if ((lastRead != null && now - lastRead > heartbeat)
                        || (lastWrite != null && now - lastWrite > heartbeat)) {
                        Request req = new Request();
                        req.setVersion("2.0.0");
                        req.setTwoWay(true);
                        req.setEvent(Request.HEARTBEAT_EVENT);
                        channel.send(req);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Send heartbeat to remote channel "
                                + channel.getRemoteAddress()
                                + ", cause: The channel has no data-transmission exceeds a heartbeat period: "
                                + heartbeat + "ms");
                        }
                    }
                    //超过心跳周期,启动重连或者直接关闭
                    if (lastRead != null && now - lastRead > heartbeatTimeout) {
                        logger.warn("Close channel " + channel
                            + ", because heartbeat read idle time out: "
                            + heartbeatTimeout + "ms");
                        if (channel instanceof Client) {
                            try {
                                ((Client) channel).reconnect();
                            } catch (Exception e) {
                                // do nothing
                            }
                        } else {
                            channel.close();
                        }
                    }
                } catch (Throwable t) {
                    logger.warn("Exception when heartbeat to remote channel "
                        + channel.getRemoteAddress(), t);
                }
            }
        } catch (Throwable t) {
            logger.warn( "Unhandled exception when heartbeat, cause: " 
                    + t.getMessage(),t);
        }
    }

    interface ChannelProvider
    {

        Collection<Channel> getChannels();
    }

}
