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

package org.solmix.hola.rs;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.identity.RemoteServiceID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月29日
 */

public interface RemoteReference<S>
{

    RemoteServiceID getID();
    
    RemoteInfo getRemoteInfo();
    

    String[] getInterfaces();
    
    RemoteResponse doInvoke(RemoteRequest request);

    /**
     * 该服务引用是否有效
     * @return
     */
    boolean isActive();
}
