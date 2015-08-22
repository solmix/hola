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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.hola.common.Params;
import org.solmix.hola.common.ParamsUtils;
import org.solmix.hola.rpc.RemoteReference;
import org.solmix.hola.rpc.RpcException;
import org.solmix.hola.rpc.event.RemoteRegisteredEvent;
import org.solmix.hola.rpc.support.RemoteReferenceImpl;
import org.solmix.hola.rpc.support.RemoteRegistrationImpl;
import org.solmix.hola.rpc.support.ServiceProperties;
import org.solmix.hola.rpc.support.ServiceRegistry;
import org.solmix.exchange.Server;
import org.solmix.exchange.model.NamedID;

/**
 * 端口号必选在注册前指定,如果没有显式配置,则随机取一个放入.
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月20日
 */

public class HolaRemoteRegistration<S> extends RemoteRegistrationImpl<S>
    implements java.io.Serializable {

    private static final long serialVersionUID = 1656647471796485503L;
    private static final Logger LOG = LoggerFactory.getLogger(HolaRemoteRegistration.class);

    Server server;

    private HolaServerFactory serverFactory;

    private NamedID serviceName;

    public HolaRemoteRegistration(HolaRpcManager manager,
        ServiceRegistry registry, Class<?> clazze, S service) {
        super(manager, registry, clazze, service);
    }

    @Override
    public void unregister() {
        super.unregister();

    }

    HolaServerFactory getServerFactory() {
        return serverFactory;
    }

    public void setServerFactory(HolaServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    @Override
    public void register(Dictionary<String, ?> props) {
        final RemoteReferenceImpl<S> ref;
        synchronized (registry) {
            synchronized (registrationLock) {
                ref = reference;
                this.properties = createProperties(props);
            }
            setupServer(properties);
            serviceKey = createServiceKey(properties);
            properties.set(Params.SERVICE_ID_KEY, serviceKey);
            properties.set(RemoteReference.ReferenceType.class.getName(), RemoteReference.ReferenceType.LOCAL,true);
            properties.setReadOnly();
            registry.addServiceRegistration(serviceKey, this);
        }
        if (LOG.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Registered service :").append(getServiceKey());
            LOG.trace(sb.toString());
        }
        registry.publishServiceEvent(new RemoteRegisteredEvent(ref));
    }
    
    @Override
    protected ServiceProperties createProperties(Dictionary<String, ?> props) {
        assert Thread.holdsLock(registrationLock);
        ServiceProperties sp = new ServiceProperties(props);
        return sp;
    }

    private void setupServer(ServiceProperties properties) {
        try {
            if (server == null) {
                if (serverFactory == null) {
                    serverFactory = new HolaServerFactory();
                }
                HolaRpcManager hm = (HolaRpcManager) getManager();
                hm.configureBean(serverFactory);
                server = createServer();
            }
            if (server != null) {
                server.start();
            }
        } catch (Exception e) {
            if (server != null) {
                server.destroy();
            }
            throw new RpcException(e);
        }
    }

    @Override
    protected String createServiceKey(ServiceProperties props) {
      NamedID serviceID =  server.getEndpoint().getService().getServiceInfo().getName();
      DataTypeMap dt = new DataTypeMap(props);
      return HolaRpcManager.serviceKey(dt.getString(Params.GROUP_KEY),
          serviceID.toString(),
          dt.getString(Params.VERSION_KEY),
          dt.getInteger(Params.PORT_KEY));
    }
    
    public HolaRpcManager getRpcManager(){
        return (HolaRpcManager)getManager();
    }
    private Server createServer() {
        serverFactory.setContainer(getRpcManager().getContainer());
        serverFactory.setStart(false);
        serverFactory.setServiceBean(service);
        serverFactory.setServiceClass(clazze);
        //放入配置参数
        serverFactory.setConfigObject(properties);
        setFactoryProperties(serverFactory,new DataTypeMap(properties));
        
        Server server = serverFactory.create();
        configureBean(server);
        configureBean(server.getEndpoint());
        configureBean(server.getEndpoint().getService());
        return server;
    }
    
    private void setFactoryProperties(HolaServerFactory factory, DataTypeMap dic) {
        factory.setAddress(ParamsUtils.getAddress(dic));
        factory.setTransporter(dic.getString(Params.TRANSPORTER_KEY,
            Params.DEFAULT_RPC_TRANSPORTER));
        factory.setProtocol(dic.getString(Params.PROTOCOL_KEY,
            Params.DEFAULT_PROTOCOL));
    }

    private void configureBean(Object instance){
        getRpcManager().configureBean(instance);
    }

    /**   */
    public NamedID getServiceName() {
        return serviceName;
    }

    /**   */
    public void setServiceName(NamedID serviceName) {
        this.serviceName = serviceName;
    }

}
