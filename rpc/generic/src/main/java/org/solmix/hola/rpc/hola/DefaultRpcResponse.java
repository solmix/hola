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
package org.solmix.hola.rpc.hola;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.solmix.hola.rpc.RpcResponse;
import org.solmix.runtime.exchange.ClientCallback;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月7日
 */

public class DefaultRpcResponse<V> implements RpcResponse<V> {
    ClientCallback callback;
    public DefaultRpcResponse(ClientCallback callback){
        this.callback=callback;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return callback.cancel(mayInterruptIfRunning);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return callback.isCancelled();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone() {
        return callback.isDone();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.concurrent.Future#get()
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get() throws InterruptedException, ExecutionException {
        return (V)callback.get()[0];
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException,
        ExecutionException, TimeoutException {
        return (V)callback.get(timeout, unit)[0];
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rpc.hola.RpcResponse#getContext()
     */
    @Override
    public Map<String, Object> getContext() {
        try {
            return callback.getResponseContext();
        } catch (Exception ex) {
            return null;
        }
    }

}
