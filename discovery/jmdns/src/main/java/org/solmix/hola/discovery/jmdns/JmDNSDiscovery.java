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

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.HolaRuntimeException;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.common.model.ServiceType;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.hola.discovery.support.FailbackDiscovery;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月7日
 */
@ThreadSafe
public class JmDNSDiscovery extends FailbackDiscovery implements javax.jmdns.ServiceListener, javax.jmdns.ServiceTypeListener, Discovery
{

    public static final int DEFAULT_REQUEST_TIMESOUT = 3000;

    private static final Logger LOG = LoggerFactory.getLogger(JmDNSDiscovery.class);


    final Object lock = new Object();

    private boolean closed;

    private boolean connected;

    private volatile boolean admin = false;
    
    private LinkedBlockingQueue<Runnable> queue;

    private Thread notificationThread;

    private JmDNS jmdns;
    private final ScheduledFuture<?> cleanFuture;
    private final ScheduledExecutorService cleanExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("JmDNSCleanTimer", true));

    private int expirePeriod;
    public JmDNSDiscovery(Dictionary<String, ?> properties, Container container) throws DiscoveryException
    {
        super(properties, container);
        String multiAddress = PropertiesUtils.getString(properties, HOLA.HOST_KEY);
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
                throw new DiscoveryException("JmDNS can't created,Check network connection", e);
            }
        }
        this.expirePeriod=PropertiesUtils.getInt(properties, HOLA.DISCOVERY_SESSION_TIMEOUT, HOLA.DEFAULT_SESSION_TIMEOUT);
        if(PropertiesUtils.getBoolean(properties, "clean", true)){
            this.cleanFuture = cleanExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        clean(); // 清除过期者
                    } catch (Throwable t) { // 防御性容错
                        LOG.error("Unexpected exception occur at clean expired provider, cause: " + t.getMessage(), t);
                    }
                }
            }, expirePeriod, expirePeriod, TimeUnit.MILLISECONDS);
        }else{
            this.cleanFuture=null;
        }
    }

    private void clean() {
        if (admin) {
                for (DiscoveryInfo info : new HashSet<DiscoveryInfo>(registered)) {
                    if (isExpired(info)) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Clean expired provider " + info);
                        }
                        doUnregister(info);
                    }
                }
        }
    }
    
    private boolean isExpired(DiscoveryInfo info) {
        Dictionary<String, ?>properties=info.getServiceProperties();
        if (!PropertiesUtils.getBoolean(properties, HOLA.DYNAMIC_KEY, true)
                  || PropertiesUtils.getInt(properties, HOLA.PORT_KEY,-1) <= 0
                  || HOLA.CONSUMER_CATEGORY.equals(PropertiesUtils.getString(properties, HOLA.CATEGORY_KEY))) {
            return false;
        }
        String host =PropertiesUtils.getString(properties, HOLA.HOST_KEY);
        int port =PropertiesUtils.getInt(properties, HOLA.PORT_KEY);
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (Throwable e) {
            try {
                Thread.sleep(100);
            } catch (Throwable e2) {
            }
            Socket socket2 = null;
            try {
                socket2 = new Socket(host, port);
            } catch (Throwable e2) {
                return true;
            } finally {
                if (socket2 != null) {
                    try {
                        socket2.close();
                    } catch (Throwable e2) {
                    }
                }
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e) {
                }
            }
        }
        return false;
    }
    
    @Override
    public void unregisterAll() {
        jmdns.unregisterAllServices();
    }

    @Override
    public DiscoveryInfo getService(ServiceID id) {
        Assert.isNotNull(id);
        synchronized (lock) {
            try {

                final ServiceInfo[] serviceInfos = jmdns.list(id.getServiceType().getIdentityName());
                for (int i = 0; i < serviceInfos.length; i++) {
                    ServiceInfo serviceInfo = serviceInfos[i];
                    DiscoveryInfo iServiceInfo = createDiscoveryInfo(serviceInfo);
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

    @Override
    public DiscoveryInfo[] getServices() {
        synchronized (lock) {
            return registered.toArray(new DiscoveryInfo[] {});
        }
    }

    @Override
    public DiscoveryInfo[] getServices(ServiceType type) {
        Assert.isNotNull(type);
        final List<DiscoveryInfo> metas = new ArrayList<DiscoveryInfo>();
        synchronized (lock) {
            for (final Iterator<DiscoveryInfo> it = registered.iterator(); it.hasNext();) {
                final DiscoveryInfo dinfo = it.next();
                if (type.equals(dinfo.getServiceID().getServiceType())) {
                    final ServiceInfo[] infos = jmdns.list(type.getIdentityName());
                    for (int i = 0; i < infos.length; i++) {
                        try {
                            if (infos[i] != null) {
                                final DiscoveryInfo si = createDiscoveryInfo(infos[i]);
                                if (si != null)
                                    metas.add(si);
                            }
                        } catch (final Exception e) {
                            LOG.error("getServices(" + type.getIdentityName() + ")", e);
                        }
                    }
                }
            }
        }
        return metas.toArray(new DiscoveryInfo[] {});
    }

    @Override
    public ServiceType[] getServiceTypes() {
        synchronized (lock) {
            Set<ServiceType> typeSet= new HashSet<ServiceType>();
            for (final Iterator<DiscoveryInfo> it = registered.iterator(); it.hasNext();) {
                DiscoveryInfo info = it.next();
                if(info.getServiceID()==null){
                    continue;
                }
                if(!typeSet.contains(info.getServiceID().getServiceType())){
                    typeSet.add(info.getServiceID().getServiceType());
                }
            }
            return typeSet.toArray(new ServiceType[] {});
        }
    }

    public DiscoveryInfo[] purgeCache() {
        DiscoveryInfo[]  tmp  = registered.toArray(new DiscoveryInfo[registered.size()]);
        synchronized (lock) {
            registered.clear();
        }
        return tmp;
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
                    if (run == null){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {}
                        continue;
                    }
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
    public void destroy()  {
        super.destroy();
        try {
            if (cleanFuture != null) {
                cleanFuture.cancel(true);
            }
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
        synchronized (lock) {
            if (closed)
                return;
            try {
                jmdns.close();
            } catch (IOException e) {
               throw new DiscoveryException( e);
            }
            notificationThread.interrupt();
            notificationThread = null;
            registered.clear();
            closed = true;
        }
    }

    @Override
    public void serviceTypeAdded(ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("serviceTypeAdded:" + event);
        event.getDNS().addServiceListener(event.getType(), JmDNSDiscovery.this);
    }

    @Override
    public void subTypeForServiceTypeAdded(ServiceEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("serviceAdded(" + event + ")");
        try {
            queue.put(new Runnable() {

                @Override
                public void run() {
                    final String serviceType = event.getType();
                    final String serviceName = event.getName();
                    DiscoveryInfo discoveryInfo = null;
                    synchronized (lock) {
                        if (closed) {
                            return;
                        }
                        try {
                            final ServiceInfo info = event.getDNS().getServiceInfo(serviceType, serviceName);
                            discoveryInfo = createDiscoveryInfo(info);
                            registered.add(discoveryInfo);
                            notifyListener(discoveryInfo,DiscoveryTypeEvent.REGISTER);
                        } catch (Exception e) {
                            LOG.trace("Failed to resolve in serviceAdded(" + event.getName() + ")",e);
                        }
                    }
                    
                }

            });
        } catch (InterruptedException e) {
            LOG.error("serviceAdded() exception:", e);
        }
    }
    
    private void notifyListener(DiscoveryInfo info,int eventType){
        Map<ServiceType, Set<ServiceTypeListener>> typeListeners = new HashMap<ServiceType, Set<ServiceTypeListener>>(getTypeListeners());
        for(Map.Entry<ServiceType, Set<ServiceTypeListener>> entry:typeListeners.entrySet()){
            ServiceType type = entry.getKey();
            Set<ServiceTypeListener> listeners = entry.getValue();
            if(isMatch(type, info)){
                if(listeners!=null&&listeners.size()>0){
                    for(ServiceTypeListener listener:listeners){
                        doNotify(type, listener, Arrays.asList(info), eventType);
                    }
                }
            }
        }
    }

   

    @Override
    public void serviceRemoved(final ServiceEvent event) {
        if (LOG.isTraceEnabled())
            LOG.trace("------------------serviceRemoved(" + event + ")");
        try {
            queue.put(new Runnable() {

                @Override
                public void run() {
                    DiscoveryInfo discoveryInfo = null;
                    try {
                    final String serviceType = event.getType();
                    final String serviceName = event.getName();
                    final ServiceInfo info = event.getDNS().getServiceInfo(serviceType, serviceName);
                    discoveryInfo= createDiscoveryInfo(info);
                    registered.remove(discoveryInfo);
                    notifyListener(discoveryInfo,DiscoveryTypeEvent.REGISTER);
                    } catch (Exception e) {
                        LOG.trace("Failed to resolve in serviceAdded(" + event.getName() + ")");
                    }
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

    @Override
    protected void doRegister(DiscoveryInfo meta) {
        final ServiceInfo info = createServiceInfo(meta);
        try {
            jmdns.registerService(info);
        } catch (IOException e) {
            throw new HolaRuntimeException("Exception registering service", e);
        }
    }

    @Override
    protected void doUnregister(DiscoveryInfo meta) {
        final ServiceInfo info = createServiceInfo(meta);
        jmdns.unregisterService(info);
    }

    @Override
    protected void doSubscribe(ServiceType type, ServiceTypeListener listener) {
        DiscoveryInfo[] infos= getServices(type);
        doNotify(type, listener, Arrays.asList(infos), DiscoveryTypeEvent.REGISTER);
    }

    @Override
    protected void doUnsubscribe(ServiceType type, ServiceTypeListener listener) {
        
    }
    
    private ServiceInfo createServiceInfo(DiscoveryInfo discoveryInfo) {
        if (discoveryInfo == null){
            return null;
        }
        Dictionary<String, ?> prop = discoveryInfo.getServiceProperties();
        final ServiceID id = discoveryInfo.getServiceID();
        int port = PropertiesUtils.getInt(prop, HOLA.PORT_KEY);
        final ServiceInfo si = ServiceInfo.create(id.getServiceType().getIdentityName(), 
                                                discoveryInfo.getServiceID().getName(), 
                                                port,
                                                discoveryInfo.getWeight(), 
                                                discoveryInfo.getPriority(), 
                                                "");
        return si;
       /* 
        final Hashtable<String, Object> props = new Hashtable<String, Object>();
        if (prop != null) {
            for (final Enumeration<String> e = prop.keys(); e.hasMoreElements();) {
                final String key = e.nextElement();
                final Object value = prop.get(key);
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
        }*/
    }

    protected DiscoveryInfo createDiscoveryInfo(ServiceInfo info) throws Exception {
        Assert.isNotNull(info);
        // service name
        final String name = info.getName();
        return new DiscoveryInfoImpl(PropertiesUtils.toProperties(name));

       /* final int priority = info.getPriority();
        final int weight = info.getWeight();
        final Hashtable<String, Object> props = new Hashtable<String, Object>();
        String uriProtocol = null;
        String uriPath = null;
        String namingAuthority =ServiceType.DEFAULT_NA;
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
        */
    }
}
