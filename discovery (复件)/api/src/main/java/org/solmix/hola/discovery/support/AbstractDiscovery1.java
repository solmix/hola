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

package org.solmix.hola.discovery.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryListener;
import org.solmix.hola.discovery.DiscoveryTypeListener;
import org.solmix.hola.discovery.event.DiscoveryEvent;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.identity.DiscoveryID;
import org.solmix.hola.discovery.identity.DiscoveryType;
import org.solmix.hola.discovery.internal.DiscoveryServiceListener;
import org.solmix.hola.discovery.internal.DiscoveryServiceTypeListener;
import org.solmix.hola.discovery.internal.ServiceMetadataTracker;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.adapter.AdapterManager;
import org.solmix.runtime.identity.IDFactory;
import org.solmix.runtime.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月4日
 */
@ThreadSafe
public abstract class AbstractDiscovery1 implements Discovery
{

    protected final Map<DiscoveryType, Collection<DiscoveryListener>> serviceListeners;

    private final String discoveryNamespace;

    protected final Set<DiscoveryListener> allServiceListeners;

    protected final Collection<DiscoveryTypeListener> serviceTypeListeners;

    private final DiscoveryServiceListener discoveryServiceListener;

    private final DiscoveryServiceTypeListener discoveryServiceTypeListener;

    private final ServiceMetadataTracker serviceMetadataTracker;


    private final ExecutorService executor = Executors.newFixedThreadPool(1, new NamedThreadFactory("DiscoveryExecutor", true));

    protected  Container container;


    public AbstractDiscovery1(String discoveryNamespace, Container container)
    {
        Assert.isNotNull(container);
        this.container = container;
        this.discoveryNamespace = discoveryNamespace;
        Assert.isNotNull(discoveryNamespace);
        allServiceListeners = Collections.synchronizedSet(new HashSet<DiscoveryListener>());
        serviceListeners = Collections.synchronizedMap(new HashMap<DiscoveryType, Collection<DiscoveryListener>>());
        serviceTypeListeners = Collections.synchronizedSet(new HashSet<DiscoveryTypeListener>());
        discoveryServiceListener = new DiscoveryServiceListener(this);
        discoveryServiceTypeListener = new DiscoveryServiceTypeListener(this);
        serviceMetadataTracker = new ServiceMetadataTracker(this);
    }
    
    public void setContainer(Container container){
        this.container=container;
    }

    @Override
    public Namespace getNamespace() {
        return IDFactory.getDefault().getNamespaceByName(discoveryNamespace);
    }

    @Override
    public void addServiceListener(final DiscoveryListener listener) {
        if (listener.triggerDiscovery()) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    final DiscoveryInfo[] metadatas = getServices();
                    for (DiscoveryInfo metadata : metadatas) {
                        listener.discovered(getDiscoveryServiceEvent(metadata));
                    }
                    allServiceListeners.add(listener);
                }
            });
        } else {
            allServiceListeners.add(listener);
        }
    }

    /**
     * @return
     */
    protected DiscoveryEvent getDiscoveryServiceEvent(DiscoveryInfo metadata) {
        return new DiscoveryEvent(this, metadata);
    }

    @Override
    public void addServiceListener(final DiscoveryType type, final DiscoveryListener listener) {
        Assert.isNotNull(type);
        Assert.isNotNull(listener);
        if (listener.triggerDiscovery()) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    final DiscoveryInfo[] metadatas = getServices(type);
                    for (DiscoveryInfo metadata : metadatas) {
                        listener.discovered(getDiscoveryServiceEvent(metadata));
                    }
                    addServiceListener0(type, listener);
                }
            });
        } else {
            addServiceListener0(type, listener);
        }
    }

    private void addServiceListener0(final DiscoveryType type, final DiscoveryListener listener) {
        synchronized (serviceListeners) {
            Collection<DiscoveryListener> v = serviceListeners.get(type);
            if (v == null) {
                v = Collections.synchronizedSet(new HashSet<DiscoveryListener>());
                serviceListeners.put(type, v);
            }
            v.add(listener);
        }
    }

    @Override
    public void addServiceTypeListener(DiscoveryTypeListener listener) {
        Assert.isNotNull(listener);
        serviceTypeListeners.add(listener);
    }

    @Override
    public void removeServiceListener(DiscoveryListener listener) {
        Assert.isNotNull(listener);
        serviceListeners.remove(listener);

    }

    @Override
    public void removeServiceListener(DiscoveryType type, DiscoveryListener listener) {
        Assert.isNotNull(type);
        Assert.isNotNull(listener);
        synchronized (serviceListeners) {
            final Collection<DiscoveryListener> ls = serviceListeners.get(type);
            if (ls != null) {
                ls.remove(listener);
            }
        }
    }

    @Override
    public void removeServiceTypeListener(DiscoveryTypeListener listener) {
        Assert.isNotNull(listener);
        serviceTypeListeners.remove(listener);
    }

    @Override
    public void unregisterAll() {
        throw new java.lang.UnsupportedOperationException();

    }

    @Override
    public Future<DiscoveryType[]> getAsyncServiceTypes() {
        return executor.submit(new Callable<DiscoveryType[]>() {

            @Override
            public DiscoveryType[] call() throws Exception {
                return getServiceTypes();
            }

        });
    }

    @Override
    public Future<DiscoveryInfo[]> getAsyncServices() {
        return executor.submit(new Callable<DiscoveryInfo[]>() {

            @Override
            public DiscoveryInfo[] call() throws Exception {
                return getServices();
            }

        });
    }

    @Override
    public Future<DiscoveryInfo> getAsyncService(final DiscoveryID serviceID) {
        return executor.submit(new Callable<DiscoveryInfo>() {

            @Override
            public DiscoveryInfo call() throws Exception {
                return getService(serviceID);
            }
        });
    }

    @Override
    public Future<DiscoveryInfo[]> getAsyncServices(final DiscoveryType type) {
        return executor.submit(new Callable<DiscoveryInfo[]>() {

            @Override
            public DiscoveryInfo[] call() throws Exception {
                return getServices(type);
            }
        });
    }

    public void close() throws IOException {
        // disconnect();
        clearListeners();
        discoveryServiceListener.destroy();
        discoveryServiceTypeListener.destroy();
        serviceMetadataTracker.destroy();
    }

    protected void clearListeners() {
        serviceListeners.clear();
        serviceTypeListeners.clear();
        allServiceListeners.clear();
    }

    protected void fireServiceDiscovered(DiscoveryEvent serviceEvent) {
        Assert.isNotNull(serviceEvent);
        final Collection<DiscoveryListener> listeners = getListeners(serviceEvent.getServiceMetadata().getServiceID().getServiceType());
        if (listeners != null) {
            for (DiscoveryListener listener : listeners) {
                listener.discovered(serviceEvent);
            }
        }
    }

    protected void fireServiceUnDiscovered(DiscoveryEvent serviceEvent) {
        Assert.isNotNull(serviceEvent);
        final Collection<DiscoveryListener> listeners = getListeners(serviceEvent.getServiceMetadata().getServiceID().getServiceType());
        if (listeners != null) {
            for (DiscoveryListener listener : listeners) {
                listener.undiscovered(serviceEvent);
            }
        }
    }

    protected void fireServiceTypeDiscovered(DiscoveryTypeEvent event) {
        Assert.isNotNull(event);
        List<DiscoveryTypeListener> notify = null;
        synchronized (serviceTypeListeners) {
            notify = new ArrayList<DiscoveryTypeListener>(serviceTypeListeners);
        }
        for (final Iterator<DiscoveryTypeListener> i = notify.iterator(); i.hasNext();) {
            final DiscoveryTypeListener l = i.next();
            l.handle(event);
        }
    }

    protected Collection<DiscoveryListener> getListeners(DiscoveryType serviceType) {
        Collection<DiscoveryListener> listeners = new HashSet<DiscoveryListener>();
        synchronized (serviceListeners) {
            for (DiscoveryType type : serviceListeners.keySet()) {
                if (serviceType.equals(type)) {
                    Collection<DiscoveryListener> collection = serviceListeners.get(serviceType);
                    if (collection != null) {
                        listeners.addAll(collection);
                    }
                }
            }

        }
        synchronized (allServiceListeners) {
            listeners.addAll(allServiceListeners);
        }
        return Collections.unmodifiableCollection(listeners);
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type == null)
            return null;
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        AdapterManager ad = org.solmix.hola.discovery.internal.Activator.getDefault().getAdapterManager();

        return (ad == null) ? null : ad.getAdapter(this, type);
    }
}
