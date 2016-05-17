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

import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.runtime.Container;
import org.solmix.runtime.helper.ProxyHelper;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月20日
 */

public class RemoteProxyFactory 
{

    private RemoteProxyFactory()
    {
    }

    public static <T> T getProxy(RemoteService<T> remote){
        return getProxy(remote);
    }
    /**创建远程调用的本地代理*/
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(RemoteService<T> remote,Container container){
        ClassLoaderHolder loader = null;
        try {
            if (container != null) {
                ClassLoader cl = container.getExtension(ClassLoader.class);
                if (cl != null) {
                    loader = ClassLoaderUtils.setThreadContextClassloader(cl);
                }
            }
            RemoteProxy proxyHandler = new RemoteProxy(remote,container);
            Class<?> classes[] = new Class[] {remote.getServiceClass(), Closeable.class, RemoteService.class};
            Object object = ProxyHelper.getProxy(remote.getServiceClass().getClassLoader(), classes, proxyHandler);
            return (T) object;
        } finally {
            if (loader != null) {
                loader.reset();
            }
        }
    }
    
}
