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
import org.solmix.hola.rm.RemoteRegistration;
import org.solmix.runtime.exchange.Server;
import org.solmix.runtime.exchange.model.NamedID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月20日
 */

public class GenericRemoteRegistration<S> implements RemoteRegistration<S>,
    java.io.Serializable {

    public static final int REGISTERED = 0x00;

    public static final int UNREGISTERING = 0x01;

    public static final int UNREGISTERED = 0x02;

    private static final long serialVersionUID = 1656647471796485503L;

    protected transient Object registrationLock = new Object();

    Server server;

    final Object service;

    final Class<?> clazze;

    /** The registration state */
    protected int state = REGISTERED;
    
    protected transient LocalRemoteReference<S> reference;
    
    private final GenericRemoteManager manager;

    private final RemoteServiceConfig remoteServiceConfig;

    private GenericServerFactory serverFactory;
  
    private NamedID serviceName;

    public GenericRemoteRegistration(GenericRemoteManager manager,
        Class<?> clazze, Object service, RemoteServiceConfig info) {
        this.manager = manager;
        this.remoteServiceConfig = info;
        this.service = service;
        this.clazze = clazze;
    }

    @Override
    public RemoteReference<S> getReference() {
        if (reference == null) {
            synchronized (this) {
                reference = new LocalRemoteReference<S>(this);
            }
        }
        return reference;
    }

    @Override
    public void unregister() {
        if(manager!=null){
            manager.unregisterService(this);
        }

    }

    @Override
    public RemoteServiceConfig getServiceConfig() {
        return remoteServiceConfig;
    }

    GenericServerFactory getServerFactory() {
        return serverFactory;
    }

    public void setServerFactory(GenericServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    void publish() {
        manager.publish(this);
    }

    /**
     * @return
     */
    public Object getService() {
        return service;
    }
    
    /**   */
    public NamedID getServiceName() {
        return serviceName;
    }
    
    /**   */
    public void setServiceName(NamedID serviceName) {
        this.serviceName = serviceName;
    }
    

}
