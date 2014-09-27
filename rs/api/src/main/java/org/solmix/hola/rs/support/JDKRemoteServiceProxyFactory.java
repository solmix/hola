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
package org.solmix.hola.rs.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.solmix.hola.rs.RemoteProxyFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月1日
 */

public class JDKRemoteServiceProxyFactory implements RemoteProxyFactory
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteProxyFactory#createProxy(java.lang.ClassLoader, java.lang.Class[], java.lang.reflect.InvocationHandler)
     */
    @Override
    public Object createProxy(ClassLoader classloader, Class<?>[] interfaces,
        InvocationHandler handler) {
        return Proxy.newProxyInstance(classloader, interfaces, handler);
    }

}
