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

package org.solmix.hola.discovery.event;

import java.util.EventObject;
import java.util.List;

import org.solmix.hola.common.model.ServiceType;
import org.solmix.hola.discovery.model.DiscoveryInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月5日
 */

public class DiscoveryTypeEvent extends EventObject
{

    private static final long serialVersionUID = -8960251126420910112L;

    public static final int REGISTER = 1;

    public static final int UNREGISTER = 2;

    private final ServiceType serviceType;
    
    private int type;

    private final List<DiscoveryInfo> discoveryInfoList;

    public DiscoveryTypeEvent(Object source, ServiceType type, List<DiscoveryInfo> categoryList )
    {
        this(source,type,categoryList,REGISTER);
    }
    public DiscoveryTypeEvent(Object source, ServiceType type, List<DiscoveryInfo> categoryList ,int eventType)
    {
        super(source);
        this.serviceType = type;
        this.discoveryInfoList = categoryList;
        this.type=eventType;
    }

    public ServiceType getServiceType() {
        return serviceType;

    }

    public List<DiscoveryInfo> getDiscoveryInfoList() {
        return discoveryInfoList;
    }
    
    public int getEventType(){
        return type;
    }

}
