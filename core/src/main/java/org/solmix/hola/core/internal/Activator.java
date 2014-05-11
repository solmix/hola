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
package org.solmix.hola.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.hola.core.identity.Namespace;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月2日
 */

public class Activator implements BundleActivator
{
    private BundleContext bundleContext;
    private ServiceRegistration<IDFactory> idFactoryRegistration;
    private ServiceTracker<Namespace,Namespace> namespacesTracker;
    private IDFactory factory;
    private static final Logger LOG=  LoggerFactory.getLogger(Activator.class);
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.bundleContext=context;
        factory= DefaultIDFactory.getDefault();
        idFactoryRegistration=  context.registerService(IDFactory.class, factory, null);
        namespacesTracker = new ServiceTracker<Namespace,Namespace>(context,
            Namespace.class.getName(), new ServiceTrackerCustomizer<Namespace,Namespace>() {

                @Override
                public Namespace addingService(
                    ServiceReference<Namespace> reference) {
                    Namespace ns=  bundleContext.getService(reference);
                    if(ns!=null&&ns.getName()!=null){
                        if(LOG.isInfoEnabled()){
                            LOG.info("Add Namespace: "+ns);
                        }
                        factory.addNamespace(ns);
                    }
                        
                    return ns;
                }

                @Override
                public void modifiedService(
                    ServiceReference<Namespace> reference, Namespace service) {
                }

                @Override
                public void removedService(
                    ServiceReference<Namespace> reference, Namespace service) {
                    if(LOG.isInfoEnabled()){
                        LOG.info("Remove Namespace: "+service);
                    }
                   factory.removeNamespace(service);
                    
                }
        });
        namespacesTracker.open();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if(idFactoryRegistration!=null)
            idFactoryRegistration.unregister();
        if(namespacesTracker!=null)
            namespacesTracker.close();
       
        bundleContext=null;
    }

}
