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
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryAdvertiser;
import org.solmix.hola.discovery.identity.DiscoveryID;
import org.solmix.hola.discovery.identity.DiscoveryType;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.runtime.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月5日
 */

public class ServiceMetadataTracker
{

    private ServiceTracker<DiscoveryInfo, DiscoveryInfo> serviceTracker;

    public ServiceMetadataTracker(final Discovery provider)
    {
        final BundleContext bundleContext = Activator.getDefault().getBundleContext();
        if (bundleContext != null) {
            serviceTracker = new ServiceTracker<DiscoveryInfo, DiscoveryInfo>(bundleContext, DiscoveryInfo.class,
                new ServiceTrackerCustomizer<DiscoveryInfo, DiscoveryInfo>() {

                    @Override
                    public DiscoveryInfo addingService(ServiceReference<DiscoveryInfo> reference) {
                        final DiscoveryInfo serviceInfo = bundleContext.getService(reference);
                        final DiscoveryInfo specific = convertToProviderSpecific(provider, serviceInfo);
                        provider.register(specific);
                        return serviceInfo;
                    }

                    @Override
                    public void modifiedService(ServiceReference<DiscoveryInfo> reference, DiscoveryInfo service) {
                        // TODO discovery containers might require to
                        // unregisterService first
                        provider.register(convertToProviderSpecific(provider, service));
                    }

                    @Override
                    public void removedService(ServiceReference<DiscoveryInfo> reference, DiscoveryInfo service) {
                        provider.unregister(convertToProviderSpecific(provider, service));
                    }
                });
            serviceTracker.open();
        }
    }

    private DiscoveryInfo convertToProviderSpecific(final DiscoveryAdvertiser advertiser, final DiscoveryInfo genericMeta) {

        final Namespace servicesNamespace = advertiser.getNamespace();

        final DiscoveryID genericServiceID = genericMeta.getServiceID();
        final DiscoveryID specificServiceID = (DiscoveryID) servicesNamespace.createID(
            new Object[] { genericServiceID.getServiceType().getName(), genericServiceID.getName()});

        final DiscoveryType serviceType = specificServiceID.getServiceType();

        return new DiscoveryInfoImpl( genericMeta.getServiceName(), serviceType, genericMeta.getPriority(),
            genericMeta.getWeight(), genericMeta.getServiceProperties());
    }

    public void destroy() {
        if (serviceTracker != null)
            serviceTracker.close();
    }
}
