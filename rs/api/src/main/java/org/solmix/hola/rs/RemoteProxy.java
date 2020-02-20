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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.call.DefaultRemoteRequest;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteResponse;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterChain;
import org.solmix.hola.rs.filter.InvokeFilterFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.extension.ExtensionLoader;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月20日
 */

public class RemoteProxy implements InvocationHandler, Closeable,InvokeFilterChain
{

    private  RemoteService<?> remoteService;
    private List<InvokeFilter> filters;
    private InvokeFilterChain last;
    public RemoteProxy(RemoteService<?> remoteService,Container container)
    {
        this.remoteService=remoteService;
       List<?>   fstring =PropertiesUtils.getCommaSeparatedList(remoteService.getServiceProperties(), HOLA.FILTER_KEY);
       if(fstring!=null&&container!=null){
           filters = new ArrayList<InvokeFilter>();
           ExtensionLoader<InvokeFilterFactory> loader = container.getExtensionLoader(InvokeFilterFactory.class);
           for(Object f:fstring){
               InvokeFilterFactory factory = loader.getExtension(f.toString());
               if(factory!=null){
                   InvokeFilter ivf= factory.create(remoteService.getServiceProperties());
                   filters.add(ivf);
               }
           }
       }
    }

    @Override
    public void close() throws IOException {
        if (remoteService != null) {
            remoteService.destroy();
            remoteService = null;
        }
        if(last!=null){
            last=null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Map<String, Object> requestContext = new HashMap<String, Object>();
        DefaultRemoteRequest request = new DefaultRemoteRequest(method, args, requestContext);
        if (method.getDeclaringClass().equals(Object.class) || method.getDeclaringClass().equals(Closeable.class)) {
            return method.invoke(this);
        } else if (method.getDeclaringClass().isInstance(remoteService)) {
            return method.invoke(remoteService, args);
        }
        request.setContextAttr(Method.class.getName(), method);
        boolean isAsync = isAsync(method);
        request.setAsync(isAsync);
        if (filters != null) {
            Object response;
            last = this;
            for (final InvokeFilter filter : filters) {
                final InvokeFilterChain next = last;
                last = new InvokeFilterChain() {

                    @Override
                    public Object doFilter(RemoteRequest request) throws Throwable {
                        return filter.doFilter(request, next);
                    }
                };
            }
            response = last.doFilter(request);
            if (request.isAsync()) {
                return response;
            } else if (response instanceof RemoteResponse) {
                RemoteResponse f = (RemoteResponse) response;
                if (f.hasException()) {
                    throw f.getException();
                } else {
                    return f.getValue();
                }
            }else{
                return null;
            }
        } else {
            try {
                if (isAsync) {
                    return remoteService.async(request);
                } else {
                    RemoteResponse response = remoteService.sync(request);
                    if (response.hasException()) {
                        throw response.getException();
                    } else {
                        return response.getValue();
                    }
                }
            } catch (RemoteException e) {
                throw e;
            }
        }
    }

    protected Object adapteObject(Object o) {
        return o;
    }
    
    boolean isAsync(Method m) {
        return m.getName().endsWith("Async") && (Future.class.equals(m.getReturnType()) );
    }


    @Override
    public Object doFilter(RemoteRequest request) throws Throwable {
        
        try{
            if(request.isAsync()){
               return remoteService.async(request);
            }else{
                return remoteService.sync(request);
            }
        }catch(RemoteException e){
            throw e;
        }
    }

}
