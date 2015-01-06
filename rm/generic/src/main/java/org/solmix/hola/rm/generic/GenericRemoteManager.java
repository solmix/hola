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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.config.RemoteServiceConfig;
import org.solmix.hola.rm.RemoteException;
import org.solmix.hola.rm.RemoteListener;
import org.solmix.hola.rm.RemoteManager;
import org.solmix.hola.rm.RemoteReference;
import org.solmix.hola.rm.RemoteRegistration;
import org.solmix.hola.rm.event.RemoteEvent;
import org.solmix.hola.rm.event.RemoteRegisteredEvent;
import org.solmix.hola.rm.event.RemoteUnregisteredEvent;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.bean.BeanConfigurer;
import org.solmix.runtime.exchange.Server;
import org.solmix.runtime.exchange.model.NamedID;
import org.solmix.runtime.exchange.model.ServiceInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月19日
 */

public class GenericRemoteManager implements RemoteManager {

    private static final Logger LOG = LoggerFactory.getLogger(GenericRemoteManager.class);
    
    protected final List<RemoteListener> listeners = new ArrayList<RemoteListener>();

    protected Map<NamedID,RemoteRegistration<?>>  registeredServices =   new ConcurrentHashMap<NamedID, RemoteRegistration<?>>();
    
    private final Container container;
    
    private GenericServerFactory serverFactory;
    
    public GenericRemoteManager(){
        this(ContainerFactory.getThreadDefaultContainer());
    }
    
    public GenericRemoteManager(Container container){
        this.container=container;
    }
    
    @Override
    public RemoteRegistration<?> registerService(String clazze,
        Object service, RemoteServiceConfig hei) throws RemoteException {
        Assert.isNotNull(service, "register service is null");
        if (clazze==null ) {
            throw new IllegalArgumentException( "Service classe is not null.");
        }
        
        final String copy = new String(clazze.getBytes());
        clazze = copy;
        //验证接口和实例是否对应
        final Class<?> clz = checkServiceClass(clazze, service);
        if(LOG.isInfoEnabled()){
            LOG.info("Registering service :"+clz);
        }
        final GenericRemoteRegistration<Object> reg = new GenericRemoteRegistration<Object>(this, clz, service, hei);
        if(serverFactory!=null){
            reg.setServerFactory(serverFactory);
        }
        reg.publish();
        registeredServices.put(reg.getServiceName(), reg);
        //触发注册完毕事项
        fireRemoteListeners(createRegisteredEvent(reg));
        return reg;
    }
    
    @Override
    public <S> RemoteRegistration<S> registerService(Class<S> clazze,
        S service, RemoteServiceConfig hei) throws RemoteException {
        if(LOG.isInfoEnabled()){
            LOG.info("Registering service :"+clazze);
        }
        final GenericRemoteRegistration<S> reg = new GenericRemoteRegistration<S>(this, clazze, service, hei);
        if(serverFactory!=null){
            reg.setServerFactory(serverFactory);
        }
        reg.publish();
        registeredServices.put(reg.getServiceName(), reg);
        //触发注册完毕事项
        fireRemoteListeners(createRegisteredEvent(reg));
        return reg;
    }
    
    protected  void unregisterService(GenericRemoteRegistration<?> reg){
        if(LOG.isTraceEnabled()) {
            LOG.trace("Unregistering service :"+reg.getServiceName());
        }
        fireRemoteListeners(createUnregisteredEvent(reg));
        registeredServices.remove(reg);
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
    
    private RemoteUnregisteredEvent createUnregisteredEvent(
        GenericRemoteRegistration<?> reg) {
        return new RemoteUnregisteredEvent(reg.getReference());
    }
    
    /** 创建注册事项 */
    private RemoteRegisteredEvent createRegisteredEvent(
        GenericRemoteRegistration<?> reg) {
        return new RemoteRegisteredEvent(reg.getReference());
    }
    
    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz,
        RemoteServiceConfig hei) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public <S> Collection<RemoteReference<S>> getReferences(
        Class<S> clazz, String filter) throws RemoteException {
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
        if (reference instanceof LocalRemoteReference) {
            return (S) ((LocalRemoteReference<?>) reference).getService();
        } else if (reference instanceof ProxyRemoteReference) {

        }
        return null;
    }

   
    @Override
    public void addRemoteListener(RemoteListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
      }
    }
    
    @Override
    public void removeRemoteListener(RemoteListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
      }
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
                    throw new IllegalArgumentException("Service=" + clazze+ " is invalid");
                }
                return serviceClazz;
            } catch (final ClassNotFoundException e) {
                // This check is rarely done
                if (extensiveCheckServiceClass(clazze, service.getClass())) {
                    throw new IllegalArgumentException("Service=" + clazze+ " is invalid");
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

    /**   */
    public Container getContainer() {
        return container;
    }

    void publish(GenericRemoteRegistration<?> registration) {
        GenericServerFactory factory = registration.getServerFactory();
        if (factory == null && this.serverFactory != null) {
            factory = this.serverFactory;
        }
        if (factory == null) {
            factory = new GenericServerFactory();
        }
        configureBean(factory);
        registration.setServerFactory(factory);
        Server srv = registration.server;
        try {
            if (srv == null) {
                srv = createServer(factory, registration);
            }
            registration.server = srv;
            if (srv != null) {
                srv.start();
            }
        } catch (Exception e) {
            if (null != srv) {
                srv.destroy();
                srv = null;
            }
            throw new RemoteException(e);
        }
        ServiceInfo si = srv.getEndpoint().getService().getServiceInfo();
        registration.setServiceName(si.getName());
        if (LOG.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Registered service :");
                sb.append(si.getName()).append(",");
            LOG.trace(sb.toString());
        }
    }

    private Server createServer(GenericServerFactory factory,
        GenericRemoteRegistration<?> registration) {
        factory.setContainer(container);
        factory.setStart(false);
        factory.setServiceBean(registration.service);
        factory.setServiceClass(registration.clazze);
        
        setFactoryConfig(factory,registration.getServiceConfig());
        Server server = factory.create();
        configureBean(server);
        configureBean(server.getEndpoint());
        configureBean(server.getEndpoint().getService());
        return server;
    }

    
    protected void setFactoryConfig(GenericServerFactory factory,
        RemoteServiceConfig sc) {
        if(sc==null){
            return;
        }
        factory.setConfigObject(sc);
        //设置发布地址
        factory.setAddress(sc.getAddress());
        
        //设置传输层
        if(sc.getServer()!=null){
          String transporter=  sc.getServer().getTransporter();
          if(StringUtils.isNotEmpty(transporter)){
            factory.setTransporter(transporter);
          }
        }
        //设置传输协议
        String protocol = null;
        if(sc.getService()!=null){
            protocol = sc.getService().getProtocol();
            //service中没有指定,用server的协议.
            if(StringUtils.isEmpty(protocol)){
                protocol=sc.getServer().getProtocol();
            }
        }
        if(StringUtils.isNotEmpty(protocol)){
            factory.setProtocol(protocol);
        }
        //
        
        
        
    }
}
