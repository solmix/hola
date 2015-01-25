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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.hola.rpc.RemoteListener;
import org.solmix.hola.rpc.RemoteReference;
import org.solmix.hola.rpc.RemoteReference.ReferenceType;
import org.solmix.hola.rpc.RemoteRegistration;
import org.solmix.hola.rpc.RpcException;
import org.solmix.hola.rpc.RpcManager;
import org.solmix.hola.rpc.support.RemoteReferenceImpl;
import org.solmix.hola.rpc.support.ServiceRegistry;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.bean.BeanConfigurer;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月19日
 */

public class HolaRpcManager implements RpcManager {

    private static final Logger LOG = LoggerFactory.getLogger(HolaRpcManager.class);

    private final Container container;

    private HolaServerFactory serverFactory;

    private final ServiceRegistry registry = new ServiceRegistry(this);

    public HolaRpcManager() {
        this(ContainerFactory.getThreadDefaultContainer());
    }

    public HolaRpcManager(Container container) {
        this.container = container;
    }

    @Override
    public RemoteRegistration<?> registerService(String clazze, Object service,
        Dictionary<String, ?> properties) throws RpcException {
        Assert.isNotNull(service, "register service is null");
        if (clazze == null) {
            throw new IllegalArgumentException("Service classe is not null.");
        }

        final String copy = new String(clazze.getBytes());
        clazze = copy;
        // 验证接口和实例是否对应
        final Class<?> clz = checkServiceClass(clazze, service);
        if (LOG.isInfoEnabled()) {
            LOG.info("Registering service :" + clz);
        }
        final HolaRemoteRegistration<Object> reg = new HolaRemoteRegistration<Object>(
            this, registry, clz, service);
        if (serverFactory != null) {
            reg.setServerFactory(serverFactory);
        }
        reg.register(properties);
        return reg;
    }

    @Override
    public <S> RemoteRegistration<S> registerService(Class<S> clazze,
        S service, Dictionary<String, ?> properties) throws RpcException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Registering service :" + clazze);
        }
        final HolaRemoteRegistration<S> reg = new HolaRemoteRegistration<S>(
            this, registry, clazze, service);
        if (serverFactory != null) {
            reg.setServerFactory(serverFactory);
        }
        reg.register(properties);
        return reg;
    }

    protected void unregisterService(HolaRemoteRegistration<?> reg) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Unregistering service :" + reg.getServiceName());
        }
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz,
        Dictionary<String, ?> properties) {
        
        return new DelegateRemoteReference<S>(this, clazz, properties);
    }

    @Override
    public <S> Collection<RemoteReference<S>> getReferences(Class<S> clazz,
        String filter) throws RpcException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RemoteReference<?> getReference(String clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getService(RemoteReference<S> reference) {
        if (reference == null) {
            throw new NullPointerException(
                "A null service reference is not allowed.");
        }
        Object refType = reference.getProperty(RemoteReference.ReferenceType.class.getName());
        S service = null;
        if (refType == ReferenceType.LOCAL) {
            if (reference instanceof RemoteReferenceImpl<?>) {
                service = (S) registry.getService((RemoteReferenceImpl<S>) reference);
            }
        }else if(refType == ReferenceType.REMOTE){
            
        }
        return service;
    }

    protected <S> S getRemoteService(DelegateRemoteReference<S> reference) {
        // HolaDelegate delegate = new HolaDelegate(reference);
        // delegate.getProxy(reference.get, type);
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

    public HolaServerFactory getServerFactory() {
        return serverFactory;
    }

    public void setServerFactory(HolaServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    /**
     * 检查服务是否为接口的实例
     * 
     * @param clazzes
     * @param service
     * @return
     */
    static Class<?> checkServiceClass(final String clazze, final Object service) {
        final ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return service.getClass().getClassLoader();
            }
        });
        try {
            final Class<?> serviceClazz = cl == null ? Class.forName(clazze)
                : cl.loadClass(clazze);
            if (!serviceClazz.isInstance(service)) {
                throw new IllegalArgumentException("Service=" + clazze
                    + " is invalid");
            }
            return serviceClazz;
        } catch (final ClassNotFoundException e) {
            // This check is rarely done
            if (extensiveCheckServiceClass(clazze, service.getClass())) {
                throw new IllegalArgumentException("Service=" + clazze
                    + " is invalid");
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
    static boolean extensiveCheckServiceClass(String clazz,
        Class<?> serviceClazz) {
        if (clazz.equals(serviceClazz.getName())) {
            return false;
        }
        final Class<?>[] interfaces = serviceClazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (!extensiveCheckServiceClass(clazz, interfaces[i])) {
                return false;
            }
        }
        final Class<?> superClazz = serviceClazz.getSuperclass();
        if (superClazz != null) {
            if (!extensiveCheckServiceClass(clazz, superClazz)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param serverFactory2
     */
    public void configureBean(Object bean) {
        BeanConfigurer bc = container.getExtension(BeanConfigurer.class);
        if (bc != null) {
            bc.configureBean(bean);
        }
    }
    /**   */
    public Container getContainer() {
        return container;
    }


    public static String serviceKey(String group,String serviceid,String version,Integer port) {
        StringBuilder buf = new StringBuilder();
        if (group != null && group.length() > 0) {
            buf.append(group);
            buf.append("/");
        }
        buf.append(serviceid);
        if (version != null && version.length() > 0 && !"0.0.0".equals(version)) {
            buf.append(":");
            buf.append(version);
        }
        buf.append(":");
        buf.append(port);
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rpc.RpcManager#destroy()
     */
    @Override
    public void destroy() {
        registry.destroy();
        
    }
}
