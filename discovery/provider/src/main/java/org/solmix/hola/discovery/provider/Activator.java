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

import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.DiscoveryAdvertiser;
import org.solmix.hola.discovery.DiscoveryLocator;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年6月7日
 */

public class Activator implements BundleActivator
{

    private static Activator plugin;

    private ServiceRegistration<?> discoveryRegistration;

    private ServiceRegistration<?> namespaceRegistration;

    public Activator()
    {
        plugin = this;
    }

    public static Activator getDefault() {
        return plugin;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        final Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(DiscoveryLocator.PROVIDER_NAME, CompositeNamespace.NAME);
        //较高的权重,被优先选取
        props.put(Constants.SERVICE_RANKING, new Integer(1000));
        String[] clazzes = new String[] { DiscoveryLocator.class.getName(),
            DiscoveryAdvertiser.class.getName() };
        namespaceRegistration = context.registerService(
            Namespace.class.getName(),
            new CompositeNamespace("Composite Namespace"), null);

        discoveryRegistration = context.registerService(clazzes,
            new ServiceFactory<CompositeDiscoveryProvider>() {

                @Override
                public CompositeDiscoveryProvider getService(Bundle bundle,
                    ServiceRegistration<CompositeDiscoveryProvider> registration) {
                    final CompositeDiscoveryProvider composite=new CompositeDiscoveryProvider();
                    try {
                        composite.connect(null, null);
                    } catch (ConnectException e) {
                        return null;
                    }
                    Filter filter = null;
                    try {
                        final String filter2 = "(&(" + Constants.OBJECTCLASS + "=" + DiscoveryAdvertiser.class.getName() + ")(!(" + DiscoveryLocator.PROVIDER_NAME + "=" + CompositeNamespace.NAME + ")))"; 
                        filter = context.createFilter(filter2);
                        context.addServiceListener(new ServiceListener() {
                              /* (non-Javadoc)
                               * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
                               */
                              @Override
                            public void serviceChanged(final ServiceEvent arg0) {
                                    final Object anIDS = context.getService(arg0.getServiceReference());
                                    switch (arg0.getType()) {
                                          case ServiceEvent.REGISTERED :
                                              composite.addProvider(anIDS);
                                                break;
                                          case ServiceEvent.UNREGISTERING :
                                              composite.removeProvider(anIDS);
                                                break;
                                          default :
                                                break;
                                    }
                              }

                        }, filter2);
                  } catch (final InvalidSyntaxException e) {
                        // nop
                  }
                 // get all previously registered IDS from OSGi (but not this one)
                    final ServiceTracker<?,?> tracker = new ServiceTracker<Object,Object>(context, filter, null);
                    tracker.open();
                    final Object[] services = tracker.getServices();
                    tracker.close();
                    if (services != null) {
                          for (int i = 0; i < services.length; i++) {
                                final Object obj = services[i];
                                if (obj != composite)
                                    composite.addProvider(obj);
                          }
                    }

                    return composite;
                }

                @Override
                public void ungetService(Bundle bundle,
                    ServiceRegistration<CompositeDiscoveryProvider> registration, CompositeDiscoveryProvider service) {
                    service.destroy();
                }

            }, props);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (discoveryRegistration != null) {
            discoveryRegistration.unregister();
            discoveryRegistration = null;
        }
        if(namespaceRegistration!=null){
            namespaceRegistration.unregister();
            namespaceRegistration=null;
        }

    }

}
