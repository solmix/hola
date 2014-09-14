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


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.identity.DiscoveryNamespace;
import org.solmix.runtime.adapter.AdapterManager;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月4日
 */

public class Activator implements BundleActivator
{
    private BundleContext context;
    private static Activator plugin;
    private ServiceTracker<AdapterManager,AdapterManager> adapterManagerTracker = null;
    public Activator(){
        super();
        plugin=this;
    }
    public static final Activator getDefault(){
        if(plugin==null)
            plugin=new Activator();
        return plugin;
    }
    
    
    /**
     * @return the context
     */
    public BundleContext getBundleContext() {
        return context;
    }

    private ServiceRegistration<?> namespaceRegistration;
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
       this.context=context;
       namespaceRegistration=  this.context.registerService(Namespace.class.getName(), new DiscoveryNamespace(
           "Discovery Namespace"), null);

    }
    
    public AdapterManager getAdapterManager(){
        if(context==null)
            return null;
        if (adapterManagerTracker == null) {
            adapterManagerTracker = new ServiceTracker<AdapterManager,AdapterManager>(context, AdapterManager.class, null);
            adapterManagerTracker.open();
      }
      return adapterManagerTracker.getService();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if(namespaceRegistration!=null)
            namespaceRegistration.unregister();
        this.context=null;
        if(adapterManagerTracker!=null)
            adapterManagerTracker.close();

    }

}
