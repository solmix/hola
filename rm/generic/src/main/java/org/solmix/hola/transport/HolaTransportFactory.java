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
package org.solmix.hola.transport;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.solmix.commons.util.StringUtils;
import org.solmix.hola.protocol.HolaProtocolFactory;
import org.solmix.hola.rm.RemoteEndpointInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.runtime.exchange.EndpointInfoFactory;
import org.solmix.runtime.exchange.Pipeline;
import org.solmix.runtime.exchange.PipelineFactory;
import org.solmix.runtime.exchange.Transporter;
import org.solmix.runtime.exchange.TransporterFactory;
import org.solmix.runtime.exchange.TransporterFactoryManager;
import org.solmix.runtime.exchange.model.EndpointInfo;
import org.solmix.runtime.exchange.model.ProtocolInfo;
import org.solmix.runtime.exchange.model.ServiceInfo;


/**
 * 传输工厂类.
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月10日
 */
@Extension(name = HolaProtocolFactory.NAME)
public class HolaTransportFactory implements PipelineFactory,
    EndpointInfoFactory, TransporterFactory {

    public static  Set<String> transports;
    @Override
    public Transporter getTransporter(EndpointInfo ei, Container container) throws IOException {
        TransporterFactory factory;
        String address = ei.getAddress();
        String transport = ei.getTransporter();
        
        try {
            TransporterFactoryManager tfm = container.getExtension(TransporterFactoryManager.class);
            if(StringUtils.isEmpty(address)){
                factory=tfm.getFactory(transport);
            }else{
                factory=tfm.getFactoryForUri(address);
            }
            return factory.getTransporter(ei, container);
        } catch (Exception e) {
            IOException ex = new IOException("Could not find transporter factory for transporter " + transport);
            ex.initCause(e);
            throw ex;
        }
    }

    protected String getProtocolFromAddress(String address) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.EndpointInfoFactory#createEndpointInfo(org.solmix.runtime.Container, org.solmix.runtime.exchange.model.ServiceInfo, org.solmix.runtime.exchange.model.ProtocolInfo, java.util.List)
     */
    @Override
    public EndpointInfo createEndpointInfo(Container container,
        ServiceInfo serviceInfo, ProtocolInfo b, List<?> extensions) {
        EndpointInfo info = new RemoteEndpointInfo(serviceInfo, "hola");
        if(extensions!=null){
            for(Iterator<?> itr = extensions.iterator(); itr.hasNext();){
                Object extension = itr.next();
                info.addExtension(extension);
            }
        }
        return info;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.PipelineFactory#getPipeline(org.solmix.runtime.exchange.model.EndpointInfo, org.solmix.runtime.Container)
     */
    @Override
    public Pipeline getPipeline(EndpointInfo info, Container c) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.PipelineFactory#getpPipeline(org.solmix.runtime.exchange.model.EndpointInfo, java.lang.String, org.solmix.runtime.Container)
     */
    @Override
    public Pipeline getpPipeline(EndpointInfo info, String address, Container c) {
        // TODO Auto-generated method stub
        return null;
    }

}
