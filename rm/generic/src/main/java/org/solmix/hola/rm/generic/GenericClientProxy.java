/**
 * Copyright (c) 2014 The Solmix Project
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

package org.solmix.hola.rm.generic;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;

import org.solmix.hola.rm.ClientProxy;
import org.solmix.hola.rm.RemoteException;
import org.solmix.runtime.exchange.Client;
import org.solmix.runtime.exchange.ClientCallback;
import org.solmix.runtime.exchange.Endpoint;
import org.solmix.runtime.exchange.invoker.OperationDispatcher;
import org.solmix.runtime.exchange.model.OperationInfo;
import org.solmix.runtime.interceptor.Fault;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月7日
 */

public class GenericClientProxy extends ClientProxy {

    /**
     * @param c
     */
    public GenericClientProxy(Client c) {
        super(c);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        if (client == null) {
            throw new IllegalStateException("The client has been closed.");
        }
        Endpoint endpoint = getClient().getEndpoint();
        OperationDispatcher dispatcher = (OperationDispatcher) endpoint.getService().get(
            OperationDispatcher.class.getName());
        Object[] params = args;
        if (null == params) {
            params = new Object[0];
        }
        try {
            if (method.getDeclaringClass().equals(Object.class)
                || method.getDeclaringClass().equals(Closeable.class)) {
                return method.invoke(this);
            } else if (method.getDeclaringClass().isInstance(client)) {
                return method.invoke(client, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        OperationInfo oi = dispatcher.getOperation(method);
        if (oi == null) {
            throw new Fault("No operationInfo for method " + method.getName());
        }
        boolean isAsync = isAsync(method);

        Object result = null;
        try {
            if (isAsync) {
                result = invokeAsync(method, oi, params);
            } else {
                result = invokeSync(method, oi, params);
            }
        } catch (RemoteException wex) {
            throw wex;
        }
    }
    
    
    boolean isAsync(Method m) {
        return m.getName().endsWith("Async")
            && (Future.class.equals(m.getReturnType()) 
                || ResponseFuture.class.equals(m.getReturnType()));
    }
    
    private Object invokeAsync(Method method, OperationInfo oi, Object[] params) throws Exception {

        client.setExecutor(getClient().getEndpoint().getExecutor());
        
        AsyncHandler<Object> handler;
        if (params.length > 0 && params[params.length - 1] instanceof AsyncHandler) {
            handler = (AsyncHandler<Object>)params[params.length - 1];
            Object[] newParams = new Object[params.length - 1];
            for (int i = 0; i < newParams.length; i++) {
                newParams[i] = params[i];
            }
            params = newParams;
        } else {
            handler = null;
        }
        ClientCallback callback = new RemoteCallback<Object>(handler, this);
             
        ResponseFuture<Object> ret = new RemoteResponse<Object>(callback);
        client.invoke(callback, oi, params);
        return ret;
    }
}
