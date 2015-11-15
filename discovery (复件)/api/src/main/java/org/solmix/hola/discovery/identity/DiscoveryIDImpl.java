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

package org.solmix.hola.discovery.identity;

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.util.Assert;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.runtime.identity.AbstractNamespace;
import org.solmix.runtime.identity.BaseID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */
@Immutable
public class DiscoveryIDImpl extends BaseID implements DiscoveryID
{

    private static final long serialVersionUID = -3700231292762874357L;

    private final DiscoveryType type;

    private ServiceProperties serviceProperties;

    private String serviceName;

    public DiscoveryIDImpl(AbstractNamespace namespace, DiscoveryType type, String address)
    {
        super(namespace);
        Assert.isNotNull(type);
        this.type = type;
        this.serviceName = address;
        this.serviceProperties = new ServiceProperties(PropertiesUtils.toProperties(address));
    }

    public DiscoveryIDImpl(AbstractNamespace namespace, DiscoveryType type, DiscoveryInfo info)
    {

        super(namespace);
        Assert.isNotNull(type);
        this.type = type;
        this.serviceProperties = info.getServiceProperties();
        this.serviceName = PropertiesUtils.toAddress(serviceProperties);

    }

    @Override
    public DiscoveryType getServiceType() {
        return type;
    }

    @Override
    protected int namespaceCompareTo(BaseID o) {
        if (o instanceof DiscoveryIDImpl) {
            final DiscoveryIDImpl other = (DiscoveryIDImpl) o;
            final String typename = other.getFullyQualifiedName();
            return getFullyQualifiedName().compareTo(typename);
        }
        return 1;
    }

    @Override
    protected boolean namespaceEquals(BaseID o) {
        if (o == null)
            return false;
        if (o instanceof DiscoveryIDImpl) {
            final DiscoveryIDImpl other = (DiscoveryIDImpl) o;
            if (other.getName().equals(getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String namespaceGetName() {
        return getFullyQualifiedName();
    }

    @Override
    protected int namespaceHashCode() {
        return getFullyQualifiedName().hashCode();
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
        return type.getName() + "@" + serviceName;
    }

    @Override
    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }
}
