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
package org.solmix.hola.rs.generic;

import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.rs.RemoteServiceListener;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.hola.rs.RemoteServiceManagerProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月19日
 */
@Extension(name=HolaRemoteManagerProvider.NAME)
public class HolaRemoteManagerProvider implements RemoteServiceManagerProvider
{

    public static final String NAME="hola";
    
    private final Container container;
    
   public HolaRemoteManagerProvider(Container container){
       this.container=container;
   }
 
    @Override
    public RemoteServiceManager createManager(
        RemoteServiceListener... listeners) {
        return new HolaRemoteServiceManager(container, listeners);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceManagerProvider#createManager(org.solmix.hola.core.model.RemoteInfo)
     */
    @Override
    public RemoteServiceManager createManager() {
        return createManager(new RemoteServiceListener[]{} );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceManagerProvider#getSupportedIntents(org.solmix.hola.core.model.RemoteInfo)
     */
    @Override
    public String[] getSupportedIntents(EndpointInfo info) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceManagerProvider#getSupportedConfigs(org.solmix.hola.core.model.RemoteInfo)
     */
    @Override
    public String[] getSupportedConfigs(EndpointInfo info) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceManagerProvider#getImportedConfigs(org.solmix.hola.core.model.RemoteInfo, java.lang.String[])
     */
    @Override
    public String[] getImportedConfigs(EndpointInfo info,
        String[] remoteSupportedConfigs) {
        // TODO Auto-generated method stub
        return null;
    }

}
