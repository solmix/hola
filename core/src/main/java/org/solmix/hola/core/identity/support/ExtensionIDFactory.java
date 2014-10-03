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
package org.solmix.hola.core.identity.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.runtime.Container;
import org.solmix.runtime.extension.ExtensionLoader;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月14日
 */

public class ExtensionIDFactory extends AbstractIDFactory implements IDFactory
{
    private final Map<String, Namespace> namespaces = new java.util.concurrent.ConcurrentHashMap<String, Namespace>();

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionIDFactory.class);
    public ExtensionIDFactory(Container container){
        addNamespace(new StringNamespace());
        addNamespace(new GUIDNamespace());
        addNamespace(new LongNamespace());
        addNamespace(new URINamespace());
        ExtensionLoader<Namespace> loader=  container.getExtensionLoader(Namespace.class);
        try {
            if(loader!=null){
             for(String name:   loader.getLoadedExtensions()){
                 addNamespace(loader.getExtension(name));
             }
            }
        } catch (Exception e) {
            LOG.error("Error add extension namespace:",e);
        }
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.IDFactory#addNamespace(org.solmix.hola.core.identity.Namespace)
     */
    @Override
    public Namespace addNamespace(Namespace namespace) throws SecurityException {
        if (namespace == null)
            return null;
        return namespaces.put(namespace.getName(), namespace);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.IDFactory#containsNamespace(org.solmix.hola.core.identity.Namespace)
     */
    @Override
    public boolean containsNamespace(Namespace namespace)
        throws SecurityException {
        if (namespace == null)
            return false;
        return namespaces.containsKey(namespace.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.IDFactory#getNamespaces()
     */
    @Override
    public List<Namespace> getNamespaces() throws SecurityException {
        return new ArrayList<Namespace>(namespaces.values());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.IDFactory#getNamespace(org.solmix.hola.core.identity.Namespace)
     */
    @Override
    public Namespace getNamespace(Namespace namespace) throws SecurityException {
        if (namespace == null)
            return null;
        return namespaces.get(namespace.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.IDFactory#getNamespaceByName(java.lang.String)
     */
    @Override
    public Namespace getNamespaceByName(String name) throws SecurityException {
        return namespaces.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.IDFactory#removeNamespace(org.solmix.hola.core.identity.Namespace)
     */
    @Override
    public Namespace removeNamespace(Namespace namespace) throws SecurityException {
        if (namespace == null)
            return null;
        return namespaces.remove(namespace.getName());
    }
   
}
