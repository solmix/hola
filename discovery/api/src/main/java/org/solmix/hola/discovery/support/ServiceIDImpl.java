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

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.AbstractNamespace;
import org.solmix.hola.core.identity.BaseID;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */
@Immutable
public class ServiceIDImpl extends BaseID implements ServiceID
{

    private static final long serialVersionUID = -3700231292762874357L;

    private ServiceMetadata metadata;

    private final ServiceType type;

    private final URI location;

    public ServiceIDImpl(AbstractNamespace namespace, ServiceType type, URI location)
    {
        super(namespace);
        Assert.isNotNull(type);
        Assert.isNotNull(location);
        this.type = type;
        this.location = location;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceID#getServiceType()
     */
    @Override
    public ServiceType getServiceType() {
        return type;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceID#getServiceName()
     */
    @Override
    public String getServiceName() {
        return metadata.getServiceName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceID#getLocation()
     */
    @Override
    public URI getLocation() {
        return location;
    }

    
    /**
     * @param metadata the metadata to set
     */
    public void setServiceMetadata(ServiceMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceCompareTo(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected int namespaceCompareTo(BaseID o) {
        if (o instanceof ServiceIDImpl) {
            final ServiceIDImpl other = (ServiceIDImpl) o;
            final String typename = other.getFullyQualifiedName();
            return getFullyQualifiedName().compareTo(typename);
        }
        return 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceEquals(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected boolean namespaceEquals(BaseID o) {
        if (o == null)
            return false;
        if (o instanceof ServiceIDImpl) {
            final ServiceIDImpl other = (ServiceIDImpl) o;
            if (other.getName().equals(getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceGetName()
     */
    @Override
    protected String namespaceGetName() {
        return getFullyQualifiedName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceHashCode()
     */
    @Override
    protected int namespaceHashCode() {
        return getFullyQualifiedName().hashCode();
    }

    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer("ServiceID[");
        buf.append("type=").append(type).append(";location=").append(
            getLocation()).append(";full=" + getFullyQualifiedName()).append(
            "]");
        return buf.toString();
    }

    /**
     * @return
     */
    protected String getFullyQualifiedName() {
        return type.getName() + "@" + location;
    }
}
