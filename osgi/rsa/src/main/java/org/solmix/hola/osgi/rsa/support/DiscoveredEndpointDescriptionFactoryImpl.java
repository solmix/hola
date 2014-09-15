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

package org.solmix.hola.osgi.rsa.support;

import java.util.List;

import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.DiscoveryLocator;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.osgi.rsa.AbstractMetadataFactory;
import org.solmix.hola.osgi.rsa.DiscoveredEndpointDescription;
import org.solmix.hola.osgi.rsa.DiscoveredEndpointDescriptionFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */

public class DiscoveredEndpointDescriptionFactoryImpl extends
    AbstractMetadataFactory implements
    org.solmix.hola.osgi.rsa.DiscoveredEndpointDescriptionFactory
{

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveredEndpointDescriptionFactory.class.getName());

    protected List<DiscoveredEndpointDescription> discoveredEndpointDescriptions = new java.util.concurrent.CopyOnWriteArrayList<DiscoveredEndpointDescription>();

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.DiscoveredEndpointDescriptionFactory#create(org.solmix.hola.discovery.DiscoveryLocator,
     *      org.solmix.hola.discovery.ServiceMetadata)
     */
    @Override
    public DiscoveredEndpointDescription create(DiscoveryLocator locator,
        ServiceMetadata metadata) {
        try {
            EndpointDescription ed = transform(metadata.getServiceProperties());
            DiscoveredEndpointDescription ded = find(ed);
            if (ded != null)
                return ded;
            else {
                ded = createDiscovered(locator, metadata, ed);
                discoveredEndpointDescriptions.add(ded);
                return ded;
            }
        } catch (Exception e) {
            LOG.error("Exception creating discovered endpoint description", e);
            return null;
        }
    }

    private DiscoveredEndpointDescription createDiscovered(
        DiscoveryLocator locator, ServiceMetadata metadata,
        EndpointDescription ed) {
        return new DiscoveredEndpointDescription(
            locator.getNamespace(), metadata.getServiceID(), ed);
    }

    private DiscoveredEndpointDescription find(
        EndpointDescription endpointDescription) {
        for (DiscoveredEndpointDescription d : discoveredEndpointDescriptions) {
            EndpointDescription ed = d.getEndpointDescription();
            if (ed.equals(endpointDescription))
                return d;
        }
        return null;
    }

    private DiscoveredEndpointDescription findNoDiscovery(
        DiscoveryLocator locator, ServiceID serviceID) {
        for (DiscoveredEndpointDescription d : discoveredEndpointDescriptions) {
            Namespace dln = d.getDiscoveryLocatorNamespace();
            ServiceID svcId = d.getServiceID();
            if (dln.getName().equals(locator.getNamespace().getName())
                && svcId.equals(serviceID)) {
                return d;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.DiscoveredEndpointDescriptionFactory#remove(org.solmix.hola.discovery.DiscoveryLocator,
     *      org.solmix.hola.discovery.ServiceID)
     */
    @Override
    public DiscoveredEndpointDescription remove(DiscoveryLocator locator,
        ServiceID serviceID) {
        DiscoveredEndpointDescription ued = findNoDiscovery(locator, serviceID);
        if (ued != null) {
            discoveredEndpointDescriptions.remove(ued);
            return ued;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.DiscoveredEndpointDescriptionFactory#remove(org.osgi.service.remoteserviceadmin.EndpointDescription)
     */
    @Override
    public boolean remove(EndpointDescription endpointDescription) {
        DiscoveredEndpointDescription ded = find(endpointDescription);
        if (ded != null) {
            discoveredEndpointDescriptions.remove(ded);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.DiscoveredEndpointDescriptionFactory#removeAll()
     */
    @Override
    public void removeAll() {
        discoveredEndpointDescriptions.clear();
    }

    @Override
    public void close() {
        removeAll();
        super.close();
    }
}
