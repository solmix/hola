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

import org.solmix.hola.common.HOLA;
import org.solmix.runtime.identity.AbstractNamespace;
import org.solmix.runtime.identity.IDFactory;
import org.solmix.runtime.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public class DefaultServiceTypeFactory implements DiscoveryTypeFactory
{

    private static final DiscoveryTypeFactory instance = new DefaultServiceTypeFactory();

    public static DiscoveryTypeFactory getDefault() {
        return instance;
    }

    @Override
    public DiscoveryType create(Namespace namespace, String service) {
        return create(namespace,DiscoveryType.DEFAULT_GROUP,service,HOLA.DEFAULT_CATEGORY);
    }

    @Override
    public DiscoveryType create(Namespace ns, String grop, String serviceInterface, String category) {
        DiscoveryTypeImpl type = new DiscoveryTypeImpl(ns.adaptTo(AbstractNamespace.class), grop, serviceInterface, category);
        return (DiscoveryType) IDFactory.getDefault().createID(ns, new Object[] { type });
    }

    @Override
    public DiscoveryType create(Namespace ns, String services, String category) {
        return create(ns,  DiscoveryType.DEFAULT_GROUP, services, category);
    }

    @Override
    public DiscoveryType create(Namespace ns, DiscoveryType type) {
        return (DiscoveryType) IDFactory.getDefault().createID(ns, new Object[] { type });
    }

}
