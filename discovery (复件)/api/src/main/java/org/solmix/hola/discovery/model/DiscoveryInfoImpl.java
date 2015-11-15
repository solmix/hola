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

package org.solmix.hola.discovery.model;

import java.util.Dictionary;

import org.solmix.commons.util.Assert;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.identity.DiscoveryID;
import org.solmix.hola.discovery.identity.DiscoveryNamespace;
import org.solmix.hola.discovery.identity.DiscoveryType;
import org.solmix.hola.discovery.identity.DiscoveryTypeImpl;
import org.solmix.runtime.identity.IDFactory;
import org.solmix.runtime.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */

public class DiscoveryInfoImpl implements DiscoveryInfo
{

    private static final long serialVersionUID = 3885770619029848938L;

    public static final long DEFAULT_TTL = 3600; // 1h

    public static final int DEFAULT_PRIORITY = 0;

    public static final int DEFAULT_WEIGHT = 0;

    public static final String UNKNOWN_PROTOCOL = "unknown";

    protected String serviceName;

    protected DiscoveryID serviceID;

    protected int priority;

    protected int weight;

    protected Dictionary<String, ?> properties;

    protected long timeToLive;

    public DiscoveryInfoImpl(Dictionary<String, ?> properties)
    {
        String service = PropertiesUtils.getServiceInterface(properties);
        String group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY, DiscoveryType.DEFAULT_GROUP);
        String category = PropertiesUtils.getString(properties, HOLA.CATEGORY_KEY, HOLA.DEFAULT_CATEGORY);
        DiscoveryTypeImpl type = new DiscoveryTypeImpl(IDFactory.getDefault().getNamespaceByName(DiscoveryNamespace.NAME), service, group, category);
        int weight = PropertiesUtils.getInt(properties, HOLA.WEIGHT_KEY, HOLA.DEFAULT_WEIGHT);
        int priority = PropertiesUtils.getInt(properties, HOLA.PRIORITY_KEY, HOLA.DEFAULT_PRIORITY);
        long ttl = PropertiesUtils.getLong(properties, HOLA.TTL_KEY, -1);
        this.serviceID = (DiscoveryID) type.getNamespace().createID((new Object[] { type, this }));
        this.serviceName = type.getName();

        this.weight = weight;
        this.priority = priority;

        this.properties = properties;

        this.timeToLive = ttl;
    }

    public DiscoveryInfoImpl(String serviceName, DiscoveryType type, int priority, int weight, ServiceProperties props, long ttl)
    {
        Assert.isNotNull(serviceName);
        Assert.isNotNull(type);
        Namespace ns = type.getNamespace();
        this.serviceID = (DiscoveryID) ns.createID((new Object[] { type, this }));

        this.serviceName = serviceName;

        this.weight = weight;
        this.priority = priority;

        this.properties =props;

        this.timeToLive = ttl;

    }

    public DiscoveryInfoImpl(String serviceName, DiscoveryType serviceType, int priority, int weight, ServiceProperties serviceProperties)
    {
        this(serviceName, serviceType, priority, weight, serviceProperties, DEFAULT_TTL);
    }

    @Override
    public String toQueryString() {
        return serviceID.toQueryString();
    }

    @Override
    public DiscoveryID getServiceID() {
        return serviceID;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public long getTTL() {
        return timeToLive;
    }

    @Override
    public Dictionary<String, ?> getServiceProperties() {
        return properties;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

}
