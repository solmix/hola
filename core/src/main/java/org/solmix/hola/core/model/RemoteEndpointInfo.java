/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.core.model;

import org.solmix.runtime.exchange.model.EndpointInfo;
import org.solmix.runtime.exchange.model.ProtocolInfo;
import org.solmix.runtime.exchange.model.ServiceInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月17日
 */

public class RemoteEndpointInfo implements EndpointInfo {

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.model.EndpointInfo#getService()
     */
    @Override
    public ServiceInfo getService() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.model.EndpointInfo#setService(org.solmix.runtime.exchange.model.ServiceInfo)
     */
    @Override
    public void setService(ServiceInfo serviceInfo) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.model.EndpointInfo#getAddress()
     */
    @Override
    public String getAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.model.EndpointInfo#getTransportId()
     */
    @Override
    public String getTransportId() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.model.EndpointInfo#getProtocol()
     */
    @Override
    public ProtocolInfo getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

}
