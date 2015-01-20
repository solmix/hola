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

package org.solmix.hola.rpc.support;

import java.util.Dictionary;

import org.solmix.hola.rpc.RemoteReference;
import org.solmix.hola.rpc.RemoteRegistration;
import org.solmix.hola.rpc.RpcManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月20日
 */

public  class RemoteRegistrationImpl<S> implements RemoteRegistration<S> {

    public static final int REGISTERED = 0x00;

    public static final int UNREGISTERING = 0x01;

    public static final int UNREGISTERED = 0x02;

    protected transient Object registrationLock = new Object();

    protected final Object service;

    protected final Class<?> clazze;
    private int state;
    private RemoteReferenceImpl<S> reference;
    private final RpcManager manager;

    public RemoteRegistrationImpl(RpcManager manager,Class<?> clazze, Object service) {
        this.clazze = clazze;
        this.service = service;
        this.manager=manager;
        synchronized (registrationLock) {
            this.state = REGISTERED;
            reference=new RemoteReferenceImpl<S>(this);
        }
    }
    
    void register(Dictionary<String, ?> props){
        
    }

    @Override
    public void setProperties(Dictionary<String, ?> properties) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rpc.RemoteRegistration#getReference()
     */
    @Override
    public RemoteReference<S> getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rpc.RemoteRegistration#unregister()
     */
    @Override
    public void unregister() {
        // TODO Auto-generated method stub
        
    }

    public Object getProperty(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public String[] getPropertyKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public RpcManager getManager() {
        synchronized (registrationLock) {
            if(reference==null){
                return null;
            }
            return manager;
        }
    }

}
