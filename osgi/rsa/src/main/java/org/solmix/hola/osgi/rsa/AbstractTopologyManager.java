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

package org.solmix.hola.osgi.rsa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.osgi.internal.Activator;
import org.solmix.hola.osgi.rsa.HolaRemoteServiceAdmin.ImportRegistrationImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年6月4日
 */

public abstract class AbstractTopologyManager
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTopologyManager.class);

    private BundleContext context;

    private final Object remoteServiceAdminTrackerLock = new Object();

    private final ServiceTracker<ServiceMetadataFactory, ServiceMetadataFactory> serviceMetadataFactoryTracker;

    private ServiceTracker<RemoteServiceAdmin, RemoteServiceAdmin> remoteServiceAdminTracker;

    private final ReentrantLock registrationLock;

    private final Map<EndpointDescription, ServiceRegistration<ServiceMetadata>> registrations = new HashMap<EndpointDescription, ServiceRegistration<ServiceMetadata>>();

    public AbstractTopologyManager(BundleContext context)
    {
        serviceMetadataFactoryTracker = new ServiceTracker<ServiceMetadataFactory, ServiceMetadataFactory>(
            org.solmix.hola.osgi.internal.Activator.getContext(),
            createServiceMetadataFactoryFilter(context), null);
        serviceMetadataFactoryTracker.open();
        this.context = context;
        this.registrationLock = new ReentrantLock(true);
    }

    protected BundleContext getContext() {
        return context;
    }

    public void close() {
        registrationLock.lock();
        try {
            registrations.clear();
        } finally {
            registrationLock.unlock();
        }
        synchronized (remoteServiceAdminTrackerLock) {
            if (remoteServiceAdminTracker != null) {
                remoteServiceAdminTracker.close();
                remoteServiceAdminTracker = null;
            }
        }
        context = null;
    }

    /**
     * @param endpoint
     */
    protected void handleEndpointRemoved(EndpointDescription endpoint) {
        HolaRemoteServiceAdmin rsa = (HolaRemoteServiceAdmin) getRemoteServiceAdmin();
        List<ImportRegistrationImpl> importRegs = rsa.getImportedRegistrations();
        for (ImportRegistrationImpl reg : importRegs) {
            if (reg.match(endpoint)) {
                reg.close();
            }
        }
    }

    protected void handleOtherEndpointRemoved(EndpointListener listener,
        EndpointDescription endpoint) {
        notifyOtherEndpointListeners(listener, endpoint, false);
    }

    private Filter createServiceMetadataFactoryFilter(BundleContext ctx) {
        String filterString = new StringBuilder().append("(").append(
            org.osgi.framework.Constants.OBJECTCLASS).append("=").append(
            ServiceMetadataFactory.class.getName()).append(")").toString();
        try {
            return ctx.createFilter(filterString);
        } catch (InvalidSyntaxException doesNotHappen) {
            // Ignore
            doesNotHappen.printStackTrace();
            return null;
        }
    }

    /**
     * @param endpoint
     */
    protected void advertiseEndpointDescription(EndpointDescription description) {
        registrationLock.lock();
        try {
            if (this.registrations.containsKey(description)) {
                return;
            }
            final ServiceMetadataFactory factory = serviceMetadataFactoryTracker.getService();
            if (factory != null) {
                ServiceMetadata metadata = factory.create(null, description);
                if (metadata != null) {
                    if (LOG.isTraceEnabled())
                        LOG.debug("advertising EndpointDescription "
                            + description);
                    final ServiceRegistration<ServiceMetadata> registerService = this.context.registerService(
                        ServiceMetadata.class, metadata, null);
                    this.registrations.put(description, registerService);
                } else {
                    LOG.error("ServiceMetadataFactory failed to convert EndpointDescription "
                        + description);
                }
            } else {
                LOG.error("No ServiceMetadataFactory service found");
            }
        } finally {
            registrationLock.unlock();
        }
    }

    protected void unadvertiseEndpointDescription(
        EndpointDescription endpointDescription) {
        this.registrationLock.lock();
        try {
            final ServiceRegistration<ServiceMetadata> serviceRegistration = this.registrations.remove(endpointDescription);
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                return;
            }
        } finally {
            this.registrationLock.unlock();
        }
        LOG.warn("Failed to unadvertise endpointDescription: "
            + endpointDescription + ". Seems it was never advertised.");
    }

    /**
     * @param defaultTopologyManager
     * @param endpoint
     */
    protected void handleOtherEndpointAdded(EndpointListener endpointListener,
        EndpointDescription endpoint) {
        notifyOtherEndpointListeners(endpointListener, endpoint, true);

    }

    protected void notifyOtherEndpointListeners(
        EndpointListener endpointListener, EndpointDescription description,
        boolean added) {
        ServiceReference<?>[] listeners = null;
        try {
            listeners = context.getServiceReferences(
                EndpointListener.class.getName(), "("
                    + EndpointListener.ENDPOINT_LISTENER_SCOPE + "=*)");
        } catch (InvalidSyntaxException doesNotHappen) {
            // Should never happen
        }
        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                EndpointListener listener = (EndpointListener) getContext().getService(
                    listeners[i]);
                if (listener != endpointListener) {
                    Object scope = listeners[i].getProperty(EndpointListener.ENDPOINT_LISTENER_SCOPE);
                    String matchedFilter = isInterested(scope, description);
                    if (matchedFilter != null) {
                        if (added)
                            listener.endpointAdded(description, matchedFilter);
                        else
                            listener.endpointRemoved(description, matchedFilter);
                    }
                }
            }
        }
    }

    private String isInterested(Object scopeobj, EndpointDescription description) {
        if (scopeobj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> scope = (List<String>) scopeobj;
            for (Iterator<String> it = scope.iterator(); it.hasNext();) {
                String filter = it.next();

                if (description.matches(filter)) {
                    return filter;
                }
            }
        } else if (scopeobj instanceof String[]) {
            String[] scope = (String[]) scopeobj;
            for (String filter : scope) {
                if (description.matches(filter)) {
                    return filter;
                }
            }
        } else if (scopeobj instanceof String) {
            StringTokenizer st = new StringTokenizer((String) scopeobj, " ");
            for (; st.hasMoreTokens();) {
                String filter = st.nextToken();
                if (description.matches(filter)) {
                    return filter;
                }
            }
        }
        return null;
    }

    /**
     * @param endpoint
     */
    protected void handleEndpointAdded(EndpointDescription endpoint) {
        getRemoteServiceAdmin().importService(endpoint);

    }

    protected RemoteServiceAdmin getRemoteServiceAdmin() {
        synchronized (remoteServiceAdminTrackerLock) {
            if (remoteServiceAdminTracker == null) {
                remoteServiceAdminTracker = new ServiceTracker<RemoteServiceAdmin, RemoteServiceAdmin>(
                    Activator.getContext(), createRemoteServiceAdminFilter(),
                    null);
                remoteServiceAdminTracker.open();
            }
        }
        return remoteServiceAdminTracker.getService();
    }

    /**
     * @return
     */
    private Filter createRemoteServiceAdminFilter() {
        String filterString = new StringBuilder().append("(&(").append(
            org.osgi.framework.Constants.OBJECTCLASS).append("=").append(
            RemoteServiceAdmin.class.getName()).append(")(").append(
            HolaRemoteServiceAdmin.RSA_SUPPORT_KEY).append("=*))").toString();
        try {
            return getContext().createFilter(filterString);
        } catch (InvalidSyntaxException doesNotHappen) {
            // Ignore
            doesNotHappen.printStackTrace();
            return null;
        }
    }

    /**
     * @return
     */
    protected Object getFrameworkUUID() {
        org.solmix.hola.osgi.internal.Activator a = org.solmix.hola.osgi.internal.Activator.getDefault();
        if (a == null)
            return null;
        return a.getFrameworkUUID();
    }

    protected void handleEvent(ServiceEvent event, Map listeners) {
        switch (event.getType()) {
            case ServiceEvent.MODIFIED:
                handleServiceModifying(event.getServiceReference());
                break;
            case ServiceEvent.REGISTERED:
                handleServiceRegistering(event.getServiceReference());
                break;
            default:
                break;
        }
    }

    /**
     * @param serviceReference
     */
    protected void handleServiceRegistering(ServiceReference<?> serviceReference) {
        // Using OSGI 4.2 Chap 13 Remote Services spec, get the specified remote
        // interfaces for the given service reference
        String[] exportedInterfaces = PropertiesUtil.getExportedInterfaces(serviceReference);
        // If no remote interfaces set, then we don't do anything with it
        if (exportedInterfaces == null)
            return;

        // prepare export properties
        Map<String, Object> exportProperties = new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER);
        exportProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
            exportedInterfaces);
        if (LOG.isTraceEnabled())
            LOG.trace("serviceReference=" + serviceReference
                + " exportProperties=" + exportProperties);
        // Do the export with RSA
        getRemoteServiceAdmin().exportService(serviceReference,
            exportProperties);

    }

    /**
     * @param serviceReference
     */
    private void handleServiceModifying(ServiceReference<?> serviceReference) {
        LOG.warn("serviceReference=" + serviceReference
            + " modified with no response");

    }

}
