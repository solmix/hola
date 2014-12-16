/*
 * Copyright 2014 The Solmix Project
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

package org.solmix.hola.rt;

import java.rmi.RemoteException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.util.SystemPropertyAction;
import org.solmix.hola.common.config.DiscoveryConfig;
import org.solmix.hola.common.config.RemoteServiceConfig;
import org.solmix.hola.common.config.ServiceConfig;
import org.solmix.hola.common.config.ServerConfig;
import org.solmix.hola.common.security.HolaServicePermission;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.hola.rm.RemoteListener;
import org.solmix.hola.rm.RemoteManager;
import org.solmix.hola.rm.RemoteManagerFactory;
import org.solmix.hola.rm.RemoteReference;
import org.solmix.hola.rm.RemoteRegistration;
import org.solmix.hola.rm.event.RemoteEvent;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月10日
 */

public class GenericPublisher implements ServicePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(GenericPublisher.class);

    private static final HolaServicePermission PUBLISH_PERMISSION = new HolaServicePermission(
        "publishEndpoint");

    protected ServiceConfig<?> service;

    private final Container container;

    private volatile boolean unpublished;

    private volatile boolean published;

    private final List<RemoteRegistration<?>> registrations = new ArrayList<RemoteRegistration<?>>();

    private final static String CHECK_PUBLISH_SERVICE_PERMISSION_WITH_SECURITYMANAGER = "org.solmix.hola.service.publish.permission.withSecurityManager";

    private final static String CHECK_PUBLISH_SERVICE_PERMISSION = "org.solmix.hola.service.publish.permission";

    public GenericPublisher(ServiceConfig<?> type) {
        this.service = type;
        Assert.isNotNull(service);
        this.container = type.getContainer();
        Assert.isNotNull(container);

    }

    @Override
    public ServiceConfig<?> getService() {
        return service;
    }

    @Override
    public synchronized void publish() {
        if (unpublished) {
            throw new IllegalStateException("Service already unpublished!");
        }
        if (published) {
            return;
        }
        checkPublishPermission();
        published = true;
        service = checkService(service);
        // 可以设置为普通服务,不发布服务.
        if (service.isPublish() != null && !service.isPublish().booleanValue()) {
            return;
        }
        // 延迟启动
        final Integer delay = service.getDelay();
        if (delay != null && delay > 0) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (Throwable e) {
                    }
                    doPublish();
                }
            });
            thread.setDaemon(true);
            thread.setName("DelayPublishThread");
            thread.start();
        } else {
            doPublish();
        }

    }

    protected void checkPublishPermission() {
        SecurityManager sm = System.getSecurityManager();
        boolean publishServicePermission = Boolean.valueOf(SystemPropertyAction.getProperty(CHECK_PUBLISH_SERVICE_PERMISSION_WITH_SECURITYMANAGER));
        if (sm != null && publishServicePermission) {
            sm.checkPermission(PUBLISH_PERMISSION);
        } else if (Boolean.valueOf(SystemPropertyAction.getProperty(CHECK_PUBLISH_SERVICE_PERMISSION))) {
            AccessController.checkPermission(PUBLISH_PERMISSION);
        }
    }

    protected void doPublish() {
        List<ServerConfig> servers = service.getServers();
        if (servers == null || servers.size() == 0) {
            ServerConfig defaultServer = createDefaultServer(service);
            if (defaultServer != null) {
                service.setServer(defaultServer);
            }
        }
        servers = service.getServers();
        if (servers != null)
            for (ServerConfig server : servers) {
                ClassLoaderHolder loader = null;
                try {
                    if (container != null) {
                        ClassLoader cl = container.getExtension(ClassLoader.class);
                        if (cl != null) {
                            loader = ClassLoaderUtils.setThreadContextClassloader(cl);
                        }
                    }
                    doPublishPeerServer(server);
                } finally {
                    if (loader != null) {
                        loader.reset();
                    }
                }
                
            }
    }

    /**
     * 创建默认server
     */
    protected ServerConfig createDefaultServer(ServiceConfig<?> service) {
        // 根据properties文件创建默认server
        ClassLoaderHolder loader = null;
        try {
            if (container != null) {
                ClassLoader cl = container.getExtension(ClassLoader.class);
                if (cl != null) {
                    loader = ClassLoaderUtils.setThreadContextClassloader(cl);
                }
            }
        } finally {
            if (loader != null) {
                loader.reset();
            }
        }
        // TODO Auto-generated method stub
        return null;
    }

    protected void doPublishPeerServer(ServerConfig server) {
        String scope = service.getScope();
        if (!StringUtils.isEmpty(scope)) {
            scope = server.getScope();
        }
        // 如果配置为NONE,不发布任何服务.
        if (!ServerConfig.SCOPE_NONE.equalsIgnoreCase(scope)) {
            // 如果不配置为REMOTE,在本地发布
            if (!ServerConfig.SCOPE_REMOTE.equalsIgnoreCase(scope)) {
                jvmExport(server);
            }
            // 如果不配置为LOCAL,远程发布
            if (!ServerConfig.SCOPE_LOCAL.equalsIgnoreCase(scope)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Publish service :" + service.getInterfaces());
                }
                String protocol = server.getProtocol();
                RemoteManagerFactory manager = container.getExtensionLoader(
                    RemoteManagerFactory.class).getExtension(protocol);
                List<DiscoveryConfig> infos = service.getDiscoveries();
                if (infos == null || infos.size() == 0) {
                    infos = server.getDiscoveries();

                }
                try {
                    // 是否公告服务
                    if ((server.isAdvertise() == null || server.isAdvertise().booleanValue())
                        && (infos != null && infos.size() > 0)) {
                        // 通过discovery来发布
                        RemoteRegistration<?> registration = registerDiscoverys(
                            manager, server, infos);
                        registrations.add(registration);
                    } else {
                        // 直接发布
                        RemoteManager rm = manager.createManager(service.getContainer());
                        RemoteRegistration<?> registration = null;

                        registration = rm.registerService(
                            service.getInterfaces(), service.getRef(),
                            createEndpointInfo(server, service));

                        if (registration != null) {
                            registrations.add(registration);
                        }
                    }
                } catch (RemoteException e) {
                    LOG.error("Exception register service:", e);
                }
            }
        }
    }

    protected RemoteServiceConfig createEndpointInfo(ServerConfig server,
        ServiceConfig<?> service) {
        // TODO
        return null;

    }

    /**
     * 在jvm发布
     * 
     * @param server
     */
    private void jvmExport(ServerConfig server) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void unpublish() {
        if (!published) {
            return;
        }
        if (unpublished) {
            return;
        }
        if (registrations.size() > 0) {
            for (RemoteRegistration<?> registration : registrations) {
                try {
                    registration.unregister();
                } catch (Exception e) {
                    LOG.warn("unexpected err when unregister" + registration, e);
                }
            }
            registrations.clear();
        }
        unpublished = true;
    }

    @Override
    public boolean isPublished() {
        return published;
    }

    @Override
    public boolean isUnpublished() {
        return unpublished;
    }

    protected ServiceConfig<?> checkService(ServiceConfig<?> s) {
        if (s == null) {
            throw new IllegalArgumentException("ServiceConfig<?> is null");
        }
        if (s.getInterface() == null || s.getInterface().length() == 0) {
            throw new IllegalStateException(
                "<hola:service interface=\"\" /> interface not allow null!");
        }
        return s;
    }

    /**
     * 通过监听公告服务.
     */
    private class DiscoveryRemoteListener implements RemoteListener {

        private final DiscoveryConfig info;

        private final ServerConfig serverConfig;

        private final ServiceConfig<?> service;

        DiscoveryRemoteListener(DiscoveryConfig info, ServerConfig serverConfig,
            ServiceConfig<?> service) {
            this.info = info;
            this.serverConfig = serverConfig;
            this.service = service;
        }

        @Override
        public void onHandle(RemoteEvent event) {
            int type = event.getType();
            switch (type) {
                case RemoteEvent.REGISTERED:
                    registerService(event.getRemoteServiceReference());
                    break;
                case RemoteEvent.UNREGISTERED:
                    unregisterService(event.getRemoteServiceReference());
                    break;

            }

        }

        /**
         * @param remoteServiceReference
         */
        private void unregisterService(RemoteReference<?> remoteServiceReference) {
            DiscoveryProvider provider = container.getExtensionLoader(
                DiscoveryProvider.class).getExtension(info.getProtocol());
            Discovery discovery = provider.createDiscovery(info);
            // discovery.register(serviceInfo);
        }

        /**
         * @param remoteServiceReference
         */
        private void registerService(RemoteReference<?> remoteServiceReference) {
            DiscoveryProvider provider = container.getExtensionLoader(
                DiscoveryProvider.class).getExtension(info.getProtocol());
            Discovery discovery = provider.createDiscovery(info);
            // discovery.register(getServiceInfo(remoteInfo));

            // discovery.addServiceListener(type, listener);

        }

        /*
         * private ServiceConfig<?> getServiceInfo(RemoteInfo remoteInfo) {
         * 
         * return new RemoteServiceConfig<Object>(remoteInfo); }
         */
    }

    private RemoteRegistration<?> registerDiscoverys(
        RemoteManagerFactory manager, ServerConfig server,
        List<DiscoveryConfig> infos) throws RemoteException {
        List<RemoteListener> listeners = getRemoteListeners(infos, server);
        RemoteManager rm = manager.createManager(service.getContainer());
        if (listeners != null && listeners.size() > 0) {
            for (RemoteListener listener : listeners) {
                rm.addRemoteListener(listener);
            }
        }
        return rm.registerService(service.getInterfaces(), service.getRef(),
            createEndpointInfo(server, service));
    }

    /**
     * @param infos
     * @return
     */
    private List<RemoteListener> getRemoteListeners(List<DiscoveryConfig> infos,ServerConfig server) {
        List<RemoteListener> listeners = new ArrayList<RemoteListener>();
        for (final DiscoveryConfig info : infos) {
            listeners.add(new DiscoveryRemoteListener(info, server, service));
        }
        return listeners;
    }

}
