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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteListener;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteReference.ReferenceType;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.extension.ExtensionLoader;
import org.solmix.runtime.helper.ProxyHelper;

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
        RemoteRegistration<S> reg;
        //Invoker filter
        List<?>   fstring =PropertiesUtils.getCommaSeparatedList(properties, HOLA.FILTER_KEY);
        if(fstring!=null&&fstring.size()>0){
            List<InvokeFilter> filters = new ArrayList<InvokeFilter>();
            ExtensionLoader<InvokeFilterFactory> loader = container.getExtensionLoader(InvokeFilterFactory.class);
            for(Object f:fstring){
                InvokeFilterFactory factory = loader.getExtension(f.toString());
                if(factory!=null){
                    InvokeFilter ivf =factory.create(properties);
                    filters.add(ivf);
                }
            }
            reg= doRegister(clazz, getProxy(clazz,service,filters,properties), properties);
        }else{
            reg= doRegister(clazz, service, properties);
        }
        if(reg!=null){
            registry.addServiceRegistration(reg);
        }
        return reg;
    }

    @SuppressWarnings("unchecked")
    private <S> S getProxy(Class<S> clazz,S service, List<InvokeFilter> filters,Dictionary properties) {
        InvokeFilterWrapper wrapper = new InvokeFilterWrapper(service,filters,properties);
        return (S)ProxyHelper.getProxy(clazz.getClassLoader(), new Class[]{ clazz}, wrapper);
    }

 

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties) {
        RemoteReference<S> refer = doReference(clazz,properties);
        if(refer!=null){
            registry.addServiceReference(refer);
        }
        return refer;
    }
   
    protected <S> RemoteReference<S> doReference(Class<S> clazz, Dictionary<String, ?> properties) {
       return new RemoteReferenceImpl<S>(clazz,registry,properties,this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getService(RemoteReference<S> reference) {
        Assert.assertNotNull(reference,"RemoteReference");
        ReferenceType type = reference.getReferenceType();
        if(type==ReferenceType.LOCAL){
           return (S) registry.getService((RemoteReferenceHolder<S>)reference);
        }else if(type==ReferenceType.REMOTE){
            return doGetService(reference);
        }
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
    public <S> RemoteService<S> getRemoteService(RemoteReference<S> reference) {
        ReferenceType type = reference.getReferenceType();
        if(type==ReferenceType.LOCAL){
            throw new IllegalArgumentException("Reference is Local"); 
        }else if(type==ReferenceType.REMOTE){
            RemoteReferenceImpl<S> impl =(RemoteReferenceImpl<S>)reference;
            if(impl.getRemoteService()!=null){
                return impl.getRemoteService();
            }else{
                RemoteService<S> rs = doGetRemoteService(impl);
                impl.setRemoteService(rs);
            }
            return impl.getRemoteService();
        }
        return null;
    }

    @Override
    public void destroy() {
        registry.destroy();
    }
    protected abstract <S> S doGetService(RemoteReference<S> reference) throws RemoteException ;
    
    protected abstract <S> RemoteService<S> doGetRemoteService(RemoteReferenceImpl<S> reference) throws RemoteException ;
    
    @SuppressWarnings("rawtypes")
    public abstract <S> RemoteRegistration<S> doRegister(Class<S> clazz, S service,  Dictionary properties) throws RemoteException;


    /**
     * 检查服务是否为接口的实例
     * 
     * @param clazzes
     * @param service
     * @return
     */
    public static Class<?> checkServiceClass(final String clazz, final Object service) {
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
