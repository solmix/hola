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

import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Server;
import org.solmix.hola.transport.dispatch.Dispatcher;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月18日
 */
@Extension(name="noheartbeat")
public class NoHeartbeatNettyTransporter extends NettyTransporter
{

    /**
     * @param container
     */
    public NoHeartbeatNettyTransporter(Container container)
    {
        super(container);
    }
    @Override
    protected Server newServer(ChannelInfo info,ChannelHandler handler)
        throws TransportException {
        return new NoHeartbeatNettyServer(info, handler,getContainer());
    }
    class  NoHeartbeatNettyServer extends NettyServer{
     
        public NoHeartbeatNettyServer(ChannelInfo info, ChannelHandler handler,
            Container container) throws TransportException
        {
            super(info, handler, container);
        }
        @Override
        protected   ChannelHandler wrapChannelHandler(ChannelInfo info,
            ChannelHandler handler) {
            String dispatch = info.getDispather(HolaConstants.DEFAULT_DISPATHER);
            ChannelHandler dis = getContainer().getExtensionLoader(Dispatcher.class)
                .getExtension(dispatch)
                .dispatch(handler, info);
            return dis;

        }
    }
}
