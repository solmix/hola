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

import java.util.Arrays;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.identity.RemoteServiceID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月1日
 */

public class HolaRemoteRegistration<S> implements
    RemoteRegistration<S>, java.io.Serializable
{
    private static final long serialVersionUID = 319786149652327809L;

    protected RemoteServiceID id;

    transient protected Object registrationLock = new Object();

    /** The registration state */
    protected int state = REGISTERED;

    public static final int REGISTERED = 0x00;

    public static final int UNREGISTERING = 0x01;

    public static final int UNREGISTERED = 0x02;

    protected transient LocalRemoteReference<S> reference;

    private int serviceRanking;

    private final String[] clazzes;
    private final RemoteInfo remoteInfo;


    /**
     * @param holaRemoteServiceManager
     * @param clazzes2
     * @param service2
     * @param info
     */
    public HolaRemoteRegistration(
        HolaRemoteManager manager, String[] clazzes,
        Object service, RemoteInfo info)
    {
        this.manager=manager;
        this.service = service;
        this.reference = new LocalRemoteReference<S>(this);
        this.clazzes = clazzes;
        this.remoteServiceID = manager.createRemoteServiceID(info);
        this.remoteInfo=info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteRegistration#getReference()
     */
    @Override
    public RemoteReference<S> getReference() {
        if (reference == null) {
            synchronized (this) {
                reference = new LocalRemoteReference<S>(this);
            }
        }
        return reference;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteRegistration#unregister()
     */
    @Override
    public void unregister() {
       if(manager!=null)
           manager.unregisterRemoteService(this);

    }

    @Override
    public String toString() {
       StringBuffer sb = new StringBuffer();
       sb.append("HolaRemoteRegistration[")
       .append("RemoteServiceID=").append(remoteServiceID)
       .append(";ServiceRanking=").append(serviceRanking)
       .append(";classes=").append(Arrays.toString(clazzes))
       .append("]");
       return sb.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (!(o.getClass().equals(this.getClass())))
            return false;
        return getID().equals(((HolaRemoteRegistration<?>) o).getID());
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    protected String[] getClasses() {
        return clazzes;
    }

    private final Object service;

    private final HolaServiceID remoteServiceID;
    
    private final HolaRemoteManager manager;

  
    public Object getService(){
        return service;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteRegistration#getID()
     */
    @Override
    public RemoteServiceID getID() {
        return remoteServiceID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteRegistration#getRemoteInfo()
     */
    @Override
    public RemoteInfo getRemoteInfo() {
        return remoteInfo;
    }
}
