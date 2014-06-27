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
package org.solmix.hola.shared.generic.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.HolaException;
import org.solmix.hola.core.identity.ID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年6月25日
 */

public class ClientChannel implements Channel
{
    protected Socket socket;
    
    public ClientChannel(ChannelHandler handler,int keepAlive){
        
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#connect(org.solmix.hola.core.identity.ID, java.lang.Object, int)
     */
    @Override
    public Object connect(ID targetId, Object authData, int timeout)
        throws HolaException {
        if(socket!=null)
            throw new HolaException("already connected!");
        Assert.isNotNull(targetId);
        URI url=parseTargetId(targetId);
        final Socket s = createClientSocket(url, timeout);
        return null;
    }

   
    protected Socket createClientSocket(URI url, int timeout) throws HolaException {
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(url.getHost(), url.getPort()), timeout);
        } catch (IOException e) {
           throw new HolaException("can't create socket for id:"+url,e);
        }
        return s;
    }
    /**
     * @param targetId
     * @return
     * @throws HolaException 
     */
    private URI parseTargetId(ID targetId) throws HolaException {
        try {
            return new URI(targetId.getName());
        } catch (URISyntaxException e) {
           throw new HolaException("Invalid URI for ID:"+targetId, e);
        }
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#disconnect()
     */
    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#isConnected()
     */
    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#getLocalID()
     */
    @Override
    public ID getLocalID() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#isStarted()
     */
    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String key, Object value) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String key) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#post(org.solmix.hola.core.identity.ID, byte[])
     */
    @Override
    public void post(ID receiver, byte[] data) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.support.Channel#send(org.solmix.hola.core.identity.ID, byte[])
     */
    @Override
    public Object send(ID receiver, byte[] data) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
