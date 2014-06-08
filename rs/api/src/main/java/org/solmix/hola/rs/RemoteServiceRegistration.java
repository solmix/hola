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

import java.util.Dictionary;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.rs.identity.RemoteServiceID;

/**
 * 远程服务注册返回结果,在调用
 * {@link RemoteServiceProvider#registerRemoteService(String[], Object, Dictionary)}
 * 后返回,稍后可以通过该结果注销服务.
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月29日
 */

public interface RemoteServiceRegistration<S>
{

    RemoteServiceID getID();

    /**
     * 返回注冊该服务的节点ＩＤ
     * 
     * @return
     */
    ID getProviderID();

    Object getProperty(String key);

    String[] getPropertyKeys();

    /**
     * @param properties
     */
    void setProperties(Dictionary<String, Object> properties);

    /**
     * 返回服务引用
     * 
     * @return
     */
    RemoteServiceReference<S> getReference();

    /**
     * 注销服务
     */
    void unregister();
}
