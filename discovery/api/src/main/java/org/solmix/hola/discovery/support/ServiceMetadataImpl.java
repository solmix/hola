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

package org.solmix.hola.discovery.support;

import java.net.URI;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.ServiceInfo;
import org.solmix.hola.discovery.ServiceProperties;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */

public class ServiceMetadataImpl implements ServiceInfo
{

    private static final long serialVersionUID = 3885770619029848938L;

    public static final long DEFAULT_TTL = 3600; // 1h

    public static final int DEFAULT_PRIORITY = 0;

    public static final int DEFAULT_WEIGHT = 0;

    public static final String UNKNOWN_PROTOCOL = "unknown";

    protected String serviceName;

    protected ServiceID serviceID;

    protected int priority;

    protected int weight;

    protected ServiceProperties properties;

    protected long timeToLive;

    public ServiceMetadataImpl()
    {
    }

    /**
     * @param uri
     * @param serviceName
     * @param type
     * @param serviceProperties
     */
    public ServiceMetadataImpl(URI uri, String serviceName, ServiceType type,
        ServiceProperties serviceProperties)
    {
    }
    public ServiceMetadataImpl(URI uri, String serviceName, ServiceType type, int priority,
        int weight, ServiceProperties props, long ttl) {
        Assert.isNotNull(uri);
        Assert.isNotNull(serviceName);
        Assert.isNotNull(type);
        
        String scheme = uri.getScheme();
        if (scheme == null) {
              scheme = UNKNOWN_PROTOCOL;
        }

        // UserInfo
        String userInfo = uri.getUserInfo();
        if (userInfo == null) {
              userInfo = "";
        } else {
              userInfo += "@";
        }

        // Host
        String host = uri.getHost();
        Assert.isNotNull(host);

        // Port
        int port = uri.getPort();
        if (port == -1) {
              port = 0;
        }

        // Path
        String path = uri.getPath();
        if (path == null) {
              path = "/";
        }

        // query
        String query = uri.getQuery();
        if (query == null) {
              query = "";
        } else {
              query = "?" + query;
        }

        // fragment
        String fragment = uri.getFragment();
        if (fragment == null) {
              fragment = "";
        } else {
              fragment = "#" + fragment;
        }
        URI u = URI.create(scheme + "://" + userInfo + host + ":" + port + path + query + fragment);
        
        // service id
        Namespace ns = type.getNamespace();
        this.serviceID = (ServiceID) ns.createID((new Object[]{type, u}));
        ((ServiceIDImpl)serviceID).setServiceMetadata(this);
        
        this.serviceName = serviceName;
        
        this.weight = weight;
        this.priority = priority;
        
        properties = (props == null) ? new ServicePropertiesImpl() : props;
        
        this.timeToLive = ttl;
        
    }
    
    public ServiceMetadataImpl(URI location, String serviceName,
        ServiceType serviceType, int priority, int weight,
        ServiceProperties serviceProperties)
    {
        this(location,serviceName,serviceType,priority,weight,serviceProperties,DEFAULT_TTL);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getLocation()
     */
    @Override
    public URI getLocation() {
        return serviceID.getLocation();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getServiceID()
     */
    @Override
    public ServiceID getServiceID() {
        return serviceID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getPriority()
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getWeight()
     */
    @Override
    public int getWeight() {
        return weight;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getTTL()
     */
    @Override
    public long getTTL() {
        return timeToLive;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getServiceProperties()
     */
    @Override
    public ServiceProperties getServiceProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceInfo#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

}
