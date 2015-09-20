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

package org.solmix.hola.rs.support;

import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Dictionary;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.model.RemoteServiceInfo;
import org.solmix.hola.rs.RemoteListener;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月16日
 */
@ThreadSafe
public abstract class AbstractRemoteServiceFactory implements RemoteServiceFactory,ContainerAware
{

    protected volatile ServiceRegistry registry = new ServiceRegistry(this);

    protected Container container;

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public RemoteRegistration<?> register(String clazz, Object service, Dictionary<String, ?> properties) throws RemoteException {

        if (StringUtils.isEmpty(clazz)) {
            throw new IllegalArgumentException("register class  is null");
        }
        @SuppressWarnings("unchecked")
        Class<Object> cls = (Class<Object>) checkServiceClass(clazz, service);
        if (cls == null) {
            throw new RemoteException("No found class :" + clazz);
        }
        RemoteRegistration<Object> reg = register(cls, service, properties);
        return reg;
    }

    @Override
    public <S> RemoteRegistration<S> register(Class<S> clazz, S service, Dictionary<String, ?> properties) throws RemoteException {
        Assert.isNotNull(service, "register service is null");
        Assert.isNotNull(clazz, "register class is null");
        return doRegister(clazz, service, properties);
    }

    @SuppressWarnings("rawtypes")
    public abstract <S> RemoteRegistration<S> doRegister(Class<S> clazz, S service,  Dictionary properties) throws RemoteException;

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties) {
        RemoteServiceInfo info = prepare(properties);
        return getReference(clazz, info);
    }

    public abstract <S> RemoteReference<S> getReference(Class<S> clazz, RemoteServiceInfo info);

    @Override
    public <S> Collection<RemoteReference<S>> getReferences(Class<S> clazz, String filter) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <S> S getService(RemoteReference<S> reference) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRemoteListener(RemoteListener listener) {
        registry.addRemoteListener(listener);
    }

    @Override
    public void removeRemoteListener(RemoteListener listener) {
        registry.removeRemoteListener(listener);
    }


    @Override
    public void destroy() {
        registry.destroy();
    }

    /**
     * 检查服务是否为接口的实例
     * 
     * @param clazzes
     * @param service
     * @return
     */
    static Class<?> checkServiceClass(final String clazz, final Object service) {
        final ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return service.getClass().getClassLoader();
            }
        });
        try {
            Class<?> serviceClazz = null;
            try {
                serviceClazz = cl == null ? Class.forName(clazz) : cl.loadClass(clazz);
            } catch (ClassNotFoundException e) {/* IGNORE */
            }
            if (serviceClazz == null) {
                ClassLoader defaultLoader = ClassLoaderUtils.getDefaultClassLoader();
                serviceClazz = defaultLoader.loadClass(clazz);
            }
            if (!serviceClazz.isInstance(service)) {
                return serviceClazz;
            }
        } catch (final ClassNotFoundException e) {
            // 根据服务的父类或者接口来查找
            Class<?> auto = extensiveCheckServiceClass(clazz, service.getClass());
            if (auto != null) {
                return auto;
            }
        }
        return null;
    }

    /**
     * 检查父类和接口
     * 
     * @param clazz
     * @param serviceClazz
     * @return
     */
    static Class<?> extensiveCheckServiceClass(String clazz, Class<?> serviceClazz) {
        if (clazz.equals(serviceClazz.getName())) {
            return serviceClazz;
        }
        final Class<?>[] interfaces = serviceClazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> inf = extensiveCheckServiceClass(clazz, interfaces[i]);
            if (inf != null) {
                return inf;
            }
        }
        final Class<?> superClazz = serviceClazz.getSuperclass();
        if (superClazz != null) {
            Class<?> sup = extensiveCheckServiceClass(clazz, superClazz);
            if (sup != null) {
                return sup;
            }
        }
        return null;
    }
}
