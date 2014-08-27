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
package org.solmix.hola.rs.generic;

import java.util.concurrent.atomic.AtomicInteger;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.AbstractRemoteService;
import org.solmix.hola.rs.RSRequest;
import org.solmix.hola.rs.RSRequestListener;
import org.solmix.hola.rs.RemoteServiceException;
import org.solmix.hola.rs.RemoteServiceReference;
import org.solmix.hola.rs.identity.RemoteServiceID;
import org.solmix.hola.rs.support.RSRequestImpl;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.exchange.ExchangeClient;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月21日
 */

public class HolaRemoteService extends AbstractRemoteService
{

    private final String[] clazzs;
    private final HolaRemoteServiceManager manager;
    private final RemoteInfo info;
    private final ExchangeClient[] clients;
    
    private final AtomicInteger index = new AtomicInteger();
    /**
     * @param clazz
     * @param info
     * @param clients
     */
    public HolaRemoteService(String[] clazzs, RemoteInfo info,
        ExchangeClient[] clients,HolaRemoteServiceManager manager)
    {
        this.clazzs=clazzs;
        this.manager=manager;
        this.info=info;
        this.clients=clients;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteService#sync(org.solmix.hola.rs.RSRequest)
     */
    @Override
    public Object sync(RSRequest call) throws RemoteServiceException{
        ExchangeClient client= getCurrentClient();
        try {
            return client.request(call, call.getTimeout()).get();
        } catch (TransportException e) {
           throw new  RemoteServiceException("Failed to call sync",e);
        }
    }
    @Override
    protected RSRequest createRemoteCall(final String method,
        final Object[] parameters,Class<?>[] types, final int timeOut) {
        RSRequestImpl _return= new RSRequestImpl(method,parameters,types,timeOut) ;
        _return.setProperty(RemoteInfo.PATH, info.getPath());
        _return.setProperty(RemoteInfo.VERSION, info.getVersion("0.0.0"));
        return _return;
    }
    private ExchangeClient getCurrentClient(){
        ExchangeClient currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[index.getAndIncrement() % clients.length];
        }
        
        return currentClient;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteService#async(org.solmix.hola.rs.RSRequest, org.solmix.hola.rs.RSRequestListener)
     */
    @Override
    public void async(RSRequest call, RSRequestListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteService#fireAsync(org.solmix.hola.rs.RSRequest)
     */
    @Override
    public void fireAsync(RSRequest call) throws RemoteServiceException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.AbstractRemoteService#getRemoteServiceID()
     */
    @Override
    protected RemoteServiceID getRemoteServiceID() {
        return getRemoteServiceReference().getID();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.AbstractRemoteService#getInterfaceClassNames()
     */
    @Override
    protected String[] getInterfaceClassNames() {
        return clazzs;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.AbstractRemoteService#getRemoteServiceReference()
     */
    @Override
    protected RemoteServiceReference<Object> getRemoteServiceReference() {
        return new HolaRemoteServiceReference<Object>(clazzs[0], info, manager);
    }

}
