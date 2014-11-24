/**
 * Copyright (c) 2014 The Solmix Project
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

package org.solmix.hola.rm.generic;

import java.lang.reflect.Proxy;
import java.util.List;

import org.solmix.hola.rm.RemoteServerFactory;
import org.solmix.runtime.exchange.Server;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;
import org.solmix.runtime.resource.ResourceManagerImpl;
import org.solmix.runtime.resource.ResourceResolver;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月21日
 */

public class GenericServerFactory extends RemoteServerFactory {

    private static final long serialVersionUID = -2026707232504432937L;

    private boolean blockPostConstruct;

    private boolean blockInjection;

    protected boolean doInit;

    public GenericServerFactory() {
        this(new GenericServiceFactory());
    }

    public GenericServerFactory(GenericServiceFactory factory) {
        super(factory);
        doInit = true;
    }

    /**
     * @return
     */
    @Override
    public Server create() {
        Server server = super.create();
        if (doInit) {
            initialServiceBean(getServiceBean());
        }
        doInit = false;
        return server;
    }

    private void initialServiceBean(Object instance) {
        if (instance != null && !blockInjection) {
            ResourceManager resourceManager = getContainer().getExtension(
                ResourceManager.class);
            List<ResourceResolver> resolvers = resourceManager.getResourceResolvers();
            resourceManager = new ResourceManagerImpl(resolvers);
            ResourceInjector injector = new ResourceInjector(resourceManager);
            if (Proxy.isProxyClass(instance.getClass())
                && getServiceClass() != null) {
                injector.inject(instance, getServiceClass());
                if (!blockPostConstruct) {
                    injector.construct(instance, getServiceClass());
                }
            } else {
                injector.inject(instance);
                if (!blockPostConstruct) {
                    injector.construct(instance);
                }
            }
        }
    }

    /**
     * @param blockPostConstruct @PostConstruct method will not be called if
     *        this property is set to true - this may be necessary in cases when
     *        the @PostConstruct method needs to be called at a later stage, for
     *        example, when a higher level container does its own injection.
     */
    public void setBlockPostConstruct(boolean blockPostConstruct) {
        this.blockPostConstruct = blockPostConstruct;
    }

    /**
     * No injection or PostConstruct will be called if this is set to true. If
     * the container has already handled the injection, this should be set to
     * true.
     * 
     * @param b
     */
    public void setBlockInjection(boolean b) {
        this.blockInjection = b;
    }
}
