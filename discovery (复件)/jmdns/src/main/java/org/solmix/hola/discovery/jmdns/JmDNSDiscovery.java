/*
 * Copyright 2013 The Solmix Project
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

package org.solmix.hola.discovery.jmdns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HolaRuntimeException;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.event.DiscoveryEvent;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.identity.DefaultServiceTypeFactory;
import org.solmix.hola.discovery.identity.DiscoveryID;
import org.solmix.hola.discovery.identity.DiscoveryType;
import org.solmix.hola.discovery.jmdns.identity.JmDNSNamespace;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.hola.discovery.support.AbstractDiscovery1;
import org.solmix.runtime.Container;
import org.solmix.runtime.identity.ID;
import org.solmix.runtime.identity.IDFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月7日
 */
@ThreadSafe
public class JmDNSDiscovery extends AbstractDiscovery1 implements javax.jmdns.ServiceListener, javax.jmdns.ServiceTypeListener, Discovery
{

    public static final int DEFAULT_REQUEST_TIMESOUT = 3000;

    private static final Logger LOG = LoggerFactory.getLogger(JmDNSProvider.class);

    private static final String SCHEME_PROPERTY = "jmdns.ptcl";

    private static final String URI_PATH_PROPERTY = "path";

    private static final String NAMING_AUTHORITY_PROPERTY = "jmdns.namingauthority";

    private static int instanceCount;

    final Object lock = new Object();

    private boolean closed;

    private boolean connected;

    private static int count;

    private LinkedBlockingQueue<Runnable> queue;

    private Thread notificationThread;

    private final List<DiscoveryType> serviceTypes;

    private final Map<String, DiscoveryInfo> services = Collections.synchronizedMap(new HashMap<String, DiscoveryInfo>());;

    private JmDNS jmdns;


    /**
     * @param discoveryNamespace
     * @throws DiscoveryException
     */
    public JmDNSDiscovery(String multiAddress, Container container) throws DiscoveryException
    {
        super(JmDNSNamespace.NAME, container);
        serviceTypes = new ArrayList<DiscoveryType>();
        synchronized (lock) {
            startQueue();
            try {
                jmdns = JmDNS.create(multiAddress);
                jmdns.addServiceTypeListener(this);
            } catch (IOException e) {
                if (jmdns != null) {
                    try {
                        jmdns.close();
                    } catch (IOException e1) {
                    } // ignore
                    jmdns = null;
                }
                throw new DiscoveryException("JMDNS can't created,Check network connection", e);
            }
        }
    }

    @Override
    public void register(DiscoveryInfo serviceInfo) {
        Assert.isNotNull(serviceInfo);
        final ServiceInfo info = createServiceInfo(serviceInfo);
        try {
            jmdns.registerService(info);
        } catch (IOException e) {
            throw new HolaRuntimeException("Exception registering service", e);
        }
    }

    @Override
    public void unregister(DiscoveryInfo serviceInfo) {
        Assert.isNotNull(serviceInfo);
        final ServiceInfo info = createServiceInfo(serviceInfo);
        jmdns.unregisterService(info);

    }

    @Override
    public void unregisterAll() {
        jmdns.unregisterAllServices();
    }

    @Override
    public DiscoveryInfo getService(DiscoveryID id) {
        Assert.isNotNull(id);
        synchronized (lock) {
            try {

                final ServiceInfo[] serviceInfos = jmdns.list(id.getServiceType().getInternal());
                for (int i = 0; i < serviceInfos.length; i++) {
                    ServiceInfo serviceInfo = serviceInfos[i];
                    DiscoveryInfo iServiceInfo = createServiceMetadata(serviceInfo);
                    Assert.isNotNull(iServiceInfo);
                    Assert.isNotNull(iServiceInfo.getServiceID());
                    if (iServiceInfo.getServiceID().equals(id)) {
                        return iServiceInfo;
                    }
                }
                return null;
            } catch (final Exception e) {
                LOG.trace("getServiceInfo", e);
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices()
     */
    @Override
    public DiscoveryInfo[] getServices() {
        synchronized (lock) {
            final DiscoveryType[] serviceTypeArray = getServiceTypes();
            final List<DiscoveryInfo> results = new ArrayList<DiscoveryInfo>();
            for (int i = 0; i < serviceTypeArray.length; i++) {
                final DiscoveryType stid = serviceTypeArray[i];
                if (stid != null)
                    results.addAll(Arrays.asList(getServices(stid)));
            }
            return results.toArray(new DiscoveryInfo[] {});
        }
    }

    @Override
    public DiscoveryInfo[] getServices(DiscoveryType type) {
        Assert.isNotNull(type);
        final List<DiscoveryInfo> metas = new ArrayList<DiscoveryInfo>();
        synchronized (lock) {
            for (final Iterator<DiscoveryType> it = serviceTypes.iterator(); it.hasNext();) {
                final DiscoveryType serviceType = it.next();
                if (Arrays.equals(serviceType.getServices(), type.getServices()) && Arrays.equals(serviceType.getProtocols(), type.getProtocols())
                    && Arrays.equals(serviceType.getScopes(), type.getScopes())) {
                    final ServiceInfo[] infos = jmdns.list(type.getInternal());
                    for (int i = 0; i < infos.length; i++) {
                        try {
                            if (infos[i] != null) {
                                final DiscoveryInfo si = createServiceMetadata(infos[i]);
                                if (si != null)
                                    metas.add(si);
                            }
                        } catch (final Exception e) {
                            LOG.error("getServices(" + type.getName() + ")", e);
                        }
                    }
                }
            }
        }
        return metas.toArray(new DiscoveryInfo[] {});
    }

    @Override
    public DiscoveryType[] getServiceTypes() {
        synchronized (lock) {
            return serviceTypes.toArray(new DiscoveryType[] {});
        }
    }

    @Override
    public DiscoveryInfo[] purgeCache() {
        synchronized (lock) {
            serviceTypes.clear();
        }
        return new DiscoveryInfo[] {};
    }

    private ID getDefaultTargetId() {
        return IDFactory.getDefault().createStringID(JmDNSProvider.class.getName() + "@" + instanceCount++);
    }

    private void startQueue() {
        connected = true;
        queue = new LinkedBlockingQueue<Runnable>();
        notificationThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!closed && connected) {
                    if (Thread.currentThread().isInterrupted())
                        break;
                    final Runnable run = queue.peek();
                    if (run == null)
                        break;
                    try {
                        run.run();
                    } catch (final Throwable t) {
                        LOG.error("queue exception", t);
                    }
                }
            }
        }, "JMDNS Discovery Thread");
        notificationThread.start();
    }

    @Override
    public void close() throws IOException {
        super.close();
        synchronized (lock) {
            if (closed)
                return;
            notificationThread.interrupt();
            notificationThread = null;
            serviceTypes.clear();
            closed = true;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.jmdns.ServiceTypeListener#serviceTypeAdded(javax.jmdns.ServiceEvent)
     */
    @Override
    public void serviceTypeAdded(ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("serviceTypeAdded:" + event);
        event.getDNS().addServiceListener(event.getType(), JmDNSDiscovery.this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.jmdns.ServiceTypeListener#subTypeForServiceTypeAdded(javax.jmdns.ServiceEvent)
     */
    @Override
    public void subTypeForServiceTypeAdded(ServiceEvent event) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
     */
    @Override
    public void serviceAdded(final ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("------------------serviceAdded(" + event + ")");
        try {
            queue.put(new Runnable() {

                @Override
                public void run() {
                    final String serviceType = event.getType();
                    final String serviceName = event.getName();
                    DiscoveryInfo meta = null;
                    synchronized (lock) {
                        if (closed) {
                            return;
                        }
                        try {
                            final ServiceInfo info = event.getDNS().getServiceInfo(serviceType, serviceName);
                            meta = createServiceMetadata(info);
                            serviceTypes.add(meta.getServiceID().getServiceType());
                            services.put(serviceType + serviceName, meta);
                        } catch (Exception e) {
                            LOG.trace("Failed to resolve in serviceAdded(" + event.getName() + ")");
                        }
                    }
                    fireServiceTypeDiscovered(new DiscoveryTypeEvent(this, meta.getServiceID().getServiceType()));
                    fireServiceDiscovered(new DiscoveryEvent(this, meta));
                }

            });
        } catch (InterruptedException e) {
            LOG.error("serviceAdded() exception:", e);
        }
    }

    /**
     * @param serviceInfo
     * @return
     */
    private ServiceInfo createServiceInfo(DiscoveryInfo serviceInfo) {
        if (serviceInfo == null)
            return null;
        ServiceProperties prop = serviceInfo.getServiceProperties();
        final Hashtable<String, Object> props = new Hashtable<String, Object>();
        if (prop != null) {
            for (final Enumeration<String> e = prop.keys(); e.hasMoreElements();) {
                final String key = e.nextElement();
                final Object value = prop.getProperty(key);
                if (value instanceof String) {
                    props.put(key, value);
                } else if (value instanceof java.io.Serializable) {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        final ObjectOutputStream out = new ObjectOutputStream(bos);
                        out.writeObject(value);
                        out.close();
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                    final byte[] buf = bos.toByteArray();
                    props.put(key, buf);
                } else if (value != null) {
                    props.put(key, value.toString());
                }
            }
        }
        final URI location = serviceInfo.getServiceID().getLocation();
        if (location != null) {
            props.put(SCHEME_PROPERTY, location.getScheme());
            props.put(URI_PATH_PROPERTY, location.getPath());
        }

        final DiscoveryID id = serviceInfo.getServiceID();
        props.put(NAMING_AUTHORITY_PROPERTY, id.getServiceType().getNamingAuthority());
        final ServiceInfo si = ServiceInfo.create(id.getServiceType().getInternal(), serviceInfo.getServiceName(), location.getPort(),
            serviceInfo.getWeight(), serviceInfo.getPriority(), props);
        return si;
    }

    /**
     * @param info
     * @return
     */
    protected DiscoveryInfo createServiceMetadata(ServiceInfo info) throws Exception {
        Assert.isNotNull(info);
        final int priority = info.getPriority();
        final int weight = info.getWeight();
        final Hashtable<String, Object> props = new Hashtable<String, Object>();
        String uriProtocol = null;
        String uriPath = null;
        String namingAuthority =DiscoveryType.DEFAULT_NA;
        for (final Enumeration<String> e = info.getPropertyNames() ;e.hasMoreElements();) {
            final String key = e.nextElement();
            if (SCHEME_PROPERTY.equals(key)) {
                uriProtocol =info.getPropertyString(key);
            } else if (NAMING_AUTHORITY_PROPERTY.equals(key)) {
                namingAuthority = info.getPropertyString(key);
            } else if (URI_PATH_PROPERTY.equals(key)) {
                uriPath =info.getPropertyString(key);
            } else {
                final byte[] bytes = info.getPropertyBytes(key);
                try {
                    final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    final Object object = in.readObject();
                    in.close();
                    props.put(key, object);
                } catch (final StreamCorruptedException ioe) {
                    props.put(key, info.getPropertyString(key));
                } catch (final EOFException eofe) { // not all byte[] are
                                                    // serialized objs (e.g. a
                                                    // native service)
                    props.put(key, info.getPropertyString(key));
                }
            }
        }
        final String proto = info.getProtocol();
        // scopes
        final String domain = info.getDomain();
        final String[] scopes = new String[] { domain };

        // uri
        String authority = info.getHostAddress() + ":" + info.getPort();
        final URI uri = new URI(uriProtocol == null ? proto : uriProtocol, authority, uriPath, null, null);
        // service type
        String st = info.getType();
        final int end = st.indexOf(proto);
        String[] types = StringUtils.split(st.substring(1, end), "._");
        final DiscoveryType sID = DefaultServiceTypeFactory.getDefault().create(getNamespace(), types, scopes, new String[] { proto },
            namingAuthority);

        // service name
        final String name = info.getName();

        return new DiscoveryInfoImpl(uri, name, sID, priority, weight, new ServiceProperties(props));

    }

    @Override
    public void serviceRemoved(final ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("------------------serviceRemoved(" + event + ")");
        try {
            queue.put(new Runnable() {

                @Override
                public void run() {
                    final String serviceType = event.getType();
                    final String serviceName = event.getName();
                    DiscoveryInfo metadata = services.remove(serviceType + serviceName);
                    if (metadata == null) {
                        if (LOG.isTraceEnabled())
                            LOG.trace("Failed to resolve in serviceRemoved(" + event.getName() + ")");
                        return;
                    }
                    fireServiceUnDiscovered(new DiscoveryEvent(this, metadata));
                }
            });
        } catch (InterruptedException e) {
            LOG.error("serviceAdded() exception:", e);
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("serviceResolved(" + event.getName() + ")");

    }

}
