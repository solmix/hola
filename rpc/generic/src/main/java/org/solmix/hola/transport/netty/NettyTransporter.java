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

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.hola.transport.AbstractTCPTransporter;
import org.solmix.hola.transport.TransportServerInfo;
import org.solmix.hola.transport.TransporterRegistry;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.exchange.Processor;
import org.solmix.runtime.exchange.model.EndpointInfo;

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

    private final URI serverUrl;

    public NettyTransporter(NettyServerEngineFactory factory,
        EndpointInfo endpointInfo, Container container,
        TransporterRegistry registry) throws IOException {
        super(getAddressValue(endpointInfo, true).getAddress(), endpointInfo,
            container, registry);
        this.serverEngineFactory = factory;
        serverUrl = getURI(endpointInfo);
    }

    private URI getURI(EndpointInfo endpointInfo) throws IOException  {
        try {
            return new URI(endpointInfo.getAddress());
        } catch (URISyntaxException e) {
            LOG.error("Not a valid endpoint address",e);
            throw new IOException(e.getMessage());
        }
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

        engine = serverEngineFactory.retrieveEngine(serverUrl.getHost(),
            serverUrl.getPort());
        if (engine == null) {
            engine = serverEngineFactory.createEngine(serverUrl.getHost(),
                serverUrl.getPort());
        }
        TransportServerInfo tsi= endpointInfo.getExtension(TransportServerInfo.class);
        engine.setTransportServerInfo(tsi);

        assert engine != null;
    }

    @Override
    protected void activate(Processor p) {
        super.activate(p);
        engine.setNettyBuffedHandler(new NettyBuffedHandler(this));
        engine.start();
    }


    @Override
    protected void deactivate(Processor p) {
        super.deactivate(p);
        engine.shutdown();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.Transporter#getDefaultPort()
     */
    @Override
    public int getDefaultPort() {
        return 1314;
    }

    public void doService(ByteBuf request, ByteBuf response) {
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
