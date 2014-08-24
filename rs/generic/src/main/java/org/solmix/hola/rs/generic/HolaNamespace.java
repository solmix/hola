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
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.core.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月13日
 */

public class HolaNamespace extends Namespace
{

    private static final long serialVersionUID = -3966244288414425751L;

    public static final String NAME = "remoteservice.hola";

    public static final String REMOTESERVICE_SCHEME = "hola";

    public HolaNamespace(String desc)
    {
        super(NAME, desc);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Namespace#getScheme()
     */
    @Override
    public String getScheme() {
        return REMOTESERVICE_SCHEME;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Namespace#createID(java.lang.Object[])
     */
    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
        if (parameters == null || parameters.length != 1)
            throw new IDCreateException("EndpointInfo incorrect for remote ID creation");
      try {
            return new HolaServiceID(this,  parameters[0].toString());
      } catch (Exception e) {
            throw new IDCreateException("Exception creating remoteID", e); 
      }
    }

}
