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

import java.util.Collection;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.osgi.rsa.AbstractTopologyManager;
import org.solmix.hola.osgi.rsa.HolaEndpointDescription;
import org.solmix.hola.osgi.rsa.HolaRemoteConstants;
import org.solmix.hola.osgi.rsa.HolaRemoteServiceAdminEvent;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月29日
 */

public class DefaultTopologyManager extends AbstractTopologyManager implements
    EndpointListener
{

    private final String endpointListenerScope;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTopologyManager.class);

    private static final String ONLY_ECF_SCOPE = "("
        + HolaRemoteConstants.ENDPOINT_NAMESPACE + "=*)";

    private static final String NO_ECF_SCOPE = "(!("
        + HolaRemoteConstants.ENDPOINT_NAMESPACE + "=*))";

    public DefaultTopologyManager(BundleContext context)
    {
        super(context);

        StringBuffer elScope = new StringBuffer("");
        // filter so that local framework uuid is not the same as local
        // value
        elScope.append("(&(!(").append(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID).append(
            "=").append(getFrameworkUUID()).append("))");
        elScope.append(ONLY_ECF_SCOPE);
        elScope.append(")");
        endpointListenerScope = elScope.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.EndpointListener#endpointAdded(org.osgi.service.remoteserviceadmin.EndpointDescription,
     *      java.lang.String)
     */
    @Override
    public void endpointAdded(EndpointDescription endpoint, String matchedFilter) {
        if (matchedFilter.equals(endpointListenerScope))
            if (endpoint instanceof HolaEndpointDescription)
                handleEndpointAdded(endpoint);
            else
                handleOtherEndpointAdded(this, endpoint);
        else if (matchedFilter.equals(NO_ECF_SCOPE))
            if (endpoint instanceof HolaEndpointDescription)
                handleEndpointAdded(endpoint);
            else
                advertiseEndpointDescription(endpoint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.EndpointListener#endpointRemoved(org.osgi.service.remoteserviceadmin.EndpointDescription,
     *      java.lang.String)
     */
    @Override
    public void endpointRemoved(EndpointDescription endpoint,
        String matchedFilter) {
        if (matchedFilter.equals(endpointListenerScope))
            if (endpoint instanceof HolaEndpointDescription)
                handleEndpointRemoved(endpoint);
            else
                handleOtherEndpointRemoved(this, endpoint);
        else if (matchedFilter.equals(NO_ECF_SCOPE))
            if (endpoint instanceof HolaEndpointDescription)
                handleEndpointRemoved(endpoint);
            else
                unadvertiseEndpointDescription(endpoint);
    }

    String[] getScope() {
        return new String[] { endpointListenerScope, NO_ECF_SCOPE };
    }

    /**
     * @param event
     */
    public void handleRemoteAdminEvent(RemoteServiceAdminEvent event) {
        if (!(event instanceof HolaRemoteServiceAdminEvent))
            return;
        HolaRemoteServiceAdminEvent rsaEvent = (HolaRemoteServiceAdminEvent) event;
        int eventType = event.getType();
        EndpointDescription endpointDescription = rsaEvent.getEndpointDescription();

        boolean disableDiscovery = false;
        if (disableDiscovery) {
            return;
        }

        switch (eventType) {
            case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
                advertiseEndpointDescription(endpointDescription);
                break;
            case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
                unadvertiseEndpointDescription(endpointDescription);
                break;
            case RemoteServiceAdminEvent.EXPORT_ERROR:
                LOG.error("Export error with event=" + rsaEvent);
                break;
            case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
                break;
            case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
                break;
            case RemoteServiceAdminEvent.IMPORT_ERROR:
                break;
        }
    }

    /**
     * @param event
     * @param listeners
     */
    public void handle(ServiceEvent event,
        Map<BundleContext, Collection<ListenerInfo>> listeners) {
        handleEvent(event, listeners);
    }

    /**
     * @param object
     * @param string
     */
    public void exportRegisteredServices(String className, String filterString) {
        try {
            // 如果className为空,返回所有带filter的服务
            final ServiceReference<?>[] existingServiceRefs = getContext().getAllServiceReferences(
                className, filterString);
            if(existingServiceRefs!=null&&existingServiceRefs.length>0){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                          for (int i = 0; i < existingServiceRefs.length; i++) {
                                // This method will check the service properties for
                                // remote service props. If previously registered as a
                                // remote service, it will export the remote
                                // service if not it will simply return/skip
                                handleServiceRegistering(existingServiceRefs[i]);
                          }
                    }
              }, "BasicTopologyManagerPreRegSrvExporter").start(); 
            }
            
        } catch (InvalidSyntaxException e) {
            LOG.error(
                "Could not retrieve existing service references for exportRegisteredSvcsClassname=" //$NON-NLS-1$
                    + className
                    + " and exportRegisteredSvcsFilter="
                    + filterString, e);
        }

    }

}
