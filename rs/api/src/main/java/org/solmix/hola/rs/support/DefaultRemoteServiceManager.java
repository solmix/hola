/*
 * Copyright 2015 The Solmix Project
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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.solmix.exchange.support.TypeDetector;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.extension.ExtensionException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月16日
 */

public class DefaultRemoteServiceManager implements RemoteServiceManager, ContainerAware
{

    Map<String, RemoteServiceFactory> factories;

    private Container container;

    private final Set<String> failed = new CopyOnWriteArraySet<String>();

    private final Set<String> loaded = new CopyOnWriteArraySet<String>();

    public DefaultRemoteServiceManager()
    {
        factories = new ConcurrentHashMap<String, RemoteServiceFactory>(8, 0.75f, 4);
    }

    @Override
    public RemoteServiceFactory getRemoteServiceFactory(String protocol) {
        RemoteServiceFactory factory = factories.get(protocol);
        if (factory == null && !failed.contains(protocol)) {
            TypeDetector<RemoteServiceFactory> detector = new TypeDetector<RemoteServiceFactory>(getContainer(), factories, loaded,
                RemoteServiceFactory.class);
            factory = detector.detectInstanceForType(protocol);
        }
        if (factory == null) {
            failed.add(protocol);
            throw new ExtensionException("No found  RemoteServiceFactory extension with type: " + protocol);
        }
        return factory;
    }

    /**
     * @return
     */
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
        if (container != null) {
            container.setExtension(this, RemoteServiceManager.class);
        }
    }

    @Override
    public RemoteServiceFactory getFactoryForUri(String uri) {
        return new TypeDetector<RemoteServiceFactory>(getContainer(), factories, loaded, RemoteServiceFactory.class).detectInstanceForURI(uri);
    }

}
