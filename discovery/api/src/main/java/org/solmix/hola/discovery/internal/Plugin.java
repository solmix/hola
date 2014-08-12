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
package org.solmix.hola.discovery.internal;

import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.discovery.identity.DiscoveryNamespace;
import org.solmix.runtime.ContainerExtension;
import org.solmix.runtime.SystemContext;
import org.solmix.runtime.SystemContextFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月10日
 */

public class Plugin implements ContainerExtension
{
    private static Plugin plugin;

    private SystemContext systemContext;

    public Plugin(SystemContext context)
    {
        plugin = this;
        setSystemContext(context);
    }

    public synchronized static Plugin getDefault() {
        if (plugin == null) {
            plugin = new Plugin(null);
        }
        return plugin;
    }

    /**
     * 注册Namespace．
     */
    @Override
    public void setSystemContext(SystemContext context) {
        this.systemContext = context;
        DefaultIDFactory.getDefault().addNamespace(new DiscoveryNamespace(
           "Discovery Namespace"));
    }

    /**
     * @return the systemContext
     */
    public SystemContext getSystemContext() {
        if(systemContext==null){//在OSGI下，没有在blueprint下配置,所有为空
            return SystemContextFactory.getThreadDefaultSystemContext();
        }
        return systemContext;
    }

}
