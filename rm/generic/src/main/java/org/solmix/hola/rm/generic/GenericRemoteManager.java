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
package org.solmix.hola.rm.generic;

import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.ServiceReference;
import org.solmix.commons.util.Assert;
import org.solmix.hola.core.model.RemoteEndpointInfo;
import org.solmix.hola.rm.RemoteListener;
import org.solmix.hola.rm.RemoteManager;
import org.solmix.hola.rm.RemoteRegistration;
import org.solmix.hola.rm.event.RemoteEvent;
import org.solmix.hola.rm.event.RemoteRegisteredEvent;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.bean.BeanConfigurer;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月19日
 */

public class GenericRemoteManager implements RemoteManager {

    protected final List<RemoteListener> listeners = new ArrayList<RemoteListener>();

    protected List<RemoteRegistration<?>>  registeredServices =   new CopyOnWriteArrayList< RemoteRegistration<?>>();
    
    private final Container container;
    
    private GenericServerFactory serverFactory;
    
    public GenericRemoteManager(){
        this(ContainerFactory.getThreadDefaultContainer());
    }
    
    public GenericRemoteManager(Container container){
        this.container=container;
    }
    
    @Override
    public RemoteRegistration<?> registerService(String[] clazzes,
        Object service, RemoteEndpointInfo hei) throws RemoteException {
        Assert.isNotNull(service, "register service is null");
        if (clazzes==null || clazzes.length == 0) {
            throw new IllegalArgumentException( "Service classes list is empty");
        }
        final String[] copy = new String[clazzes.length];
        for (int i = 0; i < clazzes.length; i++) {
            copy[i] = new String(clazzes[i].getBytes());
        }
        clazzes = copy;
        //验证接口和实例是否对应
        final String invalidService = checkServiceClass(clazzes, service);
        if (invalidService != null) {
            throw new IllegalArgumentException("Service=" + invalidService+ " is invalid");
        }
        final GenericRemoteRegistration<?> reg= new GenericRemoteRegistration<Object>(this, clazzes, service, hei);
        if(serverFactory!=null){
            reg.setServerFactory(serverFactory);
        }
        reg.publish();
        registeredServices.add(reg);
        //触发注册完毕事项
        fireRemoteListeners(createRegisteredEvent(reg));
        return reg;
    }
    
    protected void fireRemoteListeners(RemoteEvent registeredEvent) {
        List<RemoteListener> entries;
        synchronized (listeners) {
            entries = new ArrayList<RemoteListener>(listeners);
        }
        for (RemoteListener listener : entries) {
            listener.onHandle(registeredEvent);
        }
    }
    
    /** 创建注册事项 */
    private RemoteRegisteredEvent createRegisteredEvent(
        GenericRemoteRegistration<?> reg) {
        return new RemoteRegisteredEvent(reg.getReference());
    }
    
    @Override
    public RemoteRegistration<?> registerService(String clazze, Object service,
        RemoteEndpointInfo hei) throws RemoteException {
        return registerService(new String[]{clazze}, service, hei);
    }

    
    @Override
    public <S> RemoteRegistration<S> registerService(Class<S> clazze,
        S service, RemoteEndpointInfo hei) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz,
        RemoteEndpointInfo hei) {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(
        Class<S> clazz, String filter) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public ServiceReference<?> getServiceReference(String clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public <S> S getService(ServiceReference<S> reference) {
        // TODO Auto-generated method stub
        return null;
    }

   
    @Override
    public void addRemoteListener(RemoteListener listener) {
        // TODO Auto-generated method stub

    }

    
    @Override
    public void removeRemoteListener(RemoteListener listener) {
        // TODO Auto-generated method stub

    }
    
    public GenericServerFactory getServerFactory() {
        return serverFactory;
    }
    
    public void setServerFactory(GenericServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    /**
     * 检查服务是否为接口的实例
     * @param clazzes
     * @param service
     * @return
     */
    static String checkServiceClass(final String[] clazzes, final Object service) {
        final ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return service.getClass().getClassLoader();
            }
        });
        for (int i = 0; i < clazzes.length; i++) {
            try {
                final Class<?> serviceClazz = cl == null ? Class.forName(clazzes[i])
                    : cl.loadClass(clazzes[i]);
                if (!serviceClazz.isInstance(service)) {
                    return clazzes[i];
                }
            } catch (final ClassNotFoundException e) {
                // This check is rarely done
                if (extensiveCheckServiceClass(clazzes[i], service.getClass())) {
                    return clazzes[i];
                }
            }
        }
        return null;
    }
    /**
     * 检查父类和接口
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
        BeanConfigurer bc= container.getExtension(BeanConfigurer.class);
        if(bc!=null){
            bc.configureBean(bean);
        }
        
    }
}
