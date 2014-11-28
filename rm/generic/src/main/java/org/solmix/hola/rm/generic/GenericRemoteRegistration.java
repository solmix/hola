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


import org.solmix.hola.core.model.RemoteEndpointInfo;
import org.solmix.hola.rm.RemoteException;
import org.solmix.hola.rm.RemoteReference;
import org.solmix.hola.rm.RemoteRegistration;
import org.solmix.runtime.exchange.Server;
import org.solmix.runtime.exchange.support.DefaultServer;

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

    /** The registration state */
    protected int state = REGISTERED;

    private final GenericRemoteManager manager;

    private final RemoteEndpointInfo info;

    private final Object service;

    private final String[] clazzes;
    
    private Server server;
    
    private GenericServerFactory serverFactory;
    

    public GenericRemoteRegistration(GenericRemoteManager manager,
        String[] clazzes, Object service, RemoteEndpointInfo info) {
        this.manager = manager;
        this.info = info;
        this.service = service;
        this.clazzes = clazzes;
    }

    @Override
    public RemoteReference<S> getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unregister() {
        // TODO Auto-generated method stub

    }

    @Override
    public RemoteEndpointInfo getEndpointInfo() {
        return info;
    }
    
    public GenericServerFactory getServerFactory() {
        return serverFactory;
    }

    
    public void setServerFactory(GenericServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    void publish() {
        String address = info.getAddress();
        DefaultServer srv = null;
        try {
            srv = getServer(address);
            if (srv != null) {
                srv.start();
            }
        } catch (Exception e) {
            if (null != server) {
                server.destroy();
                server = null;
            }
            throw new RemoteException(e);
        }
    }

   
    private DefaultServer getServer(String address) {
        if(server==null){
            if(serverFactory==null){
                serverFactory= new GenericServerFactory();
            }
            
            manager.configureBean(serverFactory);
            server=serverFactory.create();
            //TODO
        }
        return (DefaultServer)server;
    }

}