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
import org.solmix.hola.common.model.DefaultServiceID;
import org.solmix.hola.common.model.DefaultServiceType;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.common.model.ServiceType;

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

    protected ServiceID serviceID;

    protected int priority;

    protected int weight;

    protected Dictionary<String, ?> properties;

    protected long timeToLive;

    public DiscoveryInfoImpl(Dictionary<String, ?> properties)
    {
        String service = PropertiesUtils.getServiceInterface(properties);
        String group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY);
        String category = PropertiesUtils.getString(properties, HOLA.CATEGORY_KEY, HOLA.DEFAULT_CATEGORY);
        DefaultServiceType type = new DefaultServiceType( service, group, category);
        int weight = PropertiesUtils.getInt(properties, HOLA.WEIGHT_KEY, HOLA.DEFAULT_WEIGHT);
        int priority = PropertiesUtils.getInt(properties, HOLA.PRIORITY_KEY, HOLA.DEFAULT_PRIORITY);
        long ttl = PropertiesUtils.getLong(properties, HOLA.TTL_KEY, -1);
        this.serviceID = new  DefaultServiceID (type, properties );
        this.serviceName = serviceID.getName();

        this.weight = weight;
        this.priority = priority;

        this.properties = properties;

        this.timeToLive = ttl;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DiscoveryInfoImpl(ServiceType type, int priority, int weight,  Dictionary<String, ?> properties, long ttl)
    {
        Assert.isNotNull(serviceName);
        Assert.isNotNull(type);
        Dictionary dic = properties;
        dic.put(HOLA.PRIORITY_KEY, priority);
        dic.put(HOLA.WEIGHT_KEY, weight);
        dic.put(HOLA.TTL_KEY, ttl);
        this.serviceID =new  DefaultServiceID (type, dic );

        this.serviceName = serviceID.getName();

        this.weight = weight;
        this.priority = priority;

        this.properties =dic;

        this.timeToLive = ttl;

    }

    public DiscoveryInfoImpl(ServiceType serviceType, int priority, int weight, ServiceProperties serviceProperties)
    {
        this( serviceType, priority, weight, serviceProperties, DEFAULT_TTL);
    }

    @Override
    public ServiceID getServiceID() {
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
    
    @Override
    public String toString(){
        return new StringBuilder().append("[DiscoveryInfo]:").append(getServiceName()).toString();
    }

}
