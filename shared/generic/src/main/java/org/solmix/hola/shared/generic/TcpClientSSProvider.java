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

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.security.ConnectSecurityContext;
import org.solmix.hola.shared.SharedServiceProviderConfig;
import org.solmix.hola.shared.generic.support.Channel;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月17日
 */

public class TcpClientSSProvider extends ClientSSProvider
{
    public static final int DEFAULT_TCP_CONNECT_TIMEOUT = 30000;
    /**
     * @param config
     */
    public TcpClientSSProvider(SharedServiceProviderConfig config)
    {
        super(config);
        // TODO Auto-generated constructor stub
    }
    @Override
    protected int getConnectTimeout() {
        return DEFAULT_TCP_CONNECT_TIMEOUT;
  }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.ClientSSProvider#createChannel(org.solmix.hola.core.identity.ID, org.solmix.hola.core.security.ConnectSecurityContext)
     */
    @Override
    protected Channel createChannel(ID remoteID,
        ConnectSecurityContext securityContext) {
        // TODO Auto-generated method stub
        return null;
    }

}
