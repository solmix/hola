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

import java.io.IOException;
import java.util.Map;
import java.util.Queue;

import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.security.ConnectSecurityContext;
import org.solmix.hola.shared.SharedMessage;
import org.solmix.hola.shared.SharedServiceContext;
import org.solmix.hola.shared.SharedServiceProvider;
import org.solmix.runtime.event.Event;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月18日
 */

public class GenericSSContext implements SharedServiceContext
{

    private boolean isActive;
    private final GenericSSProvider provider;
    private final ID serviceID;
    private final ID providerID;
    private final Map<String, ?> properties;
    private final Queue<Event> queue;

    /**
     * @param sharedServiceID
     * @param homeProviderID
     * @param genericProvider
     * @param properties
     * @param queue
     */
    public GenericSSContext(ID sharedServiceID, ID homeProviderID,
        GenericSSProvider provider, Map<String, ?> properties,
        Queue<Event> queue)
    {
        this.provider=provider;
        this.serviceID=sharedServiceID;
        this.providerID=homeProviderID;
        this.properties=properties;
        this.queue=queue;
        isActive=true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#getLocalProviderID()
     */
    @Override
    public ID getLocalProviderID() {
        return providerID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#getProvider()
     */
    @Override
    public SharedServiceProvider getProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#connect(org.solmix.hola.core.identity.ID, org.solmix.hola.core.security.ConnectSecurityContext)
     */
    @Override
    public void connect(ID targetID, ConnectSecurityContext securityContext)
        throws ConnectException {
       if(!isActive)
           return;
       
       provider.connect(targetID, securityContext);
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#disconnect()
     */
    @Override
    public void disconnect() {
        provider.disconnect();
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#getTargetID()
     */
    @Override
    public ID getTargetID() {
        return provider.getTargetID();
    }

    /**
     * 
     */
    public void inactive() {
        isActive = false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceContext#sendMessage(org.solmix.hola.core.identity.ID, org.solmix.hola.shared.SharedMessage)
     */
    @Override
    public void sendMessage(ID target, SharedMessage message) throws IOException{
        if(!isActive)
            return ;
        provider.sendMessage(target,serviceID,message);
        
    }

}
