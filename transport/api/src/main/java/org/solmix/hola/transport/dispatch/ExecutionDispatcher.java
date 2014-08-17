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

package org.solmix.hola.transport.dispatch;

import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.dispatch.ChannelEventRunnable.ChannelState;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年8月17日
 */
@Extension(name=ExecutionDispatcher.NAME)
public class ExecutionDispatcher extends AbstractDispatcher
{
    public static final String NAME="execution";
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.dispatch.Dispatcher#dispatch(org.solmix.hola.transport.channel.ChannelHandler,
     *      org.solmix.hola.core.model.ChannelInfo)
     */
    @Override
    public ChannelHandler dispatch(ChannelHandler handler, ChannelInfo info) {
        return new ExecutionDispatcherHandler(handler, info, getContainer());
    }

    class ExecutionDispatcherHandler extends AbstractDispatcherHandler
    {

        /**
         * @param handler
         * @param info
         * @param container
         */
        public ExecutionDispatcherHandler(ChannelHandler handler,
            ChannelInfo info, Container container)
        {
            super(handler, info, container);
        }

        @Override
        public void connected(Channel channel) throws TransportException {
            executor.execute(new ChannelEventRunnable(channel, handler,
                ChannelState.CONNECTED));
        }

        @Override
        public void disconnected(Channel channel) throws TransportException {
            executor.execute(new ChannelEventRunnable(channel, handler,
                ChannelState.DISCONNECTED));
        }

        @Override
        public void received(Channel channel, Object message)
            throws TransportException {
            executor.execute(new ChannelEventRunnable(channel, handler,
                ChannelState.RECEIVED, message));
        }

        @Override
        public void caught(Channel channel, Throwable exception)
            throws TransportException {
            executor.execute(new ChannelEventRunnable(channel, handler,
                ChannelState.CAUGHT, exception));
        }

    }
}
