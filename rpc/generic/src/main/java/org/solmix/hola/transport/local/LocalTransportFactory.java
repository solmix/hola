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

package org.solmix.hola.transport.local;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.rpc.RpcEndpointInfo;
import org.solmix.hola.transport.RemoteAddress;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.runtime.exchange.EndpointInfoFactory;
import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.Pipeline;
import org.solmix.runtime.exchange.PipelineFactory;
import org.solmix.runtime.exchange.Transporter;
import org.solmix.runtime.exchange.TransporterFactory;
import org.solmix.runtime.exchange.model.EndpointInfo;
import org.solmix.runtime.exchange.model.ProtocolInfo;
import org.solmix.runtime.exchange.model.ServiceInfo;
import org.solmix.runtime.exchange.support.TransportDetectSupport;
import org.solmix.runtime.threadpool.ThreadPoolManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年12月18日
 */
@Extension(name = LocalTransportFactory.TRANSPORT_TYPE)
public class LocalTransportFactory implements TransporterFactory,
    EndpointInfoFactory, PipelineFactory, TransportDetectSupport {

    private static final Logger LOG = LoggerFactory.getLogger(LocalTransportFactory.class);

    private final ConcurrentMap<String, Transporter> transporters = new ConcurrentHashMap<String, Transporter>();

    public static final String TRANSPORT_TYPE = "local";

    private static final String NULL_ADDRESS = LocalTransportFactory.class.getName()
        + ".nulladdress";

    private static final Set<String> URI_PREFIXES = new HashSet<String>();

    public static final List<String> DEFAULT_TYPE = Arrays.asList(TRANSPORT_TYPE);

    private volatile Executor executor;

    static {
        URI_PREFIXES.add("local://");
    }

    @Override
    public Pipeline getPipeline(EndpointInfo info, Container c)
        throws IOException {
        return new LocalPipeline(this, (LocalTransporter) getTransporter(info,
            c));
    }

    @Override
    public Pipeline getPipeline(EndpointInfo info, String address, Container c)
        throws IOException {
        return new LocalPipeline(this, (LocalTransporter) getTransporter(info,
            c));
    }

    @Override
    public Transporter getTransporter(EndpointInfo ei, Container container)
        throws IOException {
        String address = ei.getAddress();
        if (address == null) {
            RemoteAddress ra = ei.getExtension(RemoteAddress.class);
            if (ra != null) {
                address = ra.getAddress();
            }
        }
        if (address == null) {
            address = NULL_ADDRESS;
        }
        Transporter t = transporters.get(address);
        if (t == null) {
            t = createTransporter(ei, address, container);
            Transporter target = transporters.putIfAbsent(address, t);
            if (target != null) {
                t = target;
            }
        }
        return t;
    }

    private Transporter createTransporter(EndpointInfo ei, String address,
        Container container) {
        LOG.info("Create transporter for address: " + address);
        return new LocalTransporter(this, address, ei, container);
    }

    @Override
    public EndpointInfo createEndpointInfo(Container container,
        ServiceInfo serviceInfo, ProtocolInfo b, Object config) {
        EndpointInfo info = new RpcEndpointInfo(serviceInfo, "hola");
        /*if (extensions != null) {
            for (Iterator<?> itr = extensions.iterator(); itr.hasNext();) {
                Object extension = itr.next();
                info.addExtension(extension);
            }
        }*/
        return info;
    }

    void remove(LocalTransporter destination) {
        for (Map.Entry<String, Transporter> e : transporters.entrySet()) {
            if (e.getValue() == destination) {
                transporters.remove(e.getKey());
            }
        }
    }

    /**   */
    public Executor getExecutor(Container container) {
        if (executor == null && container != null) {
            ThreadPoolManager tpm = container.getExtension(ThreadPoolManager.class);
            if (tpm != null) {
                Executor ex = tpm.getThreadPool("local");
                if (ex == null) {
                    ex = tpm.getDefaultThreadPool();
                }
                return ex;
            }
        }
        return executor;
    }

    /**   */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void copy(Message message, Message copy) {
        // copy all the contents
        for (Map.Entry<String, Object> e : message.entrySet()) {
            copy.put(e.getKey(), e.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.support.TransportDetectSupport#getUriPrefixes()
     */
    @Override
    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.support.TransportDetectSupport#getTransportTypes()
     */
    @Override
    public List<String> getTransportTypes() {
        return DEFAULT_TYPE;
    }

}
