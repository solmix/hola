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

import java.util.Map;

import org.osgi.framework.InvalidSyntaxException;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Identifiable;
import org.solmix.hola.core.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月30日
 */

public interface RemoteServiceProvider extends Identifiable
{


    /**
     * 注册一个远程服务，服务提供者通过该方法将本地服务暴露给远程消费者．
     * 
     * @param clazzes
     * @param service
     * @param properties
     * @return
     */
    RemoteServiceRegistration<?> registerRemoteService(String[] clazzes,
        Object service, Map<String, ?> properties);

    /**
     * 获取远程服务引用
     * 
     * @param target
     * @param idFilter
     * @param clazz
     * @param filter
     * @return
     * @throws InvalidSyntaxException
     * @throws RemoteConnectException
     */
/*    RemoteServiceReference<?>[] getRemoteServiceReferences(ID target,
        ID[] idFilter, String clazz, String filter)
        throws InvalidSyntaxException, RemoteConnectException;
*/
    /**
     * 获取远程服务引用
     * 
     * @param target
     * @param clazz
     * @param filter
     * @return
     * @throws InvalidSyntaxException
     * @throws RemoteConnectException
     */
    RemoteServiceReference<?> getRemoteServiceReferences(ID target,
        String clazz) throws InvalidSyntaxException,
        RemoteConnectException;

    RemoteServiceReference<?>[] getAllRemoteServiceReferences(String clazz,
        String filter) throws InvalidSyntaxException;

    Namespace getRemoteServiceNamespace();

//    RemoteServiceReference<?> getRemoteServiceReference(RemoteServiceID serviceID);

    /**
     * 根据远程服务引用，获取远程服务，调用远程服务成功后，如果不在使用服务需要｛@link
     * {@link #ungetRemoteService(RemoteServiceReference)}注销对该服务的调用
     * 
     * @param reference
     * @return
     */
    RemoteService getRemoteService(RemoteServiceReference<?> reference);

    boolean ungetRemoteService(RemoteServiceReference<?> reference);
    
    RemoteFilter createRemoteFilter(String filter) throws InvalidSyntaxException;
    
    void destroy();
    void addRemoteServiceListener(RemoteServiceListener listener);

    void removeRemoteServiceListener(RemoteServiceListener listener);
}