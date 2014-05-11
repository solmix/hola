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

package org.solmix.hola.core.internal;

import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.runtime.PluginActivator;
import org.solmix.runtime.SystemContext;
import org.solmix.runtime.SystemContextFactory;

/**
 * 集成spring容器，
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月2日
 */

public class Plugin implements PluginActivator
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
     * 其他模块如果要向IDFactory中注册NameSpace,可以先获取IDFactory，然后调用
     * {@link IDFactory#addNamespace(org.solmix.hola.core.identity.Namespace)}
     * 添加，这点和ＯＳＧＩ不同，ＯＳＧＩ中直接注册即可．
     */
    @Override
    public void setSystemContext(SystemContext context) {
        this.systemContext = context;
        if (context.getBean(IDFactory.class) == null) {
            context.setBean(DefaultIDFactory.getDefault(), IDFactory.class);
        }
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
