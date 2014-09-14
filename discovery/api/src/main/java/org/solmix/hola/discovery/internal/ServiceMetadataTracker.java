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
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.DiscoveryAdvertiser;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.ServiceMetadataImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月5日
 */

public class ServiceMetadataTracker
{

    private ServiceTracker<ServiceMetadata,ServiceMetadata> serviceTracker;

    public ServiceMetadataTracker(final Discovery provider)
    {
        final BundleContext bundleContext = Activator.getDefault().getBundleContext();
        if (bundleContext != null) {
            serviceTracker = new ServiceTracker<ServiceMetadata, ServiceMetadata>(
                bundleContext,
                ServiceMetadata.class,
                new ServiceTrackerCustomizer<ServiceMetadata, ServiceMetadata>() {

                    @Override
                    public ServiceMetadata addingService(
                        ServiceReference<ServiceMetadata> reference) {
                        final ServiceMetadata serviceInfo = bundleContext.getService(reference);
                        final ServiceMetadata specific = convertToProviderSpecific(
                            provider, serviceInfo);
                        provider.registerService(specific);
                        return serviceInfo;
                    }

                    @Override
                    public void modifiedService(ServiceReference<ServiceMetadata> reference,
                        ServiceMetadata service) {
                        // TODO discovery containers might require to
                        // unregisterService first
                        provider.registerService(convertToProviderSpecific(
                            provider, service));
                    }

                    @Override
                    public void removedService(ServiceReference<ServiceMetadata> reference,
                        ServiceMetadata service) {
                        provider.unregisterService(convertToProviderSpecific(
                            provider, service));
                    }
                });
            serviceTracker.open();
        }
    }
    private ServiceMetadata convertToProviderSpecific(
        final DiscoveryAdvertiser advertiser,
        final ServiceMetadata genericMeta) {

  final Namespace servicesNamespace = advertiser.getServicesNamespace();

  final ServiceID genericServiceID = genericMeta.getServiceID();
  final ServiceID specificServiceID = (ServiceID) servicesNamespace
              .createID(new Object[] {
                          genericServiceID.getServiceType().getName(),
                          genericServiceID.getLocation() });

  final ServiceType serviceType = specificServiceID
              .getServiceType();

  return new ServiceMetadataImpl(genericServiceID.getLocation(),
      genericMeta.getServiceName(), serviceType,
      genericMeta.getPriority(), genericMeta.getWeight(),
      genericMeta.getServiceProperties());
}

public void destroy() {
    if(serviceTracker!=null)
        serviceTracker.close();
}
}
