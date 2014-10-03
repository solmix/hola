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

import java.net.URI;

import org.solmix.hola.core.identity.AbstractNamespace;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月15日
 */
@Extension(name=ZKNamespace.SCHEME)
public class ZKNamespace extends AbstractNamespace implements Namespace
{

    private static final long serialVersionUID = 1423285184748568323L;

    public static final String NAME = "namespace.jmdns";

    public static final String SCHEME = "jmdns";
    public ZKNamespace(){
        
        super(NAME,"zookeeper discovery namespace");
    }
    @Override
    public String getScheme() {
        return SCHEME;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.AbstractNamespace#createID(java.lang.Object[])
     */
    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Class<?>[][] getSupportedParameterTypes() {
        return new Class[][] {  { String.class }, { ServiceType.class },
            { ServiceType.class, URI.class }};
    }
}
