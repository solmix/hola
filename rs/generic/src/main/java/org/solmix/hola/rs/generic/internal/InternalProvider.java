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
package org.solmix.hola.rs.generic.internal;

import java.util.Dictionary;

import org.osgi.framework.InvalidSyntaxException;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.rs.RemoteConnectException;
import org.solmix.hola.rs.RemoteFilter;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceListener;
import org.solmix.hola.rs.RemoteServiceProvider;
import org.solmix.hola.rs.RemoteServiceReference;
import org.solmix.hola.rs.RemoteServiceRegistration;
import org.solmix.hola.rs.identity.RemoteServiceID;
import org.solmix.hola.shared.BaseSharedService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月17日
 */

public class InternalProvider extends BaseSharedService implements RemoteServiceProvider
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#registerRemoteService(java.lang.String[], java.lang.Object, java.util.Dictionary)
     */
    @Override
    public RemoteServiceRegistration<?> registerRemoteService(String[] clazzes,
        Object service, Dictionary<String, ?> properties) {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceReferences(org.solmix.hola.core.identity.ID, org.solmix.hola.core.identity.ID[], java.lang.String, java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getRemoteServiceReferences(ID target,
        ID[] idFilter, String clazz, String filter)
        throws InvalidSyntaxException, RemoteConnectException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceReferences(org.solmix.hola.core.identity.ID, java.lang.String, java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getRemoteServiceReferences(ID target,
        String clazz, String filter) throws InvalidSyntaxException,
        RemoteConnectException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getAllRemoteServiceReferences(java.lang.String, java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getAllRemoteServiceReferences(
        String clazz, String filter) throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceNamespace()
     */
    @Override
    public Namespace getRemoteServiceNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceReference(org.solmix.hola.rs.identity.RemoteServiceID)
     */
    @Override
    public RemoteServiceReference<?> getRemoteServiceReference(
        RemoteServiceID serviceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteService(org.solmix.hola.rs.RemoteServiceReference)
     */
    @Override
    public RemoteService getRemoteService(RemoteServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#ungetRemoteService(org.solmix.hola.rs.RemoteServiceReference)
     */
    @Override
    public boolean ungetRemoteService(RemoteServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#createRemoteFilter(java.lang.String)
     */
    @Override
    public RemoteFilter createRemoteFilter(String filter)
        throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#addRemoteServiceListener(org.solmix.hola.rs.RemoteServiceListener)
     */
    @Override
    public void addRemoteServiceListener(RemoteServiceListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#removeRemoteServiceListener(org.solmix.hola.rs.RemoteServiceListener)
     */
    @Override
    public void removeRemoteServiceListener(RemoteServiceListener listener) {
        // TODO Auto-generated method stub

    }

}
