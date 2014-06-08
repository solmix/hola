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
package org.solmix.hola.shared.generic;

import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.security.ConnectSecurityContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月17日
 */

public abstract class SharedProviderClient extends GenericProvider
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#connect(org.solmix.hola.core.identity.ID, org.solmix.hola.core.security.ConnectSecurityContext)
     */
    @Override
    public void connect(ID remoteID, ConnectSecurityContext securityContext)
        throws ConnectException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#getTargetID()
     */
    @Override
    public ID getTargetID() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#disconnect()
     */
    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

}
