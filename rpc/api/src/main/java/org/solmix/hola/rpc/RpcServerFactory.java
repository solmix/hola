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
package org.solmix.hola.rpc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.hola.common.config.Param;
import org.solmix.runtime.exchange.Endpoint;
import org.solmix.runtime.exchange.EndpointException;
import org.solmix.runtime.exchange.EndpointInfoFactory;
import org.solmix.runtime.exchange.Server;
import org.solmix.runtime.exchange.event.ServiceFactoryEvent;
import org.solmix.runtime.exchange.invoker.BeanInvoker;
import org.solmix.runtime.exchange.invoker.FactoryInvoker;
import org.solmix.runtime.exchange.invoker.Invoker;
import org.solmix.runtime.exchange.invoker.SingletonFactory;
import org.solmix.runtime.exchange.support.ClassHelper;
import org.solmix.runtime.exchange.support.DefaultServer;
import org.solmix.runtime.exchange.support.ReflectServiceFactory;



/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月21日
 */

public class RpcServerFactory extends RpcEndpointFactory {

    private static final long serialVersionUID = 7245670739694833151L;
    private static final Logger LOG = LoggerFactory.getLogger(RpcServerFactory.class);
    private boolean start = true;
    private Server server;
    private Object serviceBean;
    
    private Invoker invoker;
    /**
     * @param factory 
     */
    public RpcServerFactory(ReflectServiceFactory factory) {
        super(factory);
    }

    /**
     * @return
     */
    public Server create() {
        ClassLoaderHolder loader = null;
        try {
            try {
                if (container != null) {
                    ClassLoader cl = container.getExtension(ClassLoader.class);
                    if (cl != null) {
                        loader = ClassLoaderUtils.setThreadContextClassloader(cl);
                    }
                }
                if (getServiceFactory().getProperties() == null) {
                    getServiceFactory().setProperties(getProperties());
                } else if (getProperties() != null) {
                    getServiceFactory().getProperties().putAll(getProperties());
                }
                if (serviceBean != null && getServiceClass() == null) {
                    setServiceClass(ClassHelper.getRealClass(serviceBean));
                }
                if (invoker != null) {
                    getServiceFactory().setInvoker(invoker);
                } else if (serviceBean != null) {
                    invoker = createInvoker();
                    getServiceFactory().setInvoker(invoker);
                }
                Endpoint ep = createEndpoint();
                getServiceFactory().pulishEvent(new ServiceFactoryEvent(
                        ServiceFactoryEvent.PRE_SERVER_CREATE,getServiceFactory(), 
                        server,
                        serviceBean==null?
                            (getServiceClass()==null?getServiceFactory().getServiceClass():getServiceClass())
                            :(getServiceClass()==null?ClassHelper.getRealClass(getServiceBean()):getServiceClass())));

                server=new DefaultServer(getContainer(), 
                                            ep, 
                                            getProtocolFactory(), 
                                            getTransporterFactory());
                if(ep.getService().getInvoker()==null){
                    if (invoker == null) {
                        ep.getService().setInvoker(createInvoker());
                    } else {
                        ep.getService().setInvoker(invoker);
                    }
                }
            } catch (EndpointException epe) {
                throw new RpcException(epe);
            } catch (IOException e) {
                throw new RpcException(e);
            }
            if (serviceBean != null) {
                Class<?> cls = ClassHelper.getRealClass(getServiceBean());
                if (getServiceClass() == null || cls.equals(getServiceClass())) {
                    initializeAnnotationInterceptors(server.getEndpoint(), cls);
                } else {
                    initializeAnnotationInterceptors(server.getEndpoint(), cls,
                        getServiceClass());
                }
            } else if (getServiceClass() != null) {
                initializeAnnotationInterceptors(server.getEndpoint(),
                    getServiceClass());
            }
            getServiceFactory().pulishEvent(new ServiceFactoryEvent(
                ServiceFactoryEvent.SERVER_CREATED,getServiceFactory(), 
                server,
                serviceBean==null?
                    (getServiceClass()==null?getServiceFactory().getServiceClass():getServiceClass())
                    :(getServiceClass()==null?ClassHelper.getRealClass(getServiceBean()):getServiceClass())));

            if (start) {
                try {
                    server.start();
                } catch (RuntimeException re) {
                    server.destroy(); // prevent resource leak
                    throw re;
                }
            }
            return server;
        } finally {
            if (loader != null) {
                loader.reset();
            }
        }
    }
    
    public void destroy() {
        if (getServer() != null) {
            getServer().destroy();
            setServer(null);
        }
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
    public boolean isStart() {
        return start;
    }
    public void setStart(boolean start) {
        this.start = start;
    }

    /**
     * @return
     */
    protected Invoker createInvoker() {
        if (getServiceBean() == null) {
            return new FactoryInvoker(new SingletonFactory(getServiceClass()));
        }
        return new BeanInvoker(getServiceBean());
    }

    /**   */
    public Object getServiceBean() {
        return serviceBean;
    }
    
    /**   */
    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }
    
    /**   */
    public Invoker getInvoker() {
        return invoker;
    }
    
    /**   */
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }


    @Override
    protected String getTransportTypeForAddress(String address) {
        return Param.DEFAULT_RPC_TRANSPORTER;
    }

    @Override
    protected EndpointInfoFactory defaultEndpointInfoFactory() {
        return new RpcEndpointInfoFactory(true);
    }
}
