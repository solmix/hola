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

package org.solmix.hola.discovery.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.identity.DefaultServiceTypeFactory;
import org.solmix.hola.discovery.identity.DiscoveryNamespace;
import org.solmix.hola.discovery.identity.DiscoveryType;
import org.solmix.hola.discovery.identity.DiscoveryTypeFactory;
import org.solmix.runtime.identity.IDCreateException;
import org.solmix.runtime.identity.IDFactory;
import org.solmix.runtime.identity.Namespace;

/**
 * 监听OSGI环境中注册的{@link org.solmix.hola.discovery.DiscoveryListener},并自动添加listener. 只Spring如需添加需手动代码实现
 */

public class DiscoveryServiceListener implements ServiceListener
{

    private final Discovery protocol;

    private final DiscoveryTypeFactory typeFactory;

    private final BundleContext context;

    private Namespace discoveryNamespace;

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryServiceListener.class);

    public DiscoveryServiceListener(Discovery protocol)
    {
        this.protocol = protocol;
        typeFactory = DefaultServiceTypeFactory.getDefault();
        context = Activator.getDefault().getBundleContext();
        // OSGI下查找org.solmix.hola.discovery.ServiceListener
        if (context != null) {
            discoveryNamespace = IDFactory.getDefault().getNamespaceByName(DiscoveryNamespace.NAME);
            try {
                final ServiceReference<?>[] references = context.getServiceReferences(org.solmix.hola.discovery.DiscoveryListener.class.getName(),
                    null);
                addServiceListener(references);
                context.addServiceListener(this, getFilter());
            } catch (InvalidSyntaxException e) {
                LOG.error("Cannot create filter", e);
            }
        } else {// 不做任何处理

        }
    }

    /**
     * @param references
     */
    private void addServiceListener(ServiceReference<?>[] references) {
        if (references == null) {
            return;
        }
        for (ServiceReference<?> refer : references) {
            if (isAllWildcards(refer)) {
                final org.solmix.hola.discovery.DiscoveryListener listener = (org.solmix.hola.discovery.DiscoveryListener) context.getService(refer);
                protocol.addServiceListener(listener);
            } else {
                final DiscoveryType type = getServiceType(refer);
                if (type == null)
                    continue;
                final org.solmix.hola.discovery.DiscoveryListener listener = (org.solmix.hola.discovery.DiscoveryListener) context.getService(refer);
                protocol.addServiceListener(type, listener);
            }
        }

    }

    /**
     * @param refer
     * @return
     */
    private DiscoveryType getServiceType(ServiceReference<?> refer) {
      
        try {
            final DiscoveryType createServiceTypeID = typeFactory.create(discoveryNamespace, convert(refer, "discovery.group"),
                convert(refer, "discovery.serviceInterface"), convert(refer, "discovery.categery"));
            return createServiceTypeID;
        } catch (final IDCreateException e) {
            return null;
        }
    }

    private String convert(ServiceReference<?> serviceReference, String key) {
        final Object value = serviceReference.getProperty(key);
        // default to wildcard for non-set values
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    private boolean isAllWildcards(ServiceReference<?> serviceReference) {
        return serviceReference.getProperty("discovery.namingauthority") == null && serviceReference.getProperty("discovery.services") == null
            && serviceReference.getProperty("discovery.scopes") == null && serviceReference.getProperty("discovery.protocols") == null;
    }

    /**
     * @return
     */
    protected String getFilter() {
        return "(" + Constants.OBJECTCLASS + "=" + org.solmix.hola.discovery.DiscoveryListener.class.getName() + ")";
    }

    private void removeServiceListener(ServiceReference<?>[] references) {
        if (references == null) {
            return;
        }
        for (ServiceReference<?> refer : references) {
            if (isAllWildcards(refer)) {
                final org.solmix.hola.discovery.DiscoveryListener listener = (org.solmix.hola.discovery.DiscoveryListener) context.getService(refer);
                protocol.removeServiceListener(listener);
            } else {
                final DiscoveryType type = getServiceType(refer);
                if (type == null)
                    continue;
                final org.solmix.hola.discovery.DiscoveryListener listener = (org.solmix.hola.discovery.DiscoveryListener) context.getService(refer);
                protocol.removeServiceListener(type, listener);
            }
        }

    }

    private void removeServiceListener(ServiceReference<?> reference) {
        removeServiceListener(new ServiceReference[] { reference });
    }

    private void addServiceListener(ServiceReference<?> reference) {
        addServiceListener(new ServiceReference[] { reference });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        Object service = context.getService(event.getServiceReference());
        if (service instanceof org.solmix.hola.discovery.DiscoveryListener) {
            switch (event.getType()) {
                case ServiceEvent.REGISTERED:
                    addServiceListener(event.getServiceReference());
                    break;
                case ServiceEvent.UNREGISTERING:
                    removeServiceListener(event.getServiceReference());
                    break;
                default:
                    break;
            }
        }

    }

    public void destroy() {
        if (context != null)
            context.removeServiceListener(this);
    }

}
