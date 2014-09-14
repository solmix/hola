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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月17日
 */

public class ChannelEventRunnable implements Runnable
{
    private static final Logger LOG             = LoggerFactory.getLogger(ChannelEventRunnable.class);

    private final ChannelHandler handler;
    private final Channel channel;
    private final ChannelState state;
    private final Throwable exception;
    private final Object message;
    
    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state) {
        this(channel, handler, state, null);
    }
    
    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state, Object message) {
        this(channel, handler, state, message, null);
    }
    
    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state, Throwable t) {
        this(channel, handler, state, null , t);
    }

    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state, Object message, Throwable exception) {
        this.channel = channel;
        this.handler = handler;
        this.state = state;
        this.message = message;
        this.exception = exception;
    }
    
    @Override
    public void run() {
        switch (state) {
            case CONNECTED:
                try{
                    handler.connected(channel);
                }catch (Exception e) {
                    LOG.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel, e);
                }
                break;
            case DISCONNECTED:
                try{
                    handler.disconnected(channel);
                }catch (Exception e) {
                    LOG.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel, e);
                }
                break;
            case SENT:
                try{
                    handler.sent(channel,message);
                }catch (Exception e) {
                    LOG.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel
                            + ", message is "+ message,e);
                }
                break;
            case RECEIVED:
                try{
                    handler.received(channel, message);
                }catch (Exception e) {
                    LOG.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel
                            + ", message is "+ message,e);
                }
                break;
            case CAUGHT:
                try{
                    handler.caught(channel, exception);
                }catch (Exception e) {
                    LOG.warn("ChannelEventRunnable handle " + state + " operation error, channel is "+ channel
                            + ", message is: " + message + ", exception is " + exception,e);
                }
                break;
            default:
                LOG.warn("unknown state: " + state + ", message is " + message);
        }
    }

    public enum ChannelState
    {

        CONNECTED , DISCONNECTED , SENT , RECEIVED , CAUGHT
    }

}