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

package org.solmix.hola.common.model;

import java.util.Dictionary;

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.util.Assert;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */
@Immutable
public class DefaultServiceID  implements ServiceID
{

    private static final long serialVersionUID = -3700231292762874357L;

    private final ServiceType type;

    private ServiceProperties serviceProperties;

    private String serviceName;

    public DefaultServiceID(ServiceType type, String address)
    {
        Assert.isNotNull(type);
        this.type = type;
        this.serviceName = address;
        this.serviceProperties = new ServiceProperties(PropertiesUtils.toProperties(address));
    }

    public DefaultServiceID( ServiceType type, Dictionary<String, ?> properties)
    {

        Assert.isNotNull(type);
        this.type = type;
        this.serviceProperties = new ServiceProperties(properties);
        this.serviceName = PropertiesUtils.toAddress(serviceProperties);

    }

    @Override
    public ServiceType getServiceType() {
        return type;
    }

    
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer("DiscoveryID[");
        buf.append("type=").append(type).append(";address=").append(serviceName);
        return buf.toString();
    }

    /**
     * @return
     */
    protected String getFullyQualifiedName() {
        return type.getIdentityName() + "@" + serviceName;
    }

    @Override
    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

    @Override
    public String getName() {
        return serviceName;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof ServiceID){
            return getName().equals(((ServiceID)obj).getName());
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        return getName().hashCode();
    }
}
