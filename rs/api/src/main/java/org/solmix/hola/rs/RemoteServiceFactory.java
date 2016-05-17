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
package org.solmix.hola.rs;

import java.util.Dictionary;

import org.solmix.runtime.Extension;


/**
 * 提供远程对象注册、引用、销毁等服务
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月17日
 */
@Extension(name="hola")
public interface RemoteServiceFactory {

    RemoteRegistration<?> register(String clazze,Object service,  Dictionary<String, ?> properties) throws RemoteException;
    
    <S> RemoteRegistration<S> register(Class<S> clazze,S service,  Dictionary<String, ?> properties) throws RemoteException;
    
    
    /**
     * 在配置中处理
     * @param clazz 带有注解的class
     * @return
     */
    //<S> RemoteReference<S> getReference(Class<S> clazz);
    
    <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties);
    
    <S> S getService(RemoteReference<S> reference);
    
    <S> RemoteService<S> getRemoteService(RemoteReference<S> reference);
    
    void addRemoteListener(RemoteListener listener);

    void removeRemoteListener(RemoteListener listener);
    
    void destroy();
}
