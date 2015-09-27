/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.rs.support;

import java.util.Dictionary;

import org.solmix.exchange.Client;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteServiceFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月20日
 */

public class RemoteReferenceImpl<S> implements RemoteReference<S>
{

    protected ServiceProperties properties;

    private Class<S> clazz;
    private volatile boolean available;
    private volatile boolean destroyed = false;
    private RemoteServiceFactory factory;

    private Client client;
    public RemoteReferenceImpl(Class<S> clazz, Dictionary<String, ?> properties,RemoteServiceFactory factory)
    {
        this.clazz = clazz;
        this.properties = createProperties(properties);
        this.factory=factory;
        //还未创建远程连接，不可用
        this.available=false;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    protected ServiceProperties createProperties(Dictionary<String, ?> props) {
        ServiceProperties sp = new ServiceProperties(props);
        sp.setReadOnly();
        return sp;
    }

    @Override
    public String[] getPropertyKeys() {
        return properties.getPropertyKeys();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
    
    protected void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public void destroy() {
        if (isDestroyed()) {
            return;
        }
        destroyed = true;
        setAvailable(false);
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }


    @Override
    public RemoteServiceFactory getRemoteServiceFactory() {
        return factory;
    }

    @Override
    public Class<S> getServiceClass() {
        return clazz;
    }

    public void setClient(Client client){
        this.client=client;
    }

    @Override
    public org.solmix.hola.rs.RemoteReference.ReferenceType getReferenceType() {
        return ReferenceType.REMOTE;
    }
}
