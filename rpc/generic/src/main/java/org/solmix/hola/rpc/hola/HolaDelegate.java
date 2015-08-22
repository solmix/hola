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

import java.util.Dictionary;
import java.util.concurrent.Executor;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.Assert;
import org.solmix.hola.common.Params;
import org.solmix.hola.common.ParamsUtils;
import org.solmix.hola.rpc.ClientProxy;
import org.solmix.hola.rpc.support.ServiceProperties;
import org.solmix.hola.transport.support.RpcPipelineSelector;
import org.solmix.runtime.Container;
import org.solmix.runtime.bean.BeanConfigurer;
import org.solmix.exchange.Client;
import org.solmix.exchange.PipelineSelector;
import org.solmix.exchange.Service;
import org.solmix.exchange.model.NamedID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月7日
 */

public class HolaDelegate implements RemoteDelegate {

    private final Container container;
    private final NamedID serviceName;
    private Executor executor;
    /**
     * @param reference 
     */
    public HolaDelegate(Container container,NamedID serviceName) {
        this.container=container;
        this.serviceName=serviceName;
    }

    @Override
    public <T> T getProxy(NamedID name,Class<T> type,Dictionary<String,?> config) {
       Assert.isNotNull(name);
       ServiceProperties dic = new ServiceProperties(config);
       DataTypeMap typeMap = new DataTypeMap(dic);
       PipelineSelector selector = getSelector(typeMap);
       HolaClientFactory clientFactory = new HolaClientFactory();
       clientFactory.setPipelineSelector(selector);
       clientFactory.setConfigObject(dic);
       HolaClientProxyFactory proxyFactory = new HolaClientProxyFactory(clientFactory);
       HolaServiceFactory serviceFactory= (HolaServiceFactory)  proxyFactory.getServiceFactory();
       
       proxyFactory.setContainer(container);
       proxyFactory.setServiceClass(type);
       proxyFactory.setServiceName(serviceName);
       configureObject(proxyFactory);
       configureObject(clientFactory);
       if(name==null){
           
       }
       serviceFactory.setEndpointName(name);
        if (config != null) {
            clientFactory.setAddress(ParamsUtils.getAddress(config));
        }
        Object proxy = proxyFactory.create();
        Service service = serviceFactory.getService();
        configureObject(service);
        
        Client c = ClientProxy.getClient(proxy);
        if (executor != null) {
            c.getEndpoint().setExecutor(executor);
            c.setExecutor(executor);
        }
        c.getEndpoint();
        
        return type.cast(proxy);
    }
    
    protected PipelineSelector getSelector(DataTypeMap dic) {
        boolean sharedConnect=false;
       int pipelines= dic.getInt(Params.PIPELINES,0);
       if(pipelines==0){
           sharedConnect=true;
           pipelines=1;
       }
        return new RpcPipelineSelector(sharedConnect,pipelines);
    }

    private void configureObject(Object instance) {
        configureObject(null, instance);
    }
    
    private void configureObject(String name, Object instance) {
        BeanConfigurer configurer = container.getExtension(BeanConfigurer.class);
        if (null != configurer) {
            configurer.configureBean(name, instance);
        }
    }

    @Override
    public <T> RemoteService<T> createService(NamedID id, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    public Container getContainer() {
        return container;
    }

    /**   */
    public Executor getExecutor() {
        return executor;
    }
    /**   */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }


}
