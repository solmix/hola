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

package org.solmix.hola.rs.support;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.BaseID;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月1日
 */

public class RemoteServiceIDImpl extends BaseID
{

    private static final long serialVersionUID = -8764641734539906766L;

    private final ID contextID;

    private final long relativeID;

    private int hash;

    /**
     * @param remoteServiceNamespace
     * @param id
     * @param longValue
     */
    public RemoteServiceIDImpl(Namespace namespace, ID contextID,
        long relativeID)
    {
        super(namespace);
        Assert.isNotNull(namespace);
        this.contextID = contextID;
        this.relativeID = relativeID;
        this.hash = 7;
        this.hash = 31 * hash + contextID.hashCode();
        this.hash = 31 * hash + (int) (relativeID ^ (relativeID >>> 32));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.identity.RemoteServiceID#getProviderID()
     */
    public ID getProviderID() {
        return contextID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceCompareTo(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected int namespaceCompareTo(BaseID o) {
        if (o == null || !(o instanceof RemoteServiceIDImpl)) {
            return Integer.MIN_VALUE;
        }
        RemoteServiceIDImpl other = (RemoteServiceIDImpl) o;
        int containerIDCompareResult = this.contextID.compareTo(other.getProviderID());
        if (containerIDCompareResult == 0)
            return (int) (this.getRelativeID() - other.getRelativeID());
        return containerIDCompareResult;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceEquals(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected boolean namespaceEquals(BaseID o) {
        if (o == this)
            return true;
        if (o == null || !(o instanceof RemoteServiceIDImpl)) {
            return false;
        }
        RemoteServiceIDImpl other = (RemoteServiceIDImpl) o;
        if (contextID.equals(other.getProviderID()))
            return this.getRelativeID() == other.getRelativeID();
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceGetName()
     */
    @Override
    protected String namespaceGetName() {
        return new StringBuilder().append(contextID.getName()).append("/").append(
            getRelativeID()).toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceHashCode()
     */
    @Override
    protected int namespaceHashCode() {
        return hash;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.identity.RemoteServiceID#getRelativeID()
     */
    public long getRelativeID() {
        return relativeID;
    }

}
