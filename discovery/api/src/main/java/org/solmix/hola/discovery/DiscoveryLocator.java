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

import java.util.concurrent.Future;

import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;

/**
 * 服务定位
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月5日
 */

public interface DiscoveryLocator
{

    /**
     * Synchronously retrieve metadata about the service
     * 
     * @param service IServiceID of the service to get info about. Must not be
     *        <code>null</code>.
     * @return IServiceInfo the service info retrieved. <code>null</code> if no
     *         information retrievable.
     */
    public ServiceInfo getService(ServiceID aServiceID);

    /**
     * Synchronously get service info about all known services
     * 
     * @return IServiceInfo[] the resulting array of service info instances.
     *         Will not be <code>null</code>. May be of length 0.
     */
    public ServiceInfo[] getServices();

    /**
     * Synchronously get service info about all known services of given service
     * type
     * 
     * @param type IServiceTypeID defining the type of service we are interested
     *        in getting service info about. Must not be <code>null</code>
     * @return IServiceInfo[] the resulting array of service info instances.
     *         Will not be <code>null</code>. May be of length 0.
     */
    public ServiceInfo[] getServices(ServiceType type);

    /**
     * Synchronously get service info about all known services of given service
     * type
     * 
     * @return IServiceTypeID[] the resulting array of service type IDs. Will
     *         not be <code>null</code>. May be of length 0.
     */
    public ServiceType[] getServiceTypes();

    /**
     * Get a Namespace for services associated with this discovery container
     * adapter. The given Namespace may be used via IServiceIDFactory to create
     * IServiceIDs rather than simple IDs. For example:
     * 
     * <pre>
     * 
     * IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(
     *     container.getServicesNamespace(), serviceType, serviceName);
     * </pre>
     * 
     * @return Namespace for creating service IDs. Will not be <code>null</code>
     *         .
     */
    public Namespace getNamespace();

    /**
     * Purges the underlying IServiceInfo cache if available in the current
     * provider
     * 
     * @return The previous cache content
     */
    public ServiceInfo[] purgeCache();

    /* Listener related API */

    /**
     * Add a service listener. The given listener will have its method called
     * when a service is discovered.
     * 
     * @param listener IServiceListener to be notified. Must not be
     *        <code>null</code> .
     */
    public void addServiceListener(ServiceListener listener);

    /**
     * Add a service listener. The given listener will have its method called
     * when a service with a type matching that specified by the first parameter
     * is discovered.
     * 
     * @param type String type to listen for. Must not be <code>null</code>.
     *        Must be formatted according to this specific IDiscoveryContainer
     * @param listener IServiceListener to be notified. Must not be
     *        <code>null</code> .
     */
    public void addServiceListener(ServiceType type, ServiceListener listener);

    /**
     * Add a service type listener. The given listener will have its method
     * called when a service type is discovered.
     * 
     * @param listener the listener to be notified. Must not be
     *        <code>null</code>.
     */
    public void addServiceTypeListener(ServiceTypeListener listener);

    /**
     * Remove a service listener. Remove the listener from this container
     * 
     * @param listener IServiceListener listener to be removed. Must not be
     *        <code>null</code>.
     */
    public void removeServiceListener(ServiceListener listener);

    /**
     * Remove a service listener. Remove the listener associated with the type
     * specified by the first parameter.
     * 
     * @param type String of the desired type to remove the listener. Must not
     *        be <code>null</code>. Must be formatted according to this specific
     *        IDiscoveryContainer
     * @param listener IServiceListener listener to be removed. Must not be
     *        <code>null</code>.
     */
    public void removeServiceListener(ServiceType type, ServiceListener listener);

    /**
     * Remove a service type listener. Remove the type listener.
     * 
     * @param listener IServiceTypeListener to be removed. Must not be
     *        <code>null</code>.
     */
    public void removeServiceTypeListener(ServiceTypeListener listener);

    /* Future related API */

    /**
     * Asynchronously retrieve info about the service
     * 
     * @param service IServiceID of the service to get info about. Must not be
     *        <code>null</code>.
     * @return IFuture a future status wrapping an IServiceInfo or
     *         <code>null</code> if no information retrievable.
     */
    public Future<ServiceInfo> getAsyncService(ServiceID aServiceID);

    /**
     * Asynchronously get service info about all known services
     * 
     * @return IFuture wrapping an IServiceTypeID[]. The resulting array of
     *         service type IDs will not be <code>null</code>. May be of length
     *         0.
     */
    public Future<ServiceInfo[]> getAsyncServices();

    /**
     * Asynchronously get service info about all known services of given service
     * type
     * 
     * @param type IServiceTypeID defining the type of service we are interested
     *        in getting service info about. Must not be <code>null</code>
     * @return IFuture wrapping an IServiceTypeID[]. The resulting array of
     *         service type IDs will not be <code>null</code>. May be of length
     *         0.
     */
    public Future<ServiceInfo[]> getAsyncServices(ServiceType aServiceTypeID);

    /**
     * Asynchronously get service info about all known services of given service
     * type
     * 
     * @return IFuture wrapping an IServiceTypeID[]. The resulting array of
     *         service type IDs will not be <code>null</code>. May be of length
     *         0.
     */
    public Future<ServiceType[]> getAsyncServiceTypes();
}
