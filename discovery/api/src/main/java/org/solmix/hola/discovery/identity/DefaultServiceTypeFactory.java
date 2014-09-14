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

import org.solmix.hola.core.identity.AbstractNamespace;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.internal.DefaultIDFactory;
import org.solmix.hola.discovery.support.ServiceTypeImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public class DefaultServiceTypeFactory implements ServiceTypeFactory
{
    private static final ServiceTypeFactory instance = new DefaultServiceTypeFactory();

    public static ServiceTypeFactory getDefault() {
        return instance;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceTypeFactory#create(org.solmix.hola.core.identity.Namespace,
     *      java.lang.String)
     */
    @Override
    public ServiceType create(Namespace namespace, String service) {
        return create(namespace, new String[] { service });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceTypeFactory#create(org.solmix.hola.core.identity.Namespace,
     *      java.lang.String[])
     */
    @Override
    public ServiceType create(Namespace namespace, String[] services) {
        return create(namespace, services, ServiceType.DEFAULT_SCOPE, ServiceType.DEFAULT_PROTO,
            ServiceType.DEFAULT_NA);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceTypeFactory#create(org.solmix.hola.core.identity.Namespace,
     *      java.lang.String[], java.lang.String[], java.lang.String[],
     *      java.lang.String)
     */
    @Override
    public ServiceType create(Namespace ns, String[] services, String[] scopes,
        String[] protocols, String namingAuthority) {
        ServiceTypeImpl type = new ServiceTypeImpl(ns.adaptTo(AbstractNamespace.class), services, scopes,
            protocols, namingAuthority);
        return (ServiceType) DefaultIDFactory.getDefault().createID(ns,
            new Object[] { type });
    }

    @Override
    public ServiceType create(Namespace ns, String[] services,
        String[] protocols) {
        return create(ns, services, ServiceType.DEFAULT_SCOPE, protocols,
            ServiceType.DEFAULT_NA);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceTypeFactory#create(org.solmix.hola.core.identity.Namespace,
     *      org.solmix.hola.discovery.ServiceType)
     */
    @Override
    public ServiceType create(Namespace ns, ServiceType type) {
        return (ServiceType) DefaultIDFactory.getDefault().createID(ns,
            new Object[] { type });
    }

}
