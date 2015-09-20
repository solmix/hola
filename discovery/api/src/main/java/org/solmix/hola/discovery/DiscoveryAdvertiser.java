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
package org.solmix.hola.discovery;

import org.solmix.hola.discovery.model.ServiceInfo;
import org.solmix.runtime.identity.Namespace;



/**
 * 服务公告
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年4月5日
 */

public interface DiscoveryAdvertiser
{

    /**
     * Register the given service. This publishes the service defined by the
     * serviceInfo to the underlying publishing mechanism
     * 
     * @param serviceInfo
     *            IServiceInfo of the service to be published. Must not be
     *            <code>null</code>.
     */
    public void register(ServiceInfo serviceInfo);

    /**
     * Unregister a previously registered service defined by serviceInfo.
     * 
     * @param serviceInfo
     *            IServiceInfo defining the service to unregister. Must not be
     *            <code>null</code>.
     */
    public void unregister(ServiceInfo serviceInfo);

    /**
     * Unregister all previously registered service.
     */
    public void unregisterAll();

    /**
     * Get a Namespace for services associated with this discovery container
     * adapter. The given Namespace may be used via IServiceIDFactory to create
     * IServiceIDs rather than simple IDs. For example:
     * 
     * <pre>
     * IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(
     *          container.getServicesNamespace(), serviceType, serviceName);
     * </pre>
     * 
     * @return Namespace for creating service IDs. Will not be <code>null</code>
     *         .
     */
    public Namespace getNamespace();
}
