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
import java.util.Dictionary;
import java.util.Enumeration;

import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.exchange.Client;
import org.solmix.exchange.PipelineSelector;
import org.solmix.exchange.ProtocolFactory;
import org.solmix.exchange.TransporterFactory;
import org.solmix.exchange.event.ServiceFactoryEvent;
import org.solmix.exchange.interceptor.support.InterceptorProviderSupport;
import org.solmix.runtime.Container;
import org.solmix.runtime.helper.ProxyHelper;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月20日
 */

public class RemoteProxyFactory extends InterceptorProviderSupport
{

    private static final long serialVersionUID = 4654518079882850572L;

    private ClientFactory clientFactory;
    protected Dictionary<String, ?> properties;
    private Container container;
    public RemoteProxyFactory()
    {
        this(new ClientFactory());
    }

    public RemoteProxyFactory(ClientFactory clientFactory)
    {
        this.clientFactory=clientFactory;
    }
    /**创建远程调用的本地代理*/
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized Object create() {
        ClassLoaderHolder loader = null;
        try {
            if (container != null) {
                ClassLoader cl = container.getExtension(ClassLoader.class);
                if (cl != null) {
                    loader = ClassLoaderUtils.setThreadContextClassloader(cl);
                }
            }
            if(clientFactory.getProperties()==null){
                clientFactory.setProperties(getProperties());
            }else if(getProperties()!=null){
                Enumeration<String> keys=properties.keys();
                Dictionary dic= clientFactory.getProperties();
                while(keys.hasMoreElements()){
                    String key = keys.nextElement();
                    Object value = properties.get(key);
                    dic.put(key, value);
                }
            }
            if(container!=null){
                clientFactory.setContainer(container);
            }
            
            Client  client = clientFactory.create();
            if (getInInterceptors() != null) {
                client.getInInterceptors().addAll(getInInterceptors());
            }
            if (getOutInterceptors() != null) {
                client.getOutInterceptors().addAll(getOutInterceptors());
            }
            if (getInFaultInterceptors() != null) {
                client.getInFaultInterceptors().addAll(getInFaultInterceptors());
            }
            if (getOutFaultInterceptors() != null) {
                client.getOutFaultInterceptors().addAll(getOutFaultInterceptors());
            }
            
            RemoteProxy proxyHandler = createRemoteProxy(client);
            Class<?> classes[] = getImplementingClasses();
            Object object = ProxyHelper.getProxy(clientFactory.getServiceClass().getClassLoader(), classes, proxyHandler);
            clientFactory.getServiceFactory().pulishEvent(ServiceFactoryEvent.PROXY_CREATED, classes,proxyHandler,object);
            return object;
        } finally {
            if (loader != null) {
                loader.reset();
            }
        }
        
    }

    protected Class<?>[] getImplementingClasses() {
        Class<?> cls = clientFactory.getServiceClass();
        return new Class[] {cls, Closeable.class, Client.class};
    }
    
    protected RemoteProxy createRemoteProxy(Client client) {
        return new RemoteProxy(client);
    }

    public Container getContainer() {
        return container;
    }

    
    public void setContainer(Container container) {
        this.container = container;
    }

    
    public Dictionary<String, ?> getProperties() {
        return properties;
    }

    
    public void setProperties(Dictionary<String, ?> properties) {
        this.properties = properties;
    }
    public void setServiceClass(Class<?> serviceClass) {
        clientFactory.setServiceClass(serviceClass);
    }
    
    public Class<?> getServiceClass() {
        return clientFactory.getServiceClass();
    }

    
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    
    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    /**   */
    public ProtocolFactory getProtocolFactory() {
        return clientFactory.getProtocolFactory();
    }

    /**   */
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        clientFactory.setProtocolFactory(protocolFactory);
    }
    
    /**   */
    public TransporterFactory getTransporterFactory() {
       return clientFactory.getTransporterFactory();
    }
    
    /**   */
    public void setTransporterFactory(TransporterFactory transporterFactory) {
        clientFactory.setTransporterFactory(transporterFactory);
    }

    /**   */
    public PipelineSelector getPipelineSelector() {
        return clientFactory.getPipelineSelector();
    }

    /**   */
    public void setPipelineSelector(PipelineSelector pipelineSelector) {
        clientFactory.setPipelineSelector(pipelineSelector);
    }
 
    
}
