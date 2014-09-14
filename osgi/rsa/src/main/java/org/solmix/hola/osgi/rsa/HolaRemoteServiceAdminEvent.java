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

import org.osgi.framework.Bundle;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.solmix.hola.core.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年6月4日
 */

public class HolaRemoteServiceAdminEvent extends RemoteServiceAdminEvent
{

    private final ID providerID;

    private final EndpointDescription endpointDescription;

    public HolaRemoteServiceAdminEvent(ID providerID, int type, Bundle source,
        ExportReference exportReference, Throwable exception,
        EndpointDescription endpointDescription)
    {
        super(type, source, exportReference, exception);
        this.providerID = providerID;
        this.endpointDescription = endpointDescription;
    }

    public HolaRemoteServiceAdminEvent(ID providerID, int type, Bundle source,
        ImportReference importReference, Throwable exception,
        EndpointDescription endpointDescription)
    {
        super(type, source, importReference, exception);
        this.providerID = providerID;
        this.endpointDescription = endpointDescription;
    }

    
    /**
     * @return the providerID
     */
    public ID getProviderID() {
        return providerID;
    }

    
    /**
     * @return the endpointDescription
     */
    public EndpointDescription getEndpointDescription() {
        return endpointDescription;
    }
    
}
