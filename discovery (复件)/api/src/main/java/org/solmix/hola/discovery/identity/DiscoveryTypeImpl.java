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

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.runtime.identity.AbstractNamespace;
import org.solmix.runtime.identity.BaseID;
import org.solmix.runtime.identity.IDCreateException;
import org.solmix.runtime.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public class DiscoveryTypeImpl extends BaseID implements DiscoveryType
{

    private static final long serialVersionUID = 6751472077455908271L;

    private String typeName = "";

    private String group;

    private final String serviceInterface;

    private final String category;

    public DiscoveryTypeImpl(Namespace ns, String serviceInterface, String group,String category)
    {
        super((AbstractNamespace)ns);
        Assert.isNotNull(serviceInterface);
        this.group = group;
        this.serviceInterface=serviceInterface;
        this.category = category;
        encodeType();
        Assert.isNotNull(typeName);
    }

    public DiscoveryTypeImpl(AbstractNamespace ns, DiscoveryType type)
    {
        this(ns, type.getServiceInterface(), type.getGroup(), type.getCategory());
    }

    public DiscoveryTypeImpl(AbstractNamespace ns, String typeName)
    {
        super(ns);
        if (typeName == null)
            throw new IDCreateException("Service Type Name is null");
        try {
            typeName = typeName.trim();
            
            if (typeName.endsWith(HOLA.PATH_SEPARATOR)) {
                typeName = typeName.substring(0, typeName.length() - 1);
            }
            if (typeName.startsWith(HOLA.PATH_SEPARATOR)){
                typeName = typeName.substring(1);
            }
            int last = typeName.lastIndexOf('/');
            int first=typeName.indexOf(HOLA.PATH_SEPARATOR);
            this.category=typeName.substring(last, typeName.length());
            this.group=typeName.substring(0,first);
            this.serviceInterface=typeName.substring(first,last);
            encodeType();
            Assert.isTrue((this.typeName.equals(typeName)));
        } catch (Exception e) {
            throw new IDCreateException("service type not parseable", e);
        }

    }

    /**
     * 
     */
    private void encodeType() {
        final StringBuffer buf = new StringBuffer();
        if (DataUtils.isEmpty(group)) {
            group = DEFAULT_GROUP;
        }
        buf.append(group).append(HOLA.PATH_SEPARATOR).append(serviceInterface).append(HOLA.PATH_SEPARATOR).append(category);
        typeName = buf.toString();

    }

    @Override
    public String getName() {
        return typeName;
    }

    @Override
    protected int namespaceCompareTo(BaseID o) {
        if (o instanceof DiscoveryType) {
            final DiscoveryType other = (DiscoveryType) o;
            final String typename = other.getName();
            return getName().compareTo(typename);
        }
        return 1;
    }

    @Override
    protected boolean namespaceEquals(BaseID o) {
        if (o == null)
            return false;
        if (o instanceof DiscoveryType) {
            final DiscoveryType other = (DiscoveryType) o;
            if (getNamespace().equals(other.getNamespace()) && other.getName().equals(getName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected String namespaceGetName() {
        return typeName;
    }

  
    @Override
    protected int namespaceHashCode() {
        return getName().hashCode();
    }

    @Override
    public String getInternal() {
        return typeName;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getServiceInterface() {
        return serviceInterface;
    }

}
