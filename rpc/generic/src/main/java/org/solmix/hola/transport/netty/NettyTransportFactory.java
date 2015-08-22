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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.solmix.hola.transport.AbstractTCPTransporter;
import org.solmix.hola.transport.TransporterRegistry;
import org.solmix.hola.transport.support.DefaultTcpRegistry;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerEvent;
import org.solmix.runtime.ContainerListener;
import org.solmix.runtime.Extension;
import org.solmix.runtime.bean.BeanConfigurer;
import org.solmix.exchange.Pipeline;
import org.solmix.exchange.PipelineFactory;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.TransporterFactory;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.TransportDetectSupport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */
@Extension(name = NettyTransportFactory.TRANSPORT_TYPE)
public class NettyTransportFactory implements PipelineFactory,
    TransporterFactory, TransportDetectSupport {

    private static final Set<String> URI_PREFIXES = new HashSet<String>();

    public static final String TRANSPORT_TYPE = "netty";

    public static final List<String> DEFAULT_TYPE = Arrays.asList(TRANSPORT_TYPE);
    static {
        URI_PREFIXES.add("netty://");
        URI_PREFIXES.add("hola://");
    }

    private final TransporterRegistry registry = new DefaultTcpRegistry();

    @Override
    public Transporter getTransporter(EndpointInfo ei, Container container)
        throws IOException {
        if (ei == null) {
            throw new IllegalArgumentException("EndpointInfo cannot be null");
        }
        synchronized (registry) {
            AbstractTCPTransporter t = registry.getTransporterForPath(ei.getAddress());
            if (t == null) {
                NettyServerEngineFactory factory = container.getExtension(NettyServerEngineFactory.class);
                t = new NettyTransporter(factory,ei, container,registry);
                registry.add(t);
                configure(container, t);
                t.finalizeConfig();
            }
            return t;
        }
    }

    protected void configure(Container container, Object bean) {
        configure(container, bean, null, null);
    }

    protected void configure(Container container, Object bean, String name,
        String extraName) {
        BeanConfigurer configurer = container.getExtension(BeanConfigurer.class);
        if (null != configurer) {
            configurer.configureBean(name, bean);
            if (extraName != null) {
                configurer.configureBean(extraName, bean);
            }
        }
    }

    @Override
    public Pipeline getPipeline(EndpointInfo info, Container c)
        throws IOException {
        return getPipeline(info, info.getAddress(), c);
    }

    @Override
    public Pipeline getPipeline(EndpointInfo info, String address, Container c)
        throws IOException {
        if (!address.equals(info.getAddress())) {
            info.setAddress(address);
        }
        address = info.getAddress();
        if (address.startsWith("netty://")) {
            address = address.substring(8);
        }
        info.setAddress(address);
        NettyPipeline np = createPipeline(c, info);
        String addr = np.getAddress();
        if (addr != null && addr.indexOf('?') != -1) {
            addr = addr.substring(0, addr.indexOf('?'));
        }
        configure(c, np, np.getBeanName(), addr);
        return np;
    }

    private NettyPipeline createPipeline(Container c, EndpointInfo info) {
        EventLoopGroup eventLoopGroup = c.getExtension(EventLoopGroup.class);
        if (eventLoopGroup == null) {
            final EventLoopGroup group = new NioEventLoopGroup();
            c.setExtension(group, EventLoopGroup.class);
            registerContainerListener(c, group);
        }
        return new NettyPipeline(c, info);
    }

    private void registerContainerListener(Container c,
        final EventLoopGroup group) {
        c.addListener(new ContainerListener() {

            @Override
            public void handleEvent(ContainerEvent event) {
                if (event.getType() == ContainerEvent.POSTCLOSE) {
                    group.shutdownGracefully().syncUninterruptibly();
                } else {
                    return;
                }

            }
        });

    }

    @Override
    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.support.TransportDetectSupport#getTransportTypes()
     */
    @Override
    public List<String> getTransportTypes() {
        return DEFAULT_TYPE;
    }

}
