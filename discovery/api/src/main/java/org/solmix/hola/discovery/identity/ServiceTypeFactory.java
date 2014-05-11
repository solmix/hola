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

import org.solmix.hola.core.identity.Namespace;

/**
 * {@linkp ServiceType} 工厂类
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月12日
 */

public interface ServiceTypeFactory
{

    /**
     * 创建{@linkp ServiceType}
     * 
     * @param namespace Must not be <code>null</code>.
     * @param typeName Must not be <code>null</code>.
     * @return Must not be <code>null</code>.
     */
    ServiceType create(Namespace namespace, String service);

    ServiceType create(Namespace namespace, String[] services);

    ServiceType create(Namespace ns, String[] services, String[] scopes,
        String[] protocols, String namingAuthority);

    ServiceType create(Namespace ns, String[] services, String[] protocols);

    ServiceType create(Namespace ns, ServiceType type);
}
