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

import java.util.Arrays;
import java.util.List;

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.core.identity.BaseID;
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.identity.ServiceType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月12日
 */

public class ServiceTypeImpl extends BaseID implements ServiceType
{

    private static final long serialVersionUID = 6751472077455908271L;

    private String typeName = "";

    private String[] services;

    private final String[] scopes;

    private final String[] protocols;

    private final String namingAuthority;

    protected static final String DELIM = "._";

    public ServiceTypeImpl(Namespace ns, String[] services, String[] scopes,
        String[] protocols, String namingAuthority)
    {
        super(ns);
        Assert.isNotNull(services);
        Assert.isNotNull(scopes);
        this.scopes = scopes;
        this.services=services;
        Assert.isNotNull(protocols);
        this.protocols = protocols;
        Assert.isNotNull(namingAuthority);
        this.namingAuthority = namingAuthority;
        encodeType();
        Assert.isNotNull(typeName);
    }

    public ServiceTypeImpl(Namespace ns, ServiceType type)
    {
        this(ns, type.getServices(), type.getScopes(), type.getProtocols(),
            type.getNamingAuthority());
    }

    public ServiceTypeImpl(Namespace ns, String typeName)
    {
        super(ns);
        if (typeName == null)
            throw new IDCreateException("Service Type Name is null");
        try {
            typeName = typeName.trim();
            if (typeName.endsWith(".")) {
                typeName = typeName.substring(0, typeName.length() - 1);
            }
            int lastDot = typeName.lastIndexOf('.');
            int lastUnderscore = typeName.lastIndexOf('_');
            if (lastDot + 1 != lastUnderscore) {
                typeName = typeName + "._" + DEFAULT_NA;
            }

            String type = typeName.substring(1);

            String[] split = type.split("._");
            // naming authority
            int offset = split.length - 1;
            this.namingAuthority = split[offset];

            // protocol and scope
            String string = split[--offset];
            String[] protoAndScope = StringUtils.split(string, ".");
            this.protocols = new String[] { protoAndScope[0] };
            this.scopes = new String[] { protoAndScope[1] };

            // services are the remaining strings in the array
            List<String> subList = Arrays.asList(split).subList(0, offset);
            this.services = subList.toArray(new String[0]);

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
        // services
        buf.append("_");
        for (int i = 0; i < services.length; i++) {
            buf.append(services[i]);
            buf.append(DELIM);
        }
        // protocols
        for (int i = 0; i < protocols.length; i++) {
            buf.append(protocols[i]);
            if (i != protocols.length - 1) {
                buf.append(DELIM);
            } else {
                buf.append(".");
            }
        }
        // scope
        for (int i = 0; i < scopes.length; i++) {
            buf.append(scopes[i]);
            buf.append(DELIM);
        }
        buf.append(namingAuthority);

        typeName = buf.toString();

    }

    @Override
    public String getName() {
        return typeName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceCompareTo(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected int namespaceCompareTo(BaseID o) {
        if (o instanceof ServiceType) {
            final ServiceType other = (ServiceType) o;
            final String typename = other.getName();
            return getName().compareTo(typename);
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
        if (o instanceof ServiceType) {
            final ServiceType other = (ServiceType) o;
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
        return typeName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceHashCode()
     */
    @Override
    protected int namespaceHashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceType#getNamingAuthority()
     */
    @Override
    public String getNamingAuthority() {
        return namingAuthority;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceType#getProtocols()
     */
    @Override
    public String[] getProtocols() {
        return protocols;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceType#getScopes()
     */
    @Override
    public String[] getScopes() {
        return scopes;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceType#getServices()
     */
    @Override
    public String[] getServices() {
        return services;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceType#getInternal()
     */
    @Override
    public String getInternal() {
        return typeName;
    }

}
