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

package org.solmix.hola.osgi.topology;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月29日
 */

public class Activator implements BundleActivator
{

    private  BundleContext context;

    private static Activator instance;
    
    private DefaultTopologyManager topologyManager;
    private ServiceRegistration<?> endpointListenerRegistration;

    private DefaultTopologyComponent topologyComp;

    private ServiceRegistration<RemoteServiceAdminListener> eventAdminListenerRegistration;

    private ServiceRegistration<EventListenerHook> eventListenerHookRegistration;

    public  BundleContext getContext() {
        return context;
    }

    public static Activator getDefault() {
        return instance;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        Activator.instance = this;
        topologyManager=new DefaultTopologyManager(context);
        Dictionary<String,Object> properties=new Hashtable<String,Object>();
        properties.put(EndpointListener.ENDPOINT_LISTENER_SCOPE, topologyManager.getScope());
        endpointListenerRegistration= getContext().registerService(EndpointListener.class.getName(), topologyManager, properties);

        topologyComp=new DefaultTopologyComponent();
        topologyComp.bindManager(topologyManager);
        eventAdminListenerRegistration = this.context.registerService(
            RemoteServiceAdminListener.class, topologyComp,
            null);
        eventListenerHookRegistration = this.context.registerService(
            EventListenerHook.class, topologyComp, null);
        topologyComp.exportRegistedService();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if(eventListenerHookRegistration!=null){
            eventListenerHookRegistration.unregister();
            eventListenerHookRegistration=null;
        }
        if(topologyComp!=null){
            topologyComp.unbindManager(topologyManager);
            topologyComp=null;
        }
        if(endpointListenerRegistration!=null){
            endpointListenerRegistration.unregister();
            endpointListenerRegistration=null;
        }
        if(eventAdminListenerRegistration!=null){
            eventAdminListenerRegistration.unregister();
            eventAdminListenerRegistration=null;
        }
        if(topologyManager!=null){
            topologyManager.close();
            topologyManager=null;
        }
       this.context=null;

    }

}
