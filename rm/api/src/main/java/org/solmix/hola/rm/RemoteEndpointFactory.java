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

package org.solmix.hola.rm;

import org.solmix.runtime.exchange.Endpoint;
import org.solmix.runtime.exchange.EndpointException;
import org.solmix.runtime.exchange.support.AbstractEndpointFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月21日
 */

public class RemoteEndpointFactory extends AbstractEndpointFactory {

    private static final long serialVersionUID = 121982796534012439L;

    private Class<?> serviceClass;

    private ReflectServiceFactory serviceFactory;

    protected RemoteEndpointFactory(ReflectServiceFactory factory) {
        this.serviceFactory = factory;
        this.serviceClass = factory.getServiceClass();
    }

    protected RemoteEndpointFactory() {

    }

    @Override
    protected Endpoint createEndpoint() throws EndpointException {
        // TODO Auto-generated method stub
        return null;
    }

    
    /**   */
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    
    /**   */
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

}
