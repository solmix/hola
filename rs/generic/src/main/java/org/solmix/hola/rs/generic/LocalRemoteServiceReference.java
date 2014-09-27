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

import org.solmix.hola.rs.identity.RemoteServiceID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月1日
 */

public class LocalRemoteServiceReference<S> implements org.solmix.hola.rs.RemoteReference<S>

{

    private final HolaRemoteRegistration<S> registration;
    /**
     * @param holaRemoteServiceRegistration
     */
    public LocalRemoteServiceReference(
        HolaRemoteRegistration<S> holaRemoteServiceRegistration)
    {
        this.registration=holaRemoteServiceRegistration;
    }


   
    @Override
    public RemoteServiceID getID() {
        return registration.getID();
    }

    @Override
    public String[] getInterfaces() {
        return registration.getClasses();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteReference#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        return registration.getProperty(key);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteReference#getPropertyKeys()
     */
    @Override
    public String[] getPropertyKeys() {
        return registration.getPropertyKeys();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteReference#isActive()
     */
    @Override
    public boolean isActive() {
        return registration!=null;
    }

}
