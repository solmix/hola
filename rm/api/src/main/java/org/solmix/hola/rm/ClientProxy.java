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
package org.solmix.hola.rm;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.solmix.runtime.exchange.Client;
import org.solmix.runtime.exchange.Endpoint;
import org.solmix.runtime.exchange.invoker.OperationDispatcher;
import org.solmix.runtime.exchange.model.OperationInfo;
import org.solmix.runtime.interceptor.Fault;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月7日
 */

public class ClientProxy implements InvocationHandler,Cloneable{

    protected Client client;
    private Endpoint endpoint;


    public ClientProxy(Client c) {
        endpoint = c.getEndpoint();
        client = c;
    }
    public void close() throws IOException {
        if (client != null) {
            client.destroy();
            client = null;
            endpoint = null;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (client == null) {
            throw new IllegalStateException("The client has been closed.");
        }
        if (method.getDeclaringClass().equals(Object.class)
            || method.getDeclaringClass().equals(Closeable.class)) {
            return method.invoke(this);
        } else if (method.getDeclaringClass().isInstance(client)) {
            return method.invoke(client, args);
        }
        
        OperationDispatcher dispatcher = (OperationDispatcher)endpoint.getService().get(OperationDispatcher.class
                                                                                      .getName());
        OperationInfo oi = dispatcher.getOperation(method);
        if (oi == null) {
            throw new Fault("No operationInfo for method "+method.getName());
        }

        Object[] params = args;
        if (null == params) {
            params = new Object[0];
        }

        Object o = invokeSync(method, oi, params);
        return adjustObject(o); 
    }
    protected Object adjustObject(Object o) {
        return o;
    }

    public Object invokeSync(Method method, OperationInfo oi, Object[] params)
        throws Exception {
        if (client == null) {
            throw new IllegalStateException("The client has been closed.");
        }
        Object rawRet[] = client.invoke(oi, params);

        if (rawRet != null && rawRet.length > 0) {
            return rawRet[0];
        } else {
            return null;
        }
    }
    public Map<String, Object> getRequestContext() {
        if (client == null) {
            throw new IllegalStateException("The client has been closed.");
        }
        return client.getRequestContext();
    }
    public Map<String, Object> getResponseContext() {
        if (client == null) {
            throw new IllegalStateException("The client has been closed.");
        }
        return client.getResponseContext();
    }

    public Client getClient() {
        return client;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public static Client getClient(Object o) {
        return ((ClientProxy)Proxy.getInvocationHandler(o)).getClient();
    }

}
