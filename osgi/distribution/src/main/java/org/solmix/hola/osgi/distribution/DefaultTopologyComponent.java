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

package org.solmix.hola.osgi.distribution;

import java.util.Collection;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月29日
 */

public class DefaultTopologyComponent implements EventListenerHook,
    RemoteServiceAdminListener
{

    private DefaultTopologyManager defaultManager;
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener#remoteAdminEvent(org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent)
     */
    @Override
    public void remoteAdminEvent(RemoteServiceAdminEvent event) {
        defaultManager.handleRemoteAdminEvent(event);

    }
    void activate() {
//        defaultManager.exportRegisteredServices(
//                          exportRegisteredSvcsClassname, exportRegisteredSvcsFilter);
  }
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.hooks.service.EventListenerHook#event(org.osgi.framework.ServiceEvent,
     *      java.util.Map)
     */
    @Override
    public void event(ServiceEvent event,
        Map<BundleContext, Collection<ListenerInfo>> listeners) {
        defaultManager.event(event, listeners);

    }
    
    
    public void bindManager(EndpointListener listener){
        if(listener instanceof DefaultTopologyManager)
            defaultManager=(DefaultTopologyManager)listener;
    }
    
    public void unbindManager(EndpointListener listener){
        if(listener instanceof DefaultTopologyManager)
            defaultManager=null;
    }

}
