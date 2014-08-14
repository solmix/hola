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

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.rs.identity.RemoteServiceID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月1日
 */

public class HolaRemoteServiceReference<S> implements org.solmix.hola.rs.RemoteServiceReference<S>

{

    private final HolaRemoteServiceRegistration<S> registration;
    /**
     * @param holaRemoteServiceRegistration
     */
    public HolaRemoteServiceReference(
        HolaRemoteServiceRegistration<S> holaRemoteServiceRegistration)
    {
        this.registration=holaRemoteServiceRegistration;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceReference#getID()
     */
    @Override
    public RemoteServiceID getID() {
        return registration.getRemoteServiceID();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceReference#getServiceInterfaces()
     */
    @Override
    public String[] getServiceInterfaces() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceReference#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        return registration.getProperty(key);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceReference#getPropertyKeys()
     */
    @Override
    public String[] getPropertyKeys() {
        return registration.getPropertyKeys();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceReference#isActive()
     */
    @Override
    public boolean isActive() {
        return registration!=null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceReference#getProviderID()
     */
    @Override
    public ID getProviderID() {
        // TODO Auto-generated method stub
        return null;
    }

}