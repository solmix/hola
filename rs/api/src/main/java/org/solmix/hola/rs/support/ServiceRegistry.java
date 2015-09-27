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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.exchange.model.NamedID;
import org.solmix.hola.rs.RemoteListener;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.event.RemoteEvent;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月20日
 */

public class ServiceRegistry {

    protected final Map<NamedID, RemoteRegistration<?>> services = new ConcurrentHashMap<NamedID, RemoteRegistration<?>>();
    protected final List<RemoteListener> listeners = new ArrayList<RemoteListener>(4);

    private final RemoteServiceFactory remoteServiceFactory;

    public ServiceRegistry(RemoteServiceFactory manager) {
        remoteServiceFactory = manager;
    }

    public void addServiceRegistration(NamedID serviceKey,RemoteRegistration<?> regitration) {
        //TODO
    }

    public void removeServiceRegistration(NamedID serviceKey, RemoteRegistration<?> regitration) {
        //TODO
    }
    
    public RemoteServiceFactory getRemoteServiceFactory(){
        return remoteServiceFactory;
    }

    public void publishServiceEvent(RemoteEvent event) {
        List<RemoteListener> entries;
        synchronized (listeners) {
            entries = new ArrayList<RemoteListener>(listeners);
        }
        for (RemoteListener listener : entries) {
            listener.onHandle(event);
        }
        
    }


    public void addRemoteListener(RemoteListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
      }
    }
    
    public void removeRemoteListener(RemoteListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
      }
    }
    
    public Object getService(RemoteReferenceHolder<?> reference) {
       return reference.getRegistration().getService();
    }

    /**
     * 
     */
    public void destroy() {
        synchronized (listeners) {
            listeners.clear();
        }
        
    }
}
