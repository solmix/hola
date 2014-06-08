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
package org.solmix.hola.rs.internal;

import org.solmix.hola.rs.RemoteServiceProxyFactory;
import org.solmix.runtime.PluginActivator;
import org.solmix.runtime.SystemContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月30日
 */

public class Plugin implements PluginActivator
{
    private static Plugin plugin;
    private SystemContext systemContext;
    public Plugin() {
        plugin = this;
  }
    
    public synchronized static Plugin getDefault() {
        if (plugin == null) {
              plugin = new Plugin();
        }
        return plugin;
  }
    
    /**
     * @return the systemContext
     */
    public SystemContext getSystemContext() {
        return systemContext;
    }
    
    /**
     * @param systemContext the systemContext to set
     */
    @Override
    public void setSystemContext(SystemContext systemContext) {
        this.systemContext = systemContext;
    }
    
    /**
     * 
     * @return
     */
    public ClassLoader getPluginClassLoader(){
      return  systemContext.getBean(ClassLoader.class);
    }
    
    public RemoteServiceProxyFactory getRemoteServiceProxyFactory(){
        if(systemContext!=null){
            return systemContext.getBean(RemoteServiceProxyFactory.class);
        }
        return null;
    }
 

}
