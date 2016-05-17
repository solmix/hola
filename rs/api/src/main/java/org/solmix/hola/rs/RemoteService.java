/*
 * Copyright 2015 The Solmix Project
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

import org.solmix.exchange.ClientCallback;
import org.solmix.exchange.Node;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteRequestListener;
import org.solmix.hola.rs.call.RemoteResponse;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月16日
 */

public interface RemoteService<T> extends Node
{
    Class<T> getServiceClass();
    /**
     * 同步调用远程方法，返回调用结果
     * 
     * @param call
     * @return
     * @see {@link #invoke(ClientCallback, RemoteRequest, boolean)}
     */
    RemoteResponse sync(RemoteRequest call) throws RemoteException;

    /**
     * 异步调用远程方法
     * 
     * @param call
     * @return
     * @see {@link #invoke(ClientCallback, RemoteRequest, boolean)}
     */
   void async(RemoteRequest call,RemoteRequestListener listener);
    
    /**
     * 立即返回调用结果
     * 
     * @param call
     * @return
     * @see {@link #invoke(ClientCallback, RemoteRequest, boolean)}
     */
    Future<RemoteResponse> async(RemoteRequest call);

    /**
     * 异步调用远程方法，区别于{@link #async(RemoteRequest)} 的是，该方法无须返回值，也不返回异常．
     * 
     * @param call
     * @see {@link #invoke(ClientCallback, RemoteRequest, boolean)}
     */
    void fireAsync(RemoteRequest call) throws RemoteException;
    
    ServiceProperties getServiceProperties();
    
    /**
     * 远程服务实际调用的接口
     * 
     * @param callback
     * @param request
     * @param oneway
     * @return
     * @throws RemoteException
     */
    @Deprecated
    Object[] invoke(ClientCallback callback, RemoteRequest request, boolean oneway) throws RemoteException ;
}
