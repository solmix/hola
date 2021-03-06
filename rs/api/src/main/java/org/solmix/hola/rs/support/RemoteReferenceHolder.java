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

package org.solmix.hola.rs.support;

import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteServiceFactory;

/**
 * 注册的远程服务的本地引用
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月20日
 */

public class RemoteReferenceHolder<S> implements RemoteReference<S> {

    private final RemoteRegistrationImpl<S> registration;
    private volatile boolean available = true;
    private volatile boolean destroyed = false;
    RemoteReferenceHolder(RemoteRegistrationImpl<S> registration) {
        this.registration = registration;
    }

    @Override
    public Object getProperty(String key) {
        return registration.getProperty(key);
    }

    @Override
    public String[] getPropertyKeys() {
        return registration.getPropertyKeys();
    }

    
    @Override
    public RemoteServiceFactory getRemoteServiceFactory() {
        return registration.getManager();
    }
    
    RemoteRegistrationImpl<S> getRegistration(){
        return registration;
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
    public Class<S> getServiceClass() {
        return registration.getClazze();
    }

    @Override
    public ReferenceType getReferenceType() {
        return ReferenceType.LOCAL;
    }
}
