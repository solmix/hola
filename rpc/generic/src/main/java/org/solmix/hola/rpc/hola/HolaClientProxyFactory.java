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

import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.hola.rpc.ClientProxy;
import org.solmix.hola.rpc.RpcClientFactory;
import org.solmix.hola.rpc.RpcClientProxyFactory;
import org.solmix.runtime.exchange.Client;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月7日
 */

public class HolaClientProxyFactory extends RpcClientProxyFactory {

    private static final long serialVersionUID = -3860608874988281080L;
    
    public HolaClientProxyFactory(){
        super(new HolaClientFactory());
    }
    
    public HolaClientProxyFactory(RpcClientFactory factory) {
       super(factory);
    }

    /**
     * @return
     */
    @Override
    public synchronized Object create() {
        ClassLoaderHolder orig = null;
        ClassLoader loader = null;
        try {
            if(getContainer()!=null){
                loader = getContainer().getExtension(ClassLoader.class);
                if (loader != null) {
                    orig = ClassLoaderUtils.setThreadContextClassloader(loader);
                }
            }
            return super.create();
        } finally {
            if (orig != null) {
                orig.reset();
            }
        }
    }
    @Override
    protected ClientProxy createClientProxy(Client c) {
        return new ClientProxy(c);
    }
    
}
