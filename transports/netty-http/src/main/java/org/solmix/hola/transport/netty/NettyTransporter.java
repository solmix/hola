/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.transport.netty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.exchange.Message;
import org.solmix.exchange.Processor;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.hola.transport.AbstractTCPTransporter;
import org.solmix.hola.transport.TransporterRegistry;
import org.solmix.hola.transport.model.TransportServerInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */

public class NettyTransporter extends AbstractTCPTransporter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyTransporter.class);

    private boolean configFinalized;

    protected NettyServerEngineFactory serverEngineFactory;

    private NettyServerEngine engine;

    private final TransportServerInfo serverInfo;
    private final String serverPath;

    public NettyTransporter(NettyServerEngineFactory factory,
        EndpointInfo endpointInfo, Container container,
        TransporterRegistry registry) throws IOException {
        super(getAddressValue(endpointInfo, true).getAddress(), endpointInfo,
            container, registry);
        this.serverEngineFactory = factory;
        serverInfo =  endpointInfo.getExtension(TransportServerInfo.class);
        serverPath= serverInfo.getPath();
    }


    @Override
    public void finalizeConfig() {
        assert !configFinalized;

        try {
            retrieveEngine();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        configFinalized = true;
    }

    protected void retrieveEngine() throws IOException {

        engine = serverEngineFactory.retrieveEngine(serverInfo.getHost(),
            serverInfo.getPort());
        if (engine == null) {
            engine = serverEngineFactory.createEngine(serverInfo.getHost(),
                serverInfo.getPort());
        }
        engine.setTransportServerInfo(serverInfo);

        assert engine != null;
    }

    @Override
    protected void activate(Processor p) {
        super.activate(p);
        engine.addHandler(serverPath, new NettyMessageHandler(this));
    }


    @Override
    protected void deactivate(Processor p) {
        super.deactivate(p);
        engine.removeHandler(serverPath);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public int getDefaultPort() {
        return 5715;
    }

    public void doService(Message request, Message response) {
       Container orig = ContainerFactory.getAndSetThreadDefaultContainer(container);
       ClassLoaderHolder origLoader = null;
      
       try {
           ClassLoader loader = container.getExtension(ClassLoader.class);
           if (loader != null) {
               origLoader = ClassLoaderUtils.setThreadContextClassloader(loader);
           }
           invoke(request, response);
       } finally {
           if (orig != container) {
               ContainerFactory.setThreadDefaultContainer(orig);
           }
           if (origLoader != null) {
               origLoader.reset();
           }
       }
    }


}
