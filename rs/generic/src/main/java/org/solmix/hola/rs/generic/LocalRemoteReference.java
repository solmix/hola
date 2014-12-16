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

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import org.solmix.commons.util.Reflection;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.rs.RemoteRequest;
import org.solmix.hola.rs.RemoteResponse;
import org.solmix.hola.rs.identity.RemoteServiceID;
import org.solmix.hola.rs.support.RemoteRequestImpl;
import org.solmix.hola.rs.support.RemoteResponseImpl;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月1日
 */

public class LocalRemoteReference<S> implements org.solmix.hola.rs.RemoteReference<S>

{

    private final HolaRemoteRegistration<S> registration;
    private final Object service;
    /**
     * @param holaRemoteServiceRegistration
     */
    public LocalRemoteReference(
        HolaRemoteRegistration<S> holaRemoteServiceRegistration)
    {
        this.registration=holaRemoteServiceRegistration;
        this.service=registration.getService();
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
     * @see org.solmix.hola.rs.generic.RemoteReference#isActive()
     */
    @Override
    public boolean isActive() {
        return registration!=null;
    }



    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteReference#getRemoteInfo()
     */
    @Override
    public RemoteInfo getRemoteInfo() {
        return registration.getRemoteInfo();
    }



    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteReference#doInvoke(org.solmix.hola.rs.RemoteRequest)
     */
    @Override
    public RemoteResponse doInvoke(RemoteRequest request) {
        RemoteRequestImpl req = (RemoteRequestImpl) request;
        Object[] args = (request.getParameters() == null) ? new Object[0] : request.getParameters();
        final Method method = Reflection.findMethod(service.getClass(),req.getMethod(), req.getParameterTypes());
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    if (!method.isAccessible())
                        method.setAccessible(true);
                    return null;
                }
            });
            return  new RemoteResponseImpl(method.invoke(service, args));
        } catch (Throwable e) {
            return  new RemoteResponseImpl(e);
        }
    }

}
