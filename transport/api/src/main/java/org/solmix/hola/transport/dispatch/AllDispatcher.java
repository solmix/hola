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

import java.util.concurrent.ExecutorService;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.ExecutionException;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.dispatch.ChannelEventRunnable.ChannelState;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月17日
 */
@Extension(name=AllDispatcher.NAME)
public class AllDispatcher extends AbstractDispatcher
{

    public static final String NAME="all";
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.dispatch.Dispatcher#dispatch(org.solmix.hola.transport.channel.ChannelHandler, org.solmix.hola.core.model.RemoteInfo)
     */
    @Override
    public ChannelHandler dispatch(ChannelHandler handler, RemoteInfo param) {
        return new AllDispatcherHandler(handler,param,getContainer());
    }

    class AllDispatcherHandler extends AbstractDispatcherHandler{

        /**
         * @param handler
         * @param info
         * @param container
         */
        public AllDispatcherHandler(ChannelHandler handler, RemoteInfo info,
            Container container)
        {
            super(handler, info, container);
        }
        @Override
        public void connected(Channel channel) throws TransportException {
            ExecutorService cexecutor = getExecutorService(); 
            try{
                cexecutor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CONNECTED));
            }catch (Throwable t) {
                throw new ExecutionException("connect event", channel, getClass()+" error when process connected event ." , t);
            }
        }
        
        @Override
        public void disconnected(Channel channel) throws TransportException {
            ExecutorService cexecutor = getExecutorService(); 
            try{
                cexecutor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.DISCONNECTED));
            }catch (Throwable t) {
                throw new ExecutionException("disconnect event", channel, getClass()+" error when process disconnected event ." , t);
            }
        }

        @Override
        public void received(Channel channel, Object message) throws TransportException {
            ExecutorService cexecutor = getExecutorService();
            try {
                cexecutor.execute(new ChannelEventRunnable(channel, handler, ChannelState.RECEIVED, message));
            } catch (Throwable t) {
                throw new ExecutionException(message, channel, getClass() + " error when process received event .", t);
            }
        }

        @Override
        public void caught(Channel channel, Throwable exception) throws TransportException {
            ExecutorService cexecutor = getExecutorService(); 
            try{
                cexecutor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CAUGHT, exception));
            }catch (Throwable t) {
                throw new ExecutionException("caught event", channel, getClass()+" error when process caught event ." , t);
            }
        }

        private ExecutorService getExecutorService() {
            ExecutorService cexecutor = executor;
            if (cexecutor == null || cexecutor.isShutdown()) { 
                cexecutor = SHARED_EXECUTOR;
            }
            return cexecutor;
        }
        
    }
}
