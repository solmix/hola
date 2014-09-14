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

package org.solmix.hola.discovery.provider;

import java.net.URI;

import org.solmix.hola.core.identity.AbstractNamespace;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.ServiceTypeImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年6月8日
 */

public class CompositeNamespace extends AbstractNamespace
{

    public static final String NAME = "discovery.composite";

    /**
     * 
     */
    private static final long serialVersionUID = 15502907010717760L;
    
    public CompositeNamespace(String description){
        super(NAME,description);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Namespace#getScheme()
     */
    @Override
    public String getScheme() {
        return "composite";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Namespace#createID(java.lang.Object[])
     */
    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
        if (parameters == null || parameters.length < 1
            || parameters.length > 2) {
            throw new IDCreateException(
                "parameter count must be non null and of length >= 1 and =< 2");
        } else if (parameters.length == 2 && parameters[0] instanceof String
            && parameters[1] instanceof URI) {
            return new CompositeServiceID(this, new ServiceTypeImpl(this,
                (String) parameters[0]), (URI) parameters[1]);
        } else if (parameters.length == 2
            && parameters[0] instanceof ServiceType
            && parameters[1] instanceof URI) {
            return new CompositeServiceID(this, (ServiceType) parameters[0],
                (URI) parameters[1]);
        } else if (parameters.length == 1
            && parameters[0] instanceof ServiceType) {
            final ServiceType iServiceTypeID = (ServiceType) parameters[0];
            return new ServiceTypeImpl(this, iServiceTypeID.getName());
        } else {
            throw new IDCreateException("wrong parameters");
        }
    }

}
