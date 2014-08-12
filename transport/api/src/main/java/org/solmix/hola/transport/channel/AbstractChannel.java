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
package org.solmix.hola.transport.channel;

import java.net.InetSocketAddress;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.transport.TransportException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月15日
 */

public abstract class AbstractChannel implements Channel
{
    private volatile EndpointInfo param;
    
    private volatile boolean     closed;

    private final ChannelHandler handler;
    
    public AbstractChannel(EndpointInfo param,ChannelHandler handler){
        Assert.isNotNull(param);
        Assert.isNotNull(handler);
        this.param=param;
        this.handler=handler;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#isConnected()
     */
    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(String key) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String key, Object value) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String key) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#isClosed()
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#close(int)
     */
    @Override
    public void close(int timeout) {
        close();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#close()
     */
    @Override
    public void close() {
        closed = true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#send(java.lang.Object)
     */
    @Override
    public void send(Object res) throws TransportException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getEndpointInfo()
     */
    @Override
    public EndpointInfo getEndpointInfo() {
        return param;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getRemoteAddress()
     */
    @Override
    public InetSocketAddress getRemoteAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getLocalAddress()
     */
    @Override
    public InetSocketAddress getLocalAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#send(java.lang.Object, boolean)
     */
    @Override
    public void send(Object message, boolean sent) throws TransportException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.channel.Channel#getChannelHandler()
     */
    @Override
    public ChannelHandler getChannelHandler() {
        // TODO Auto-generated method stub
        return null;
    }

}
