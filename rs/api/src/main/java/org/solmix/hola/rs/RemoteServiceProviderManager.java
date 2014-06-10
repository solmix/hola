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

package org.solmix.hola.rs;

import java.util.List;
import java.util.Map;

import org.solmix.hola.core.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月28日
 */

public interface RemoteServiceProviderManager
{

    RemoteServiceProvider getProvider(ID providerID);

    RemoteServiceProviderDescription getProviderDescription(ID providerID);

    RemoteServiceProvider[] getProviders();

    boolean hasProvider(ID providerID);

    RemoteServiceProvider addProvider(RemoteServiceProvider provider,
        RemoteServiceProviderDescription desc);

    RemoteServiceProvider removeProvider(RemoteServiceProvider provider);

    RemoteServiceProvider removeProvider(ID providerID);

    boolean addListener(RemoteServiceProviderListener listener);

    boolean remoteListener(RemoteServiceProviderListener listener);

    void remoteAllProviders();
    RemoteServiceProviderDescription addDescription(RemoteServiceProviderDescription description);
    
    RemoteServiceProviderDescription removeDescription(RemoteServiceProviderDescription description);
    
    List<RemoteServiceProviderDescription> getDescriptions();
    public RemoteServiceProvider createProvider(String descriptionName) throws ProviderCreateException;

    /**
     * @param descriptionName
     * @param properties
     * @return
     */
    RemoteServiceProvider createProvider(String descriptionName,
        Map<String, Object> properties)throws ProviderCreateException;

}
