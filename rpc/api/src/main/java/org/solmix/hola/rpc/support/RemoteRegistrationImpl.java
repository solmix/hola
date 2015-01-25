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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.solmix.hola.rpc.RemoteListener;
import org.solmix.hola.rpc.RemoteReference;
import org.solmix.hola.rpc.RemoteRegistration;
import org.solmix.hola.rpc.RpcManager;
import org.solmix.hola.rpc.event.RemoteRegisteredEvent;
import org.solmix.hola.rpc.event.RemoteUnregisteredEvent;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月20日
 */

public class RemoteRegistrationImpl<S> implements RemoteRegistration<S> {

    public static final int REGISTERED = 0x00;

    public static final int UNREGISTERING = 0x01;

    public static final int UNREGISTERED = 0x02;

    protected transient Object registrationLock = new Object();

    protected final S service;

    protected final Class<?> clazze;

    protected int state;

    protected RemoteReferenceImpl<S> reference;

    private final RpcManager manager;

    protected final ServiceRegistry registry;

    protected ServiceProperties properties;

    protected final List<RemoteListener> listeners = new ArrayList<RemoteListener>(
        4);

    protected String serviceKey;

    public RemoteRegistrationImpl(RpcManager manager, ServiceRegistry registry,
        Class<?> clazze, S service) {
        this.clazze = clazze;
        this.service = service;
        this.manager = manager;
        this.registry = registry;
        synchronized (registrationLock) {
            this.state = REGISTERED;
            reference = new RemoteReferenceImpl<S>(this);
        }
    }

    public void register(Dictionary<String, ?> props) {
        final RemoteReferenceImpl<S> ref;
        synchronized (registry) {
            synchronized (registrationLock) {
                ref = reference;
                this.properties = createProperties(props);
            }
            serviceKey = createServiceKey(properties);
            registry.addServiceRegistration(serviceKey, this);
        }
        registry.publishServiceEvent(new RemoteRegisteredEvent(ref));
    }

    protected String createServiceKey(ServiceProperties props) {
        return null;
    }

    protected ServiceProperties createProperties(Dictionary<String, ?> props) {
        assert Thread.holdsLock(registrationLock);
        ServiceProperties sp = new ServiceProperties(props);
        sp.setReadOnly();
        return sp;
    }

    public String getServiceKey() {
        return serviceKey;
    }
    public S getServiceObject(){
        return service;
    }
    public Class<?> getClazze(){
        return clazze;
    }

    @Override
    public RemoteReference<S> getReference() {
        return getReferenceImpl();
    }

    public RemoteReferenceImpl<S> getReferenceImpl() {
        synchronized (registrationLock) {
            if (reference == null) {
                throw new IllegalStateException("Service already nuregistered.");
            }
            return reference;
        }
    }

    @Override
    public void unregister() {
        final RemoteReferenceImpl<S> ref;
        synchronized (registry) {
            synchronized (registrationLock) {
                if (state != REGISTERED) {
                    throw new IllegalStateException(
                        "Service already unregistered.");
                }
                registry.removeServiceRegistration(serviceKey, this);
                state = UNREGISTERING;
                ref = reference;
            }
        }
        registry.publishServiceEvent(new RemoteUnregisteredEvent(ref));
        synchronized (registrationLock) {
            state = UNREGISTERED;
        }

    }

    public Object getProperty(String key) {
        synchronized (registrationLock) {
            return properties.getProperty(key);
        }
    }

    /**
     * @return
     */
    public String[] getPropertyKeys() {
        synchronized (registrationLock) {
            return properties.getPropertyKeys();
        }
    }

    /**
     * @return
     */
    public RpcManager getManager() {
        synchronized (registrationLock) {
            if (reference == null) {
                return null;
            }
            return manager;
        }
    }

    public Object getService() {
        return service;
    }
}
