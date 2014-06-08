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
package org.solmix.hola.shared.generic;

import java.util.Map;
import java.util.Queue;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.shared.SharedServiceConfig;
import org.solmix.hola.shared.SharedServiceContext;
import org.solmix.runtime.Event;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月18日
 */

public class GenericServiceConfig implements SharedServiceConfig
{
    
    private final ID serviceID;
    private final ID providerID;
    private final GenericProvider provider;
    private final Map<String,?> properties;
    protected boolean isActive;
    private GenericContext context;
    
    public GenericServiceConfig(ID serviceID,ID providerID,GenericProvider provider,Map<String,?> properties){
        this.serviceID=serviceID;
        this.providerID=providerID;
        this.provider=provider;
        this.properties=properties;
        isActive=false;
    }
    protected void active(Queue<Event> queue){
        isActive=true;
       context=provider.createSharedContext(this,queue);
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceConfig#getSharedServiceID()
     */
    @Override
    public ID getSharedServiceID() {
        return serviceID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceConfig#getHomeProviderID()
     */
    @Override
    public ID getHomeProviderID() {
        return providerID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceConfig#getContext()
     */
    @Override
    public SharedServiceContext getContext() {
        if (isActive) {
            return context;
      }
      return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceConfig#getProperties()
     */
    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }
    /**
     * 
     */
    public void inactive() {
        if (isActive) {
            this.context.inactive();
            this.context = null;
            isActive = false;
      }
        
    }

}
