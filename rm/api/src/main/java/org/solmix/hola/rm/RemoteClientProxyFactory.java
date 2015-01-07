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

package org.solmix.hola.rm;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.hola.rm.proxy.ProxyHelper;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.bean.BeanConfigurer;
import org.solmix.runtime.exchange.Client;
import org.solmix.runtime.exchange.event.ServiceFactoryEvent;
import org.solmix.runtime.exchange.model.NamedID;
import org.solmix.runtime.exchange.serialize.Serialization;
import org.solmix.runtime.exchange.support.ReflectServiceFactory;
import org.solmix.runtime.interceptor.support.InterceptorProviderSupport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月5日
 */

public class RemoteClientProxyFactory extends InterceptorProviderSupport {

    private static final long serialVersionUID = -2122748438240492773L;
    protected boolean configured;
    private final RemoteClientFactory remoteClientFactory;
    private Map<String, Object> properties;
    private Container container;
    private Serialization serialization;

    public RemoteClientProxyFactory() {
        this(new RemoteClientFactory());
    }

    public RemoteClientProxyFactory(RemoteClientFactory factory) {
        this.remoteClientFactory = factory;
    }

    public RemoteClientFactory getClientFactory() {
        return remoteClientFactory;
    }
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
            configureObject();
            if (properties == null) {
                properties = new HashMap<String, Object>();
            }
            
            remoteClientFactory.setProperties(properties);
            if(container!=null){
                remoteClientFactory.setContainer(container);
            }
           
            if(serialization!=null){
                remoteClientFactory.setSerialization(serialization);
            }
            Client c = remoteClientFactory.create();
            if(getInInterceptors()!=null){
                c.getInInterceptors().addAll(getInInterceptors());
            }
            if(getOutInterceptors()!=null){
                c.getOutInterceptors().addAll(getOutInterceptors());
            }
            if(getInFaultInterceptors()!=null){
                c.getInFaultInterceptors().addAll(getInFaultInterceptors());
            }
            if(getOutFaultInterceptors()!=null){
                c.getOutFaultInterceptors().addAll(getOutFaultInterceptors());
            }
            
            ClientProxy proxy = createClientProxy(c);
            
            Class<?> classes[] = getImplementingClasses();
            Object obj = ProxyHelper.getProxy(
                remoteClientFactory.getServiceClass().getClassLoader(),
                classes,
                proxy);
            getServiceFactory().pulishEvent(ServiceFactoryEvent.PROXY_CREATED,classes,proxy,obj);
            return obj;
        } finally {
            if (orig != null) {
                orig.reset();
            }
        }
    }
    
    protected Class<?>[] getImplementingClasses() {
        Class<?> cls = remoteClientFactory.getServiceClass();
        return new Class[] {cls, Closeable.class, Client.class};
    }
    
    protected ClientProxy createClientProxy(Client c) {
        return new ClientProxy(c);
    }
    private void configureObject() {
        if (configured) {
            return;
        }
        if (container == null) {
            container = ContainerFactory.getThreadDefaultContainer();
        }
        BeanConfigurer configurer = container.getExtension(BeanConfigurer.class);
        String name = getConfigureName();
        if (null != configurer && name != null) {
            configurer.configureBean(name, this);
        }
        configured = true;
    }
   
    public String getConfigureName() {
        NamedID ename = remoteClientFactory.getEndpointName();
        if (ename == null) {
            ename = remoteClientFactory.getServiceFactory().getEndpointName(
                false);
        }
        return ename.toString() + ".client.proxyFactory";
    }

    public ReflectServiceFactory getServiceFactory() {
        return remoteClientFactory.getServiceFactory();
    }

    public void setServiceFactory(ReflectServiceFactory f) {
        remoteClientFactory.setServiceFactory(f);
    }
    public Class<?> getServiceClass() {
        return remoteClientFactory.getServiceClass();
    }

    public void setServiceClass(Class<?> serviceClass) {
        remoteClientFactory.setServiceClass(serviceClass);
    }
    public NamedID getServiceName() {
        return getServiceFactory().getServiceName();
    }

    public void setServiceName(NamedID serviceName) {
        getServiceFactory().setServiceName(serviceName);
    }
    /**   */
    public Container getContainer() {
        return container;
    }

    /**   */
    public void setContainer(Container container) {
        this.container = container;
        remoteClientFactory.setContainer(container);
    }

}
