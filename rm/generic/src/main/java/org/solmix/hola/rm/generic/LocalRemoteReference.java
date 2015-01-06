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
package org.solmix.hola.rm.generic;

import org.solmix.hola.common.config.RemoteServiceConfig;
import org.solmix.hola.rm.RemoteReference;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月31日
 */

public class LocalRemoteReference<S> implements RemoteReference<S> {

    private final GenericRemoteRegistration<S> registration;
    private final Object service;
    public LocalRemoteReference(GenericRemoteRegistration<S> registration){
        this.registration=registration;
        service=registration.getService();
    }
   
    @Override
    public boolean isActive() {
        return registration!=null;
    }

   
    @Override
    public RemoteServiceConfig getServiceConfig() {
        return registration.getServiceConfig();
    }
    
    public Object getService(){
        return registration.getService();
    }

}
