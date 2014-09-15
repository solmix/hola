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
package org.solmix.hola.discovery.zk;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.model.DiscoveryInfo;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.AbstractDiscovery;
import org.solmix.hola.discovery.zk.identity.ZKNamespace;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月15日
 */

public class ZKDiscovery extends AbstractDiscovery
{

    private final DiscoveryInfo info;
    private boolean closed;
    /**
     * @param discoveryNamespace
     */
    public ZKDiscovery(DiscoveryInfo info ,Container container) throws DiscoveryException
    {
        super(ZKNamespace.NAME,container);
        this.info=info;
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryAdvertiser#register(org.solmix.hola.discovery.ServiceMetadata)
     */
    @Override
    public void register(ServiceMetadata serviceMetadata) {
       Assert.isNotNull(serviceMetadata);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryAdvertiser#unregister(org.solmix.hola.discovery.ServiceMetadata)
     */
    @Override
    public void unregister(ServiceMetadata serviceMetadata) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void close(){
        super.close();
        if(closed){
            return;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getService(org.solmix.hola.discovery.identity.ServiceID)
     */
    @Override
    public ServiceMetadata getService(ServiceID aServiceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices()
     */
    @Override
    public ServiceMetadata[] getServices() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices(org.solmix.hola.discovery.identity.ServiceType)
     */
    @Override
    public ServiceMetadata[] getServices(ServiceType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServiceTypes()
     */
    @Override
    public ServiceType[] getServiceTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#purgeCache()
     */
    @Override
    public ServiceMetadata[] purgeCache() {
        // TODO Auto-generated method stub
        return null;
    }

}
