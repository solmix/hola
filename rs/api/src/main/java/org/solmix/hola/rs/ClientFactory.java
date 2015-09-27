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
import java.util.Enumeration;

import org.solmix.exchange.Client;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.EndpointException;
import org.solmix.exchange.PipelineFactory;
import org.solmix.exchange.PipelineFactoryManager;
import org.solmix.exchange.event.ServiceFactoryEvent;
import org.solmix.exchange.support.DefaultClient;
import org.solmix.exchange.support.ReflectServiceFactory;
import org.solmix.exchange.support.TypeDetectSupport;
import org.solmix.hola.rs.interceptor.RemoteOutInterceptor;



/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月5日
 */

public class ClientFactory extends EndpointFactory {

    private static final long serialVersionUID = -1962206780820114020L;

    public ClientFactory() {
        this(new ReflectServiceFactory(new RemotePhasePolicy()));
    }
    
    public ClientFactory(ReflectServiceFactory factory) {
        super(factory);
        getOutInterceptors().add(new RemoteOutInterceptor());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Client create(){
        getServiceFactory().resetFactory();
        if (getServiceFactory().getProperties()==null){
            getServiceFactory().setProperties(properties);
        }else if(properties!=null){
            Enumeration<String> keys=properties.keys();
            Dictionary dic= getServiceFactory().getProperties();
            while(keys.hasMoreElements()){
                String key = keys.nextElement();
                Object value = properties.get(key);
                dic.put(key, value);
            }
        }
        Client client =null;
        Endpoint endpoint = null;
        try{
            endpoint= createEndpoint();
            getServiceFactory().pulishEvent(ServiceFactoryEvent.PRE_CLIENT_CREATE,endpoint);
            client = createClient(endpoint);
            initializeAnnotationInterceptors(endpoint, getServiceClass());
        }catch(EndpointException e){
            throw new RemoteException(e);
        }
        getServiceFactory().pulishEvent(ServiceFactoryEvent.CLIENT_CREATED,client,endpoint);
        return client;
    }
    
    protected Client createClient(Endpoint endpoint) {
        return new DefaultClient(getContainer(),endpoint,getPipelineSelector());
    }

    @Override
    protected String getTransportTypeForAddress(String address) {
        PipelineFactoryManager plm = getContainer().getExtension(PipelineFactoryManager.class);
        PipelineFactory pf =  plm.getFactoryForUri(address);
        if( pf instanceof TypeDetectSupport){
            return ((TypeDetectSupport)pf).getTransportTypes().get(0);
        }
        return null;
    }
   
}
