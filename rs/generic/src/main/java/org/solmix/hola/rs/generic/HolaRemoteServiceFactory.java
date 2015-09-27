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

package org.solmix.hola.rs.generic;

import java.util.Dictionary;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Server;
import org.solmix.hola.common.Constants;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteProxy;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.event.RemoteRegisteredEvent;
import org.solmix.hola.rs.generic.exchange.HolaProxyFactory;
import org.solmix.hola.rs.generic.exchange.HolaServerFactory;
import org.solmix.hola.rs.support.AbstractRemoteServiceFactory;
import org.solmix.hola.rs.support.RemoteReferenceImpl;
import org.solmix.hola.rs.support.RemoteRegistrationImpl;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月17日
 */
@Extension(name="hola")
@SuppressWarnings({"unchecked","rawtypes"})
public class HolaRemoteServiceFactory extends AbstractRemoteServiceFactory implements ContainerAware
{
    public static final String PROVIDER_ID = "hola";
    public static final int PORT = 5715;
    private static final Logger LOG = LoggerFactory.getLogger(HolaRemoteServiceFactory.class);
    
    @Override
    public <S> RemoteRegistration<S> doRegister(Class<S> clazz, S service, Dictionary properties) throws RemoteException {
       
        if(properties==null){
            properties= new Hashtable<String, Object>();
        }
        if(properties.get(Constants.CODEC_KEY)!=null){
            if(!PROVIDER_ID.equals(properties.get(Constants.CODEC_KEY))){
                LOG.warn("Hola protocol need hola codec");
            }
        }
        properties.put(Constants.CODEC_KEY, PROVIDER_ID);
        //设置默认port
        if(properties.get(Constants.PORT_KEY)==null){
            properties.put(Constants.PORT_KEY, PORT);
        }
        //设置默认protocol
        if(properties.get(Constants.PROTOCOL_KEY)==null){
            properties.put(Constants.PROTOCOL_KEY, PROVIDER_ID);
        }
        HolaServerFactory factory = new HolaServerFactory();
        factory.setContainer(container);
        factory.setProperties(properties);
        factory.setServiceClass(clazz);
        factory.setServiceBean(service);
        Server server =factory.create();
        RemoteRegistrationImpl<S> reg = new RemoteRegistrationImpl<S>(this, registry, clazz, service);
        reg.setServer(server);
        registry.publishServiceEvent(new RemoteRegisteredEvent(reg.getReference()));
        return reg;
    }
    
    @Override
    protected <S> S doGetRemoteService(RemoteReference<S> reference) throws RemoteException {
        RemoteReferenceImpl<S> ref=null;
        if(reference instanceof RemoteReferenceImpl){
            ref=(RemoteReferenceImpl<S>)reference;
        }
        if(ref==null){
            throw new RemoteException("Unsupport RemoteReference type:"+reference.getClass().getName());
        }
        HolaProxyFactory factory= new HolaProxyFactory();
        factory.setContainer(container);
        factory.setServiceClass(reference.getServiceClass());
        Hashtable<String, Object> copyed = new Hashtable<String, Object>();
        for(String key:reference.getPropertyKeys()){
            copyed.put(key, reference.getProperty(key));
        }
        
        if(copyed.get(Constants.CODEC_KEY)!=null){
            if(!PROVIDER_ID.equals(copyed.get(Constants.CODEC_KEY))){
                LOG.warn("Hola protocol need hola codec");
            }
        }
        copyed.put(Constants.CODEC_KEY, PROVIDER_ID);
        //设置默认port
        if(copyed.get(Constants.PORT_KEY)==null){
            copyed.put(Constants.PORT_KEY, PORT);
        }
        //设置默认protocol
        if(copyed.get(Constants.PROTOCOL_KEY)==null){
            copyed.put(Constants.PROTOCOL_KEY, PROVIDER_ID);
        }
        factory.setProperties(copyed);
        Object obj = factory.create();
        ref.setClient(RemoteProxy.getClient(obj));
        return (S) obj;
    }

    @Override
    public RemoteService getRemoteService(RemoteReference<?> reference) {
        return null;
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties) {
        return new RemoteReferenceImpl(clazz,properties,this);
    }
   

}
