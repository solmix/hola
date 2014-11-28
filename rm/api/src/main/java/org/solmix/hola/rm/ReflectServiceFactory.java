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

import java.lang.reflect.Proxy;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.model.RemoteServiceInfo;
import org.solmix.runtime.exchange.Service;
import org.solmix.runtime.exchange.event.ServiceFactoryEvent;
import org.solmix.runtime.exchange.invoker.Invoker;
import org.solmix.runtime.exchange.model.ServiceInfo;
import org.solmix.runtime.exchange.serialize.Serialization;
import org.solmix.runtime.exchange.support.AbstractServiceFactory;
import org.solmix.runtime.exchange.support.DefaultService;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月24日
 */

public class ReflectServiceFactory extends AbstractServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectServiceFactory.class);
    protected Class<?> serviceClass;

    private RemoteServiceInfo<?> serviceInfo;
    
    private Map<String, Object> properties;

    private Invoker invoker;
    
    @Override
    public Service create() {
        // 重置service,重新创建.
        resetFactory();
        pulishEvent(new ServiceFactoryEvent(ServiceFactoryEvent.START_CREATE,this));

        // 初始化
        initialService();

        return null;
    }

    protected void initialService() {
        // 从配置来
        if (getServiceInfo() != null) {
            createServiceFromRemoteInfo();
            // 从class来
        } else if (getServiceClass() != null) {
            createServiceFromClass();
        } else {
            throw new IllegalStateException("Not Service class configured!");
        }

    }

    private void createServiceFromRemoteInfo() {
        // TODO Auto-generated method stub
        
    }

    /**
     * 
     */
    private void createServiceFromClass() {
        if(LOG.isInfoEnabled()){
            LOG.info("create Service from class :"+getServiceClass().getName());
        }
        if (Proxy.isProxyClass(this.getServiceClass())) {
            LOG.warn("USING_PROXY_FOR_SERVICE", getServiceClass());
        }
        pulishEvent(ServiceFactoryEvent.CREATE_FROM_CLASS,getServiceClass());
        
        ServiceInfo info = new ServiceInfo();
        
        Service service = new DefaultService(info);
        
    }

    @Override
    protected Serialization defaultSerialization() {
        // 通过service class 注解
        // 通过container参数加载
        return null;
    }

    public void resetFactory() {
        if (!serializeSetted) {
            setSerialization(null);
        }
        setService(null);
    }

    /**   */
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    /**   */
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    /** RemoteServiceInfo用于初始化ServiceFactory不对外提供信息  */
    private RemoteServiceInfo<?> getServiceInfo() {
        return serviceInfo;
    }

    /**   */
    public void setServiceInfo(RemoteServiceInfo<?> serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    
    /**   */
    public Map<String, Object> getProperties() {
        return properties;
    }

    
    /**   */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    
    /**   */
    public Invoker getInvoker() {
        return invoker;
    }

    
    /**   */
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

}
