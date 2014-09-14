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

package org.solmix.hola.core;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Identifiable;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.security.ConnectSecurityContext;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月30日
 */

public interface ConnectContext extends Identifiable
{

    void connect(ID remoteID, ConnectSecurityContext securityContext)
        throws ConnectException;

    ID getTargetID();

    Namespace getRemoteNamespace();

    void disconnect();
    
    void addListener(ConnectListener listener);
    
    void removeListener(ConnectListener listener);
    
    /**
     * 销毁连接上下文
     */
    void destroy();
}
