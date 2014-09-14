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

package org.solmix.hola.discovery.jmdns.internal;

import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.solmix.hola.core.ConnectContext;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryAdvertiser;
import org.solmix.hola.discovery.DiscoveryLocator;
import org.solmix.hola.discovery.jmdns.JmDNSProvider;
import org.solmix.hola.discovery.jmdns.identity.JmDNSNamespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月4日
 */

public class Activator implements BundleActivator
{

    private BundleContext context;

    private static Activator plugin;

    public Activator()
    {
        super();
        plugin = this;
    }

    public static final Activator getDefault() {
        return plugin;
    }

    /**
     * @return the context
     */
    public BundleContext getBundleContext() {
        return context;
    }

    private ServiceRegistration<?> namespaceRegistration;

    private JmDNSServiceFactory factory;

    private ServiceRegistration<?> jmdnsRegistration;

    private final Object logServiceTrackerLock=new Object();

    private ServiceTracker<?,?> logServiceTracker;

    private LogService logService;

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        namespaceRegistration = this.context.registerService(
            Namespace.class.getName(),
            new JmDNSNamespace("Discovery Namespace"), null);
        String[] clazzes = new String[] { Discovery.class.getName(),
            DiscoveryAdvertiser.class.getName(),
            DiscoveryLocator.class.getName() };
        final Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_RANKING, new Integer(750));
        factory = new JmDNSServiceFactory();
        jmdnsRegistration = context.registerService(clazzes, factory, props);

    }
    public LogService getLogService() {
        if (context == null)
              return null;
        synchronized (logServiceTrackerLock) {
              if (logServiceTracker == null) {
                    logServiceTracker = new ServiceTracker<LogService,LogService>(context,
                                LogService.class.getName(), null);
                    logServiceTracker.open();
              }
              logService = (LogService) logServiceTracker.getService();
              return logService;
        }
  }
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (namespaceRegistration != null)
            namespaceRegistration.unregister();
        if (jmdnsRegistration != null&&factory.isActive()){
            ServiceReference<?> reference=   jmdnsRegistration.getReference();
            Discovery service=  (Discovery)context.getService(reference);
            jmdnsRegistration.unregister();
            ConnectContext cc=service.adaptTo(ConnectContext.class);
            cc.disconnect();
            cc.destroy();
        }
        synchronized (logServiceTrackerLock) {
            if(logServiceTracker!=null)
                logServiceTracker.close();
        }
        this.context = null;

    }
    class JmDNSServiceFactory implements ServiceFactory<JmDNSProvider>{

        private volatile JmDNSProvider service;

        @Override
        public JmDNSProvider getService(Bundle bundle,
            ServiceRegistration<JmDNSProvider> registration) {
            if (service == null) {
                try {
                    service = new JmDNSProvider();
//                    service.connect(null, null);
                } catch (Exception e) {
                    getLogService().log(LogService.LOG_ERROR, e.getMessage());
                }
            }
            return service;
        }

        @Override
        public void ungetService(Bundle bundle,
            ServiceRegistration<JmDNSProvider> registration,
            JmDNSProvider service) {

        }
        public boolean isActive(){
            return service!=null;
        }
    }

}
