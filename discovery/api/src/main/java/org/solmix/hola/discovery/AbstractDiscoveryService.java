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

package org.solmix.hola.discovery;

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
import org.solmix.hola.core.AbstractConnectContext;
import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.event.ServiceEvent;
import org.solmix.hola.discovery.event.ServiceTypeEvent;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.internal.DiscoveryServiceListener;
import org.solmix.hola.discovery.internal.DiscoveryServiceTypeListener;
import org.solmix.hola.discovery.internal.ServiceMetadataTracker;
import org.solmix.runtime.adapter.AdapterManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月4日
 */
@ThreadSafe
public abstract class AbstractDiscoveryService extends AbstractConnectContext
    implements DiscoveryService
{

    protected final Map<ServiceType, Collection<ServiceListener>> serviceListeners;

    private final String discoveryNamespace;

    /**
     * 
     */
    protected final Set<ServiceListener> allServiceListeners;

    protected final Collection<ServiceTypeListener> serviceTypeListeners;

    private final DiscoveryServiceListener discoveryServiceListener;

    private final DiscoveryServiceTypeListener discoveryServiceTypeListener;

    private final ServiceMetadataTracker serviceMetadataTracker;

    private final ServiceTypeComparator comparator;

    public AbstractDiscoveryService(String discoveryNamespace)
    {
        this.discoveryNamespace = discoveryNamespace;
        Assert.isNotNull(discoveryNamespace);
        allServiceListeners = Collections.synchronizedSet(new HashSet<ServiceListener>());
        serviceListeners = Collections.synchronizedMap(new HashMap<ServiceType, Collection<ServiceListener>>());
        serviceTypeListeners = Collections.synchronizedSet(new HashSet<ServiceTypeListener>());
        discoveryServiceListener = new DiscoveryServiceListener(this);
        discoveryServiceTypeListener = new DiscoveryServiceTypeListener(this);
        serviceMetadataTracker = new ServiceMetadataTracker(this);
        comparator = new ServiceTypeComparator();
    }

    @Override
    public Namespace getDiscoveryNamespace() {
        return DefaultIDFactory.getDefault().getNamespaceByName(
            discoveryNamespace);
    }

    @Override
    public void addServiceListener(final ServiceListener listener) {
        if (listener.triggerDiscovery()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    final ServiceMetadata[] metadatas = getServices();
                    for (ServiceMetadata metadata : metadatas) {
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
    protected ServiceEvent getDiscoveryServiceEvent(ServiceMetadata metadata) {
        return new ServiceEvent(this, metadata);
    }

    @Override
    public void addServiceListener(final ServiceType type,
        final ServiceListener listener) {
        Assert.isNotNull(type);
        Assert.isNotNull(listener);
        if (listener.triggerDiscovery()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    final ServiceMetadata[] metadatas = getServices(type);
                    for (ServiceMetadata metadata : metadatas) {
                        listener.discovered(getDiscoveryServiceEvent(metadata));
                    }
                    addServiceListener0(type, listener);
                }
            });
        } else {
            addServiceListener0(type, listener);
        }
    }

    private void addServiceListener0(final ServiceType type,
        final ServiceListener listener) {
        synchronized (serviceListeners) {
            Collection<ServiceListener> v = serviceListeners.get(type);
            if (v == null) {
                v = Collections.synchronizedSet(new HashSet<ServiceListener>());
                serviceListeners.put(type, v);
            }
            v.add(listener);
        }
    }

    @Override
    public void addServiceTypeListener(ServiceTypeListener listener) {
        Assert.isNotNull(listener);
        serviceTypeListeners.add(listener);
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        Assert.isNotNull(listener);
        serviceListeners.remove(listener);

    }

    @Override
    public void removeServiceListener(ServiceType type, ServiceListener listener) {
        Assert.isNotNull(type);
        Assert.isNotNull(listener);
        synchronized (serviceListeners) {
            final Collection<ServiceListener> ls = serviceListeners.get(type);
            if (ls != null) {
                ls.remove(listener);
            }
        }
    }

    @Override
    public void removeServiceTypeListener(ServiceTypeListener listener) {
        Assert.isNotNull(listener);
        serviceTypeListeners.remove(listener);
    }

    @Override
    public void unregisterAllServices() {
        throw new java.lang.UnsupportedOperationException();

    }

    @Override
    public Future<ServiceType[]> getAsyncServiceTypes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new Callable<ServiceType[]>() {

            @Override
            public ServiceType[] call() throws Exception {
                return getServiceTypes();
            }

        });
    }

    @Override
    public Future<ServiceMetadata[]> getAsyncServices() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new Callable<ServiceMetadata[]>() {

            @Override
            public ServiceMetadata[] call() throws Exception {
                return getServices();
            }

        });
    }

    @Override
    public Future<ServiceMetadata> getAsyncService(final ServiceID serviceID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new Callable<ServiceMetadata>() {

            @Override
            public ServiceMetadata call() throws Exception {
                return getService(serviceID);
            }
        });
    }

    @Override
    public Future<ServiceMetadata[]> getAsyncServices(final ServiceType type) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new Callable<ServiceMetadata[]>() {

            @Override
            public ServiceMetadata[] call() throws Exception {
                return getServices(type);
            }
        });
    }

    @Override
    public Namespace getServicesNamespace() {
        return DefaultIDFactory.getDefault().getNamespaceByName(
            discoveryNamespace);
    }

    @Override
    public Namespace getRemoteNamespace() {
        return getServicesNamespace();
    }

    @Override
    public void destroy() {
        disconnect();
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

    protected void fireServiceDiscovered(ServiceEvent serviceEvent) {
        Assert.isNotNull(serviceEvent);
        final Collection<ServiceListener> listeners = getListeners(serviceEvent.getServiceMetadata().getServiceID().getServiceType());
        if (listeners != null) {
            for (ServiceListener listener : listeners) {
                listener.discovered(serviceEvent);
            }
        }
    }

    protected void fireServiceUnDiscovered(ServiceEvent serviceEvent) {
        Assert.isNotNull(serviceEvent);
        final Collection<ServiceListener> listeners = getListeners(serviceEvent.getServiceMetadata().getServiceID().getServiceType());
        if (listeners != null) {
            for (ServiceListener listener : listeners) {
                listener.undiscovered(serviceEvent);
            }
        }
    }

    protected void fireServiceTypeDiscovered(ServiceTypeEvent event) {
        Assert.isNotNull(event);
        List<ServiceTypeListener> notify = null;
        synchronized (serviceTypeListeners) {
            notify = new ArrayList<ServiceTypeListener>(serviceTypeListeners);
        }
        for (final Iterator<ServiceTypeListener> i = notify.iterator(); i.hasNext();) {
            final ServiceTypeListener l = i.next();
            l.serviceTypeDiscovered(event);
        }
    }

    private Collection<ServiceListener> getListeners(ServiceType serviceType) {
        Collection<ServiceListener> listeners = new HashSet<ServiceListener>();
        synchronized (serviceListeners) {
            for (ServiceType type : serviceListeners.keySet()) {
                int compare = comparator.compare(serviceType, type);
                if (compare == 0) {
                    Collection<ServiceListener> collection = serviceListeners.get(serviceType);
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
        if(type==null)
            return null;
        if(type.isInstance(this)){
            return type.cast(this);
        }
        AdapterManager ad= org.solmix.hola.discovery.internal. Activator.getDefault().getAdapterManager();

        return (ad==null)?null:ad.getAdapter(this, type);
    }
}
