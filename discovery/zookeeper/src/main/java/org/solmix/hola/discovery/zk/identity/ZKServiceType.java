/*
 * Copyright 2014 The Solmix Project
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

package org.solmix.hola.discovery.zk.identity;

import java.util.UUID;

import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.ServiceTypeImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年9月15日
 */

public class ZKServiceType extends ServiceTypeImpl
{

    /**
     * @param ns
     * @param type
     */
    public ZKServiceType(ZKNamespace ns, ServiceType type)
    {
        super(ns, type);
        this.id = UUID.randomUUID().toString();
    }
    public ZKServiceType(ZKNamespace ns, ServiceType type,String internal)
    {
        super(ns, type);
        this.id = internal;
    }
    private static final long serialVersionUID = -6410131727241892261L;

    private final String id;
    @Override
    public String getInternal() {
        return this.id;
  }
}
