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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.solmix.hola.rs.call.DefaultRemoteRequest;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteResponse;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月20日
 */

public class RemoteProxy implements InvocationHandler, Closeable
{

    private RemoteService<?> remoteService;
    public RemoteProxy(RemoteService<?> remoteService)
    {
        this.remoteService=remoteService;
    }

    @Override
    public void close() throws IOException {
        if (remoteService != null) {
            remoteService.destroy();
            remoteService = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String,Object> requestContext=new HashMap<String,Object>();
        RemoteRequest request  = new DefaultRemoteRequest(method, args,requestContext);
        
        if (method.getDeclaringClass().equals(Object.class) || method.getDeclaringClass().equals(Closeable.class)) {
            return method.invoke(this);
        } else if (method.getDeclaringClass().isInstance(remoteService)) {
            return method.invoke(remoteService, args);
        }
        request.getRequestContext().put(Method.class.getName(), method);
        boolean isAsync = isAsync(method);
        try{
            if(isAsync){
               return remoteService.async(request);
            }else{
                RemoteResponse response= remoteService.sync(request);
                if(response.hasException()){
                    throw response.getException();
                }else{
                    return response.getValue();
                }
            }
        }catch(RemoteException e){
            throw e;
        }
    }

    protected Object adapteObject(Object o) {
        return o;
    }
    
    boolean isAsync(Method m) {
        return m.getName().endsWith("Async")
            && (Future.class.equals(m.getReturnType()) );
    }

}
