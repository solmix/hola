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

package org.solmix.hola.discovery.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.event.ConnectedEvent;
import org.solmix.hola.core.event.ConnectingEvent;
import org.solmix.hola.core.event.DisconnectedEvent;
import org.solmix.hola.core.event.DisconnectingEvent;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.identity.support.DefaultIDFactory;
import org.solmix.hola.core.security.ConnectSecurityContext;
import org.solmix.hola.discovery.AbstractDiscovery;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.ServiceListener;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.ServiceEvent;
import org.solmix.hola.discovery.event.ServiceTypeEvent;
import org.solmix.hola.discovery.identity.DefaultServiceTypeFactory;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.ServiceMetadataImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年6月7日
 */

public class CompositeDiscoveryProvider extends AbstractDiscovery
    implements Discovery
{

    

    protected Set<ServiceMetadata> registeredServices = new HashSet<ServiceMetadata>();

    protected final Collection<Discovery> providers = new ArrayList<Discovery>();

    /**
     * @param discoveryNamespace
     */
    public CompositeDiscoveryProvider()
    {
        super(CompositeNamespace.NAME);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryAdvertiser#registerService(org.solmix.hola.discovery.ServiceMetadata)
     */
    @Override
    public void registerService(ServiceMetadata serviceMetadata) {
        Assert.isNotNull(serviceMetadata);
        synchronized (registeredServices) {
            registeredServices.add(serviceMetadata);
        }
        synchronized (providers) {
            for (Discovery provider : providers) {
                final ServiceMetadata meta = getServiceMetadataForProvider(
                    serviceMetadata, provider);
                provider.registerService(meta);
            }
        }
    }

    /**
     * @param serviceMetadata
     * @param provider
     * @return
     */
    private ServiceMetadata getServiceMetadataForProvider(
        ServiceMetadata smeta, Discovery provider) {
        ServiceID sid = smeta.getServiceID();
        ServiceID nsid = getServiceIDforProvider(sid, provider);
        ServiceType type = nsid.getServiceType();
        return new ServiceMetadataImpl(sid.getLocation(), sid.getServiceName(),
            type, smeta.getServiceProperties());
    }

    /**
     * @param sid
     * @param provider
     * @return
     */
    private ServiceID getServiceIDforProvider(ServiceID sid,
        Discovery provider) {
        Namespace ns = provider.getDiscoveryNamespace();
        if (!ns.equals(sid.getNamespace())) {
            return (ServiceID) ns.createID(new Object[] { sid.getName(),
                sid.getLocation() });
        }
        return sid;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryAdvertiser#unregisterService(org.solmix.hola.discovery.ServiceMetadata)
     */
    @Override
    public void unregisterService(ServiceMetadata serviceMetadata) {
        Assert.isNotNull(serviceMetadata);
        synchronized (registeredServices) {
            registeredServices.remove(serviceMetadata);
        }
        synchronized (providers) {
            for (Discovery provider : providers) {
                final ServiceMetadata meta = getServiceMetadataForProvider(
                    serviceMetadata, provider);
                provider.unregisterService(meta);
            }
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getService(org.solmix.hola.discovery.identity.ServiceID)
     */
    @Override
    public ServiceMetadata getService(ServiceID aServiceID) {
        Assert.isNotNull(aServiceID);
        synchronized (providers) {
            for (Discovery provider : providers) {
                final ServiceID nid = getServiceIDforProvider(aServiceID,
                    provider);
                ServiceMetadata result = provider.getService(nid);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices()
     */
    @Override
    public ServiceMetadata[] getServices() {
        Set<ServiceMetadata> all = new HashSet<ServiceMetadata>();
        synchronized (providers) {
            for (Discovery provider : providers) {
                ServiceMetadata[] services = provider.getServices();
                all.addAll(Arrays.asList(services));
            }
        }
        return all.toArray(new ServiceMetadata[all.size()]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices(org.solmix.hola.discovery.identity.ServiceType)
     */
    @Override
    public ServiceMetadata[] getServices(ServiceType type) {
        Set<ServiceMetadata> all = new HashSet<ServiceMetadata>();
        synchronized (providers) {
            for (Discovery provider : providers) {
                ServiceType ntype = getServiceTypeForProvider(type, provider);
                ServiceMetadata[] services = provider.getServices(ntype);
                all.addAll(Arrays.asList(services));
            }
        }
        return all.toArray(new ServiceMetadata[all.size()]);
    }

    /**
     * @param type
     * @param provider
     * @return
     */
    private ServiceType getServiceTypeForProvider(ServiceType type,
        Discovery provider) {
        Namespace ns = provider.getDiscoveryNamespace();
        if (!ns.equals(type.getNamespace())) {
            return DefaultServiceTypeFactory.getDefault().create(ns, type);
        }
        return type;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServiceTypes()
     */
    @Override
    public ServiceType[] getServiceTypes() {
        Set<ServiceType> all = new HashSet<ServiceType>();
        synchronized (providers) {
            for (Discovery provider : providers) {
                ServiceType[] services = provider.getServiceTypes();
                all.addAll(Arrays.asList(services));
            }
        }
        return all.toArray(new ServiceType[all.size()]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#purgeCache()
     */
    @Override
    public ServiceMetadata[] purgeCache() {
        Set<ServiceMetadata> all = new HashSet<ServiceMetadata>();
        synchronized (providers) {
            for (Discovery provider : providers) {
                ServiceMetadata[] services = provider.purgeCache();
                all.addAll(Arrays.asList(services));
            }
        }
        return all.toArray(new ServiceMetadata[all.size()]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#connect(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.core.security.ConnectSecurityContext)
     */
    @Override
    public void connect(ID remoteID, ConnectSecurityContext securityContext)
        throws ConnectException {
        if (getTargetID() != null)
            throw new ConnectException("Already connected");
        targetID = remoteID == null ? getDefaultID() : remoteID;
        fireConnectEvent(new ConnectingEvent(this, this.getID(), targetID,
            securityContext));
        synchronized (providers) {
            final Collection<Discovery> failedToConnect = new HashSet<Discovery>();
            for (Discovery provider : providers) {
                if (provider.getTargetID() == null) {
                    try {
                        provider.connect(targetID, securityContext);
                    } catch (ConnectException e) {
                        failedToConnect.add(provider);
                        continue;
                    }
                }
                provider.addServiceListener(serviceListener);
                provider.addServiceTypeListener(serviceTypeListener);

            }
            providers.removeAll(failedToConnect);
        }
        fireConnectEvent(new ConnectedEvent(this, this.getID(), targetID));

    }

    /**
     * @return
     */
    protected ID getDefaultID() {
        return DefaultIDFactory.getDefault().createStringID(
            CompositeDiscoveryProvider.class.getName());
    }

    private ID targetID;

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#getTargetID()
     */
    @Override
    public ID getTargetID() {
        return targetID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#disconnect()
     */
    @Override
    public void disconnect() {
        fireConnectEvent(new DisconnectingEvent(this, getID(), targetID));
       
        targetID=null;
        synchronized (providers) {
            for(Discovery provider:providers){
                provider.disconnect();
            }
            providers.clear();
            
        }
        synchronized (registeredServices) {
            registeredServices.clear();
        }
        synchronized (allServiceListeners) {
            allServiceListeners.clear();
        }
        synchronized (serviceTypeListeners) {
            serviceTypeListeners.clear();
        }
        fireConnectEvent(new DisconnectedEvent(this, getID(), targetID));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Identifiable#getID()
     */
    @Override
    public ID getID() {
        return getDefaultID();
    }

    /**
     * @param obj
     */
    public boolean addProvider(Object obj) {
        Discovery p=(Discovery)obj;
        if(p.getTargetID()==null){
            try {
                p.connect(targetID, null);
            } catch (ConnectException e) {
               return false;
            }
        }
        p.addServiceListener(serviceListener);
        p.addServiceTypeListener(serviceTypeListener);
        synchronized(registeredServices){
            for(ServiceMetadata meta:registeredServices){
                p.registerService(meta);
            }
        }
        synchronized (providers) {
            return providers.add(p);
        }
    }

    /**
     * @param anIDS
     */
    public boolean removeProvider(Object obj) {
        Discovery p=(Discovery)obj;
        p.removeServiceListener(serviceListener);
        p.removeServiceTypeListener(serviceTypeListener);
        synchronized (providers) {
            return providers.remove(p);
        }
    }

    private final ServiceListener serviceListener = new InterServiceListener();

    private final ServiceTypeListener serviceTypeListener = new InternalServiceTypeListener();

    protected class InterServiceListener implements ServiceListener
    {

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.discovery.ServiceListener#triggerDiscovery()
         */
        @Override
        public boolean triggerDiscovery() {
            return false;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.discovery.ServiceListener#discovered(org.solmix.hola.discovery.event.ServiceEvent)
         */
        @Override
        public void discovered(ServiceEvent event) {
            Collection<ServiceListener> listeners = getListeners(event.getServiceMetadata().getServiceID().getServiceType());
            if (!listeners.isEmpty()) {
                for (ServiceListener listener : listeners) {
                    listener.discovered(new InternalServiceEvent(this, event));
                }
            }

        }

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.discovery.ServiceListener#undiscovered(org.solmix.hola.discovery.event.ServiceEvent)
         */
        @Override
        public void undiscovered(ServiceEvent event) {
            Collection<ServiceListener> listeners = getListeners(event.getServiceMetadata().getServiceID().getServiceType());
            if (!listeners.isEmpty()) {
                for (ServiceListener listener : listeners) {
                    listener.undiscovered(new InternalServiceEvent(this, event));
                }
            }

        }

    }

    protected class InternalServiceTypeListener implements ServiceTypeListener
    {

        /**
         * {@inheritDoc}
         * 
         * @see org.solmix.hola.discovery.ServiceTypeListener#serviceTypeDiscovered(org.solmix.hola.discovery.event.ServiceTypeEvent)
         */
        @Override
        public void serviceTypeDiscovered(ServiceTypeEvent event) {
            synchronized (serviceTypeListeners) {
                for (ServiceTypeListener listener : serviceTypeListeners) {
                    listener.serviceTypeDiscovered(new InternalServiceTypeEvent(
                        this, event));
                }
            }
            final ServiceType type = event.getServiceType();
            synchronized (providers) {
                for (Discovery provider : providers) {

                    provider.addServiceListener(type, serviceListener);
                }
            }
        }

    }

    class InternalServiceEvent extends ServiceEvent
    {

        /**
         * 
         */
        private static final long serialVersionUID = -5218553155426667712L;

        /**
         * @param source
         * @param metadata
         */
        public InternalServiceEvent(Object source, ServiceEvent event)
        {
            super(source, event.getServiceMetadata());
        }

    }

    class InternalServiceTypeEvent extends ServiceTypeEvent
    {

        /**
         * 
         */
        private static final long serialVersionUID = 8932324633783427005L;

        /**
         * @param source
         * @param serviceType
         */
        public InternalServiceTypeEvent(Object source, ServiceTypeEvent event)
        {
            super(source, event.getServiceType());
        }

    }
}
