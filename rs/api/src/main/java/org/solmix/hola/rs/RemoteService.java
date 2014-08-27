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

import java.util.concurrent.Future;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月29日
 */

public interface RemoteService
{

    /**
     * 同步调用远程方法，返回调用结果
     * 
     * @param call
     * @return
     */
    Object sync(RSRequest call) throws RemoteServiceException;

    /**
     * 异步调用远程方法
     * 
     * @param call
     * @return
     */
    void async(RSRequest call,RSRequestListener listener);
    
    /**
     * 立即返回调用结果
     * @param call
     * @return
     */
    Future<Object> async(RSRequest call);

    /**
     * 异步调用远程方法，区别于｛@link {@link #async(RSRequest)} 的是，该方法无须返回值，也不返回异常．
     * 
     * @param call
     */
    void fireAsync(RSRequest call) throws RemoteServiceException;

    /**
     * 返回远程服务接口的本地代理对象
     * 
     * @return
     * @throws RemoteServiceException
     */
    Object getProxy() throws RemoteServiceException;

    /**
     * 返回远程服务接口的本地代理对象
     * 
     * @param classLoader
     * @param interfaceClasses
     * @return
     * @throws RemoteServiceException
     */
    Object getProxy(ClassLoader classLoader, Class<?>[] interfaceClasses)
        throws RemoteServiceException;
}
