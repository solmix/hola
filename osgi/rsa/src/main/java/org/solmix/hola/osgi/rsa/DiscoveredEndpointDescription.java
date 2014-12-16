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

package org.solmix.hola.osgi.rsa;

import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.solmix.hola.common.identity.Namespace;
import org.solmix.hola.discovery.identity.ServiceID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月13日
 */

public class DiscoveredEndpointDescription
{

    private final Namespace discoveryLocatorNamespace;

    private final EndpointDescription endpointDescription;

    private final ServiceID serviceID;

    private int hashCode = 7;

    /**
     * @param servicesNamespace
     * @param serviceID
     * @param ed
     */
    public DiscoveredEndpointDescription(Namespace discoveryLocatorNamespace,
        ServiceID serviceID, EndpointDescription ed)
    {
        this.discoveryLocatorNamespace = discoveryLocatorNamespace;
        this.serviceID = serviceID;
        this.endpointDescription = ed;
        this.hashCode = 31 * this.hashCode
            + discoveryLocatorNamespace.getName().hashCode();
        this.hashCode = 31 * this.hashCode + endpointDescription.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof DiscoveredEndpointDescription))
            return false;
        DiscoveredEndpointDescription o = (DiscoveredEndpointDescription) other;
        return (this.discoveryLocatorNamespace.equals(o.discoveryLocatorNamespace) && this.endpointDescription.equals(o.endpointDescription));
    }

    /**
     * @return
     */
    public EndpointDescription getEndpointDescription() {
        return endpointDescription;
    }

    /**
     * @return
     */
    public Namespace getDiscoveryLocatorNamespace() {
        return discoveryLocatorNamespace;
    }

    /**
     * @return
     */
    public ServiceID getServiceID() {
        return serviceID;
    }

}
