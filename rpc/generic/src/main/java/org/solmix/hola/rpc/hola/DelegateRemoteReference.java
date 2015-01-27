/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.rpc.hola;

import java.io.Serializable;
import java.util.Dictionary;

import org.solmix.hola.rpc.RemoteReference;
import org.solmix.hola.rpc.RpcManager;
import org.solmix.hola.rpc.support.ServiceProperties;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月31日
 */

public class DelegateRemoteReference<S> implements RemoteReference<S>,
    Serializable {

    private static final long serialVersionUID = 7448513598696809288L;
    private final RpcManager manager;
    HolaDelegate delegate ;
    private final Class<S> clazz;
    private ServiceProperties properties;
    DelegateRemoteReference(RpcManager manager,Class<S> clazz,
        Dictionary<String, ?> properties){
        this.manager=manager;
        this.clazz=clazz;
    }
    
    @Override
    public Object getProperty(String key) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String[] getPropertyKeys() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public RpcManager getRpcManager() {
        return manager;
    }
    
    S getServiceObject(){
        if(delegate==null){
            
        }
        return delegate.getProxy(null, clazz, properties);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rpc.RemoteReference#isAvailable()
     */
    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rpc.RemoteReference#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}
