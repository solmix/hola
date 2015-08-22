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

package org.solmix.hola.rpc;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.solmix.exchange.ClientCallback;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月7日
 */

public class RpcCallback<T> extends ClientCallback {

    private final AsyncCallback<T> callback;

    private final Object proxy;

    public RpcCallback(AsyncCallback<T> callback, Object p) {
        this.callback = callback;
        this.proxy = p;
    }
    
    @Override
    public void handleResponse(final Map<String, Object> ctx, Object[] res) {
        context = ctx;
        result = res;
        if(callback != null){
            callback.onSuccess(new RpcResponse<T>() {

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return cancelled;
                }

                @Override
                public boolean isCancelled() {
                   cancelled=true;
                    return true;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @SuppressWarnings("unchecked")
                @Override
                public T get() throws InterruptedException, ExecutionException {
                    return (T)result[0];
                }
                @SuppressWarnings("unchecked")
                @Override
                public T get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException,
                    TimeoutException {
                    return (T)result[0];
                }

                @Override
                public Map<String, Object> getContext() {
                    return context;
                }
            });
        }
        done = true;
        synchronized (this) {
            notifyAll();
        }
    }
    
    @Override
    public void handleException(Map<String, Object> ctx,final Throwable ex) {
        context = ctx;
        exception = ex;
        if(callback != null){
            callback.onSuccess(new RpcResponse<T>() {

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    cancelled = true;
                    return true;
                }

                @Override
                public boolean isCancelled() {
                    return cancelled;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public T get() throws InterruptedException, ExecutionException {
                   throw new ExecutionException(ex);
                }
                
                @Override
                public T get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException,
                    TimeoutException {
                    throw new ExecutionException(ex);
                }

                @Override
                public Map<String, Object> getContext() {
                    return context;
                }
            });
        }
        done = true;
        synchronized (this) {
            notifyAll();
        }
    }

}
