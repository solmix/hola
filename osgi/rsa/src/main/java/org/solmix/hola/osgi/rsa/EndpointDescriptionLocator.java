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

package org.solmix.hola.osgi.rsa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.discovery.DiscoveryLocator;
import org.solmix.hola.discovery.ServiceMetadata;
import org.solmix.hola.discovery.event.ServiceEvent;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.osgi.rsa.support.DiscoveredEndpointDescriptionFactoryImpl;
import org.solmix.hola.osgi.rsa.support.EndpointDescriptionReaderImpl;
import org.solmix.hola.osgi.rsa.support.ServiceMetadataFactoryImpl;

/**
 * 通过服务发现/服务注册机制,查找已配置的Endpintdescription
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月2日
 */

public class EndpointDescriptionLocator
{
    private static final Logger LOG= LoggerFactory.getLogger(LocatorServiceListener.class.getName());
    private BundleContext ctx;

    private ServiceRegistration<?> serviceMetadataFactoryRegistration;

    private ServiceMetadataFactory serviceMetadataFactory;

    private DiscoveredEndpointDescriptionFactory discoveredEndpointDescriptionFactory;

    private ServiceRegistration<?> discoveredEndpointDescriptionFactoryRegistration;

    private ServiceRegistration<?> endpointDescriptionReaderRegistration;
  
    private ServiceTracker<EndpointDescriptionReader,EndpointDescriptionReader> endpointDescriptionReaderTracker;

    private ServiceTracker<EndpointListener,EndpointListener> endpointListenerTracker;

    private ServiceTracker<DiscoveryLocator,DiscoveryLocator> locatorServiceTracker;
    
    private LocatorServiceListener localLocatorServiceListener;
    
    private final Object endpointDescriptionFactoryTrackerLock=new Object();
    
    private final Object endpointListenerServiceTrackerLock = new Object();
    
    private final Object endpointDescriptionReaderTrackerLock = new Object();
    
    private ServiceTracker<DiscoveredEndpointDescriptionFactory,DiscoveredEndpointDescriptionFactory> endpointDescriptionFactoryTracker;
    //
    private Map<DiscoveryLocator, LocatorServiceListener> locatorListeners;
    
    private ExecutorService executor;
    private EndpointDescriptionFileTracker endpointDescriptionFileTracker;
    private BundleTracker<?> bundleTracker;
    
    public EndpointDescriptionLocator(BundleContext ctx)
    {
        this.ctx = ctx;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void start() {
        final Properties properties = new Properties();
        // 可以注册,替换默认服务
        properties.put(Constants.SERVICE_RANKING,
            new Integer(Integer.MIN_VALUE));
        serviceMetadataFactory = new ServiceMetadataFactoryImpl();
        // register default serviceMetadataFactory
        serviceMetadataFactoryRegistration = ctx.registerService(
            ServiceMetadataFactory.class.getName(), serviceMetadataFactory,
            (Dictionary) properties);
        // register default endpointDescriptionFactory
        discoveredEndpointDescriptionFactory = new DiscoveredEndpointDescriptionFactoryImpl();
        discoveredEndpointDescriptionFactoryRegistration = ctx.registerService(
            DiscoveredEndpointDescriptionFactory.class.getName(),
            discoveredEndpointDescriptionFactory, (Dictionary) properties);
        // register default EndpointDescriptionReader
        endpointDescriptionReaderRegistration = ctx.registerService(
            EndpointDescriptionReader.class.getName(),
            new EndpointDescriptionReaderImpl(), (Dictionary) properties);
        //跟踪OSGI中注册的EndpointListener,通过EndpointListener来发现网络中注册的Endpoint
        endpointListenerTracker = new ServiceTracker(ctx,
            EndpointListener.class.getName(),
            new ServiceTrackerCustomizer<EndpointListener, EndpointListener>() {

                @Override
                public EndpointListener addingService(
                    ServiceReference<EndpointListener> reference) {
                    if (ctx == null)
                        return null;
                    EndpointListener listener = ctx.getService(reference);
                    if (listener == null)
                        return null;
                    //首次注册,通知所有的信息
                    Collection<EndpointDescription> all = getAllDiscoveredEndpointDescriptions();
                    for (EndpointDescription ed : all) {
                        EndpointListenerHolder[] endpointListenerHolders = getMatchingEndpointListenerHolders(
                            new ServiceReference[] { reference }, ed);
                        if (endpointListenerHolders != null) {
                            for (int i = 0; i < endpointListenerHolders.length; i++) {
                                dispatchEndpointDescription(
                                    endpointListenerHolders[i].getListener(),
                                    endpointListenerHolders[i].getDescription(),
                                    endpointListenerHolders[i].getMatchingFilter(),
                                    true);
                            }
                        }
                    }
                    return listener;
                }

                @Override
                public void modifiedService(
                    ServiceReference<EndpointListener> reference,
                    EndpointListener service) {
                }

                @Override
                public void removedService(
                    ServiceReference<EndpointListener> reference,
                    EndpointListener service) {
                }
            });

        endpointListenerTracker.open();
        //跟踪OSGI中注册的DiscoveryLocator
        locatorServiceTracker = new ServiceTracker(ctx,
            DiscoveryLocator.class.getName(),
            new ServiceTrackerCustomizer<DiscoveryLocator,DiscoveryLocator>(){

                @Override
                public DiscoveryLocator addingService(
                    ServiceReference<DiscoveryLocator> reference) {
                    DiscoveryLocator locator=  ctx.getService(reference);
                    if(locator!=null){
                        //发现并启动服务
                        startDiscoveryLocator(locator);
                    }
                    return locator;
                }

                @Override
                public void modifiedService(
                    ServiceReference<DiscoveryLocator> reference,
                    DiscoveryLocator service) {
                }

                @Override
                public void removedService(
                    ServiceReference<DiscoveryLocator> reference,
                    DiscoveryLocator service) {
                    stopDiscoveryLocator(service);
                }
            
        });
            locatorServiceTracker.open();
            //发现本地服务
            localLocatorServiceListener = new LocatorServiceListener(null);
            endpointDescriptionFileTracker = new EndpointDescriptionFileTracker();
            //发现Bundle中的EndpointDescription
            bundleTracker = new BundleTracker(ctx, Bundle.ACTIVE
                | Bundle.STARTING, endpointDescriptionFileTracker);
            bundleTracker.open();
    }
    public void stop() {
        if(bundleTracker!=null){
            bundleTracker.close();
            bundleTracker=null;
        }
        if(endpointDescriptionFileTracker!=null){
            endpointDescriptionFileTracker.close();
            endpointDescriptionFileTracker=null;
        }
            
        synchronized (locatorListeners) {
            for (DiscoveryLocator l : locatorListeners.keySet()) {
                LocatorServiceListener locatorListener = locatorListeners.get(l);
                if (locatorListener != null) {
                    l.removeServiceListener(locatorListener);
                    locatorListener.close();
                }
            }
            locatorListeners.clear();
        }
        synchronized (endpointDescriptionFactoryTrackerLock) {
            if (endpointDescriptionFactoryTracker != null) {
                endpointDescriptionFactoryTracker.close();
                endpointDescriptionFactoryTracker = null;
            }
        }
        if (localLocatorServiceListener != null) {
            localLocatorServiceListener.close();
            localLocatorServiceListener = null;
        }
        if (endpointListenerTracker != null) {
            endpointListenerTracker.close();
            endpointListenerTracker = null;
        }
        
        if(locatorServiceTracker!=null){
            locatorServiceTracker.close();
            locatorServiceTracker=null;
        }
        if(serviceMetadataFactoryRegistration!=null){
            serviceMetadataFactoryRegistration.unregister();
            serviceMetadataFactoryRegistration=null;
        }
        if(discoveredEndpointDescriptionFactoryRegistration!=null){
            discoveredEndpointDescriptionFactoryRegistration.unregister();
            discoveredEndpointDescriptionFactoryRegistration=null;
        }
        if(endpointDescriptionReaderRegistration!=null){
            endpointDescriptionReaderRegistration.unregister();
            endpointDescriptionReaderRegistration=null;
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        this.ctx = null;
        this.serviceIDs.clear();
    }
    /**
     * @param service
     */
    protected void stopDiscoveryLocator(DiscoveryLocator locator) {
        if (locator == null || ctx == null)
            return;
        synchronized (locatorListeners) {
            LocatorServiceListener locatorListener = locatorListeners.remove(locator);
            if (locatorListener != null)
                locatorListener.close();
        }
    }

    private synchronized ExecutorService getExecutor(){
        if(executor==null){
            executor= Executors.newCachedThreadPool();
        }
        return executor;
    }
    /**
     * @param listener
     * @param description
     * @param matchingFilter
     * @param b
     */
    protected void dispatchEndpointDescription(final EndpointListener listener,
       final EndpointDescription description, final String matchingFilter,final boolean discovered) {
        getExecutor().execute(new Runnable(){

            @Override
            public void run() {
                try {
                    if(discovered)
                        listener.endpointAdded(description, matchingFilter);
                    else
                        listener.endpointRemoved(description, matchingFilter);
                } catch (Exception e) {
                    String message = "Exception in EndpointListener listener="
                        + listener + " description=" + description + " matchingFilter=" 
                        + matchingFilter;
                    LOG.error(message,e);
                } catch (LinkageError e) {
                    String message = "LinkageError in EndpointListener listener="
                        + listener + " description="  + description + " matchingFilter=" 
                        + matchingFilter;
                    LOG.error(message,e);
                } catch (AssertionError e) {
                    String message = "AssertionError in EndpointListener listener="
                        + listener + " description=" + description + " matchingFilter=" 
                        + matchingFilter;
                    LOG.error(message,e);
                }
            }
        });
        
    }
    /**
     * @param endpointDescription
     * @param discovered
     */
    public void dispatchEndpointDescription(
        EndpointDescription endpointDescription, boolean discovered) {
        EndpointListenerHolder[] endpointListenerHolders = getMatchingEndpointListenerHolders(endpointDescription);
        if (endpointListenerHolders != null) {
              for (int i = 0; i < endpointListenerHolders.length; i++) {
                    dispatchEndpointDescription(
                                endpointListenerHolders[i].getListener(),
                                endpointListenerHolders[i].getDescription(),
                                endpointListenerHolders[i].getMatchingFilter(),
                                discovered);

              }
        } else {
              LOG.warn("No matching EndpointListeners found for " 
                                      + (discovered ? "discovered" : "undiscovered")
                                      + " endpointDescription=" + endpointDescription); 
        }
        
    }
   
    protected EndpointListenerHolder[] getMatchingEndpointListenerHolders(
        final EndpointDescription description) {
   
        return AccessController
              .doPrivileged(new PrivilegedAction<EndpointListenerHolder[]>() {
                    @Override
                    public EndpointListenerHolder[] run() {
                          synchronized (endpointListenerServiceTrackerLock) {
                                return getMatchingEndpointListenerHolders(
                                            endpointListenerTracker.getServiceReferences(),
                                            description);
                          }
                    }
              });
}

 
    /**
     * 启动已注册服务
     * @param locator
     */
    protected void startDiscoveryLocator(final DiscoveryLocator locator) {
        synchronized (locatorListeners) {
            final LocatorServiceListener locatorListener = new LocatorServiceListener(
                locator);
            locatorListeners.put(locator, locatorListener);
            getExecutor().execute(new Runnable() {

                @Override
                public void run() {
                    ServiceMetadata[] metadatas = null;
                    try {
                        metadatas = locator.getServices();
                    } catch (Exception e) {// 防御性容错
                        LOG.error("Exception in locator.getServices()", e);
                    }
                    if (metadatas != null) {
                        for (ServiceMetadata metadata : metadatas) {
                            locatorListener.handleService(metadata, true);
                        }
                    }
                }
            });
        }

    }

    Collection<EndpointDescription> getAllDiscoveredEndpointDescriptions() {
        Collection<EndpointDescription> result = new ArrayList<EndpointDescription>();
        if (localLocatorServiceListener == null)
            return result;
        // Get local first
        result.addAll(localLocatorServiceListener.getEndpointDescriptions());
        synchronized (locatorListeners) {
            for (DiscoveryLocator l : locatorListeners.keySet()) {
                LocatorServiceListener locatorListener = locatorListeners.get(l);
                result.addAll(locatorListener.getEndpointDescriptions());
            }
        }
        return result;
    }
    /**
     * @return 在OSGI中查找Factory,可以通过注册服务来覆盖默认Factory
     */
    private DiscoveredEndpointDescriptionFactory getDiscoveredEndpointDescriptionFactory() {
        synchronized (endpointDescriptionFactoryTrackerLock) {
            if (ctx == null)
                return null;
            if (endpointDescriptionFactoryTracker == null) {
                endpointDescriptionFactoryTracker = new ServiceTracker<DiscoveredEndpointDescriptionFactory, DiscoveredEndpointDescriptionFactory>(
                    ctx, DiscoveredEndpointDescriptionFactory.class, null);
                endpointDescriptionFactoryTracker.open();
            }
            return endpointDescriptionFactoryTracker.getService();
        }
    }
  
    /**
     * @return 在OSGI中查找Reader,可以通过注册服务来覆盖默认Reader
     */
    private EndpointDescriptionReader getEndpointDescriptionReader(){
        synchronized (endpointDescriptionReaderTrackerLock) {
            if (endpointDescriptionReaderTracker == null) {
                endpointDescriptionReaderTracker = new ServiceTracker<EndpointDescriptionReader,EndpointDescriptionReader>(
                            ctx, EndpointDescriptionReader.class, null);
                endpointDescriptionReaderTracker.open();
          }
        }
        return endpointDescriptionReaderTracker.getService();
    }

    /**
     * 查找与EndpointListener对应的EndpointDescription
     * 
     * @param refs EndpointListener 服务引用
     * @param description Endpoint 描述信息
     * @return
     */
    public EndpointListenerHolder[] getMatchingEndpointListenerHolders(
        ServiceReference<EndpointListener>[] refs, EndpointDescription description) {
        if (refs == null)
            return null;
        List<EndpointListenerHolder> results = new ArrayList<EndpointListenerHolder>();
        for (int i = 0; i < refs.length; i++) {
            EndpointListener listener =  ctx.getService(refs[i]);
            if (listener == null)
                continue;
            //获取ENDPOINT_LISTENER_SCOPE 字符串
            List<String> filters = PropertiesUtil.getStringPlusProperty(
                getMapFromProperties(refs[i]),
                EndpointListener.ENDPOINT_LISTENER_SCOPE);
            if (filters.size() > 0) {
                String matchingFilter = isMatch(description, filters);
                if (matchingFilter != null)
                    results.add(new EndpointListenerHolder(listener,
                        description, matchingFilter));
            }
        }
        return results.toArray(new EndpointListenerHolder[results.size()]);
    }
  
    private String isMatch(EndpointDescription description, List<String> filters) {
        for (String filter : filters) {
            try {
                  if (description.matches(filter))
                        return filter;
            } catch (IllegalArgumentException e) {
                  LOG.error( "invalid endpoint listener filter="  + filters, e);
            }
      }
      return null;
    }

    /**
     * @return 和服务一同发布的参数
     */
    private Map<String, Object> getMapFromProperties(
        ServiceReference<?> serviceReference) {
        Map<String, Object> results = new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER);
        String[] keys = serviceReference.getPropertyKeys();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                results.put(keys[i], serviceReference.getProperty(keys[i]));
            }
        }
        return results;
    }

    private final List<ServiceID> serviceIDs = new ArrayList<ServiceID>();
    /**
     * 公告服务监听
     */
    class LocatorServiceListener implements org.solmix.hola.discovery.ServiceListener
    {

        private  DiscoveryLocator locator;
        private final Object listenerLock = new Object();
        /**
         * 缓存该监听已知EndpointDescription
         */
        private final List<EndpointDescription> discoveredEndpointDescriptions = new ArrayList<EndpointDescription>();

        public LocatorServiceListener(DiscoveryLocator locator)
        {
            this.locator = locator;
            if (locator != null) {
                this.locator.addServiceListener(this);
            }
        }

        @Override
        public boolean triggerDiscovery() {
            return false;//不触发re-discovery
        }

       //服务发现者(JMDNS/JSLP/ZOOKEEPER)发现服务后,发送ServiceEvent通知
        @Override
        public void discovered(ServiceEvent event) {
            ServiceMetadata metadata = event.getServiceMetadata();
            handleService(metadata,true);
        }

        @Override
        public void undiscovered(ServiceEvent event) {
            ServiceMetadata metadata = event.getServiceMetadata();
            handleService(metadata,false);
        }
        /**
         * 处理发现的服务
         * @param metadata
         * @param discovered 服务发现/服务消失
         */
        void handleService(ServiceMetadata metadata, boolean discovered) {
            LOG.info(new StringBuilder().append(metadata).append(
                " discovered=").append(discovered).toString());
            ServiceID serviceID = metadata.getServiceID();
            if (matchServiceID(serviceID)) {
                  synchronized (serviceIDs) {
                        if (discovered) {
                            if (serviceIDs.contains(serviceID)) {
                                LOG.trace("Found serviceInfo with same serviceID=" + serviceID + "...ignoring"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                return;
                            }
                              serviceIDs.add(serviceID);
                        } else
                              serviceIDs.remove(serviceID);
                  }
                  handleEndpointDescription(serviceID, metadata, discovered);
            }
      }
        /**
         * 移除监听,并清除已发现的EndpointDescription
         */
        public synchronized void close() {
            if (locator != null) {
                locator.removeServiceListener(this);
                locator = null;
            }
            discoveredEndpointDescriptions.clear();
        }

        private boolean matchServiceID(ServiceID serviceId) {
            if (Arrays.asList(serviceId.getServiceType().getServices()).contains(
                HolaRemoteConstants.DISCOVERY_SERVICE_TYPE))
                return true;
            return false;
        }
        
        /**
         * 根据ID和metadata,找到DiscoveredEndpointDescription
         */
        private void handleEndpointDescription(ServiceID serviceID,
            ServiceMetadata metadata, boolean discovered) {
            if (locator == null)
                return;
            DiscoveredEndpointDescription discoveredEndpointDescription = getDiscoveredEndpointDescription(
                serviceID, metadata, discovered);
            if (discoveredEndpointDescription != null) {
                handleEndpointDescription(
                    discoveredEndpointDescription.getEndpointDescription(),
                    discovered);
            } else {
                LOG.warn("DiscoveredEndpointDescription is null for Service Metadata="
                    + metadata + " ,discovered=" + discovered);
            }
        }

        /**
         * 创建EndpointDescription
         */
        private DiscoveredEndpointDescription getDiscoveredEndpointDescription(
            ServiceID serviceID, ServiceMetadata metadata, boolean discovered) {
            DiscoveredEndpointDescriptionFactory factory = getDiscoveredEndpointDescriptionFactory();
            return discovered ? factory.create(locator, metadata)
                : factory.remove(locator, serviceID);
        }

  

        /**
         * @param endpointDescription
         * @param discovered
         */
        private void handleEndpointDescription(
            EndpointDescription endpointDescription, boolean discovered) {
            synchronized (listenerLock) {
                if (discovered) {
                      if (discoveredEndpointDescriptions
                                  .contains(endpointDescription)) {
                            LOG.trace("endpointDescription previously discovered...ignoring"); //$NON-NLS-1$ //$NON-NLS-2$
                            return;
                      }
                      discoveredEndpointDescriptions.add(endpointDescription);
                } else
                      discoveredEndpointDescriptions.remove(endpointDescription);
                dispatchEndpointDescription(endpointDescription, discovered);
          }
        }

        public Collection<EndpointDescription> getEndpointDescriptions() {
              synchronized (listenerLock) {
                    Collection<EndpointDescription> result = new ArrayList<EndpointDescription>();
                    result.addAll(discoveredEndpointDescriptions);
                    return result;
              }
        }
    }
    
    public class EndpointListenerHolder {

        private final EndpointListener listener;
        private final EndpointDescription description;
        private final String matchingFilter;

        public EndpointListenerHolder(EndpointListener l,
                    EndpointDescription d, String f) {
              this.listener = l;
              this.description = d;
              this.matchingFilter = f;
        }

        public EndpointListener getListener() {
              return listener;
        }

        public EndpointDescription getDescription() {
              return description;
        }

        public String getMatchingFilter() {
              return matchingFilter;
        }
  }
    /**
     * 动态处理Bundle中的EndpintDescription文件
     */
    class EndpointDescriptionFileTracker implements BundleTrackerCustomizer<Bundle>{
        
        private static final String REMOTESERVICE_MANIFESTHEADER = "Remote-Service";
        
        private static final String XML_FILE_PATTERN = "*.xml";
        private final Map<Long, Collection<EndpointDescription>> bundleDescriptionMap=Collections.synchronizedMap(new HashMap<Long,Collection<EndpointDescription>>());
      
        @Override
        public Bundle addingBundle(Bundle bundle, BundleEvent event) {
            if (ctx != null) {
                String remoteServicesHeaderValue = bundle.getHeaders().get(REMOTESERVICE_MANIFESTHEADER);
                if (remoteServicesHeaderValue != null) {
                    // First parse into comma-separated values
                    String[] paths = remoteServicesHeaderValue.split(","); 
                    if (paths != null)
                        for (int i = 0; i < paths.length; i++)
                            handleEndpointDescriptionPath(bundle, paths[i]);
                }
            }
            return bundle;
        }

        /**
         * 收集本地Bundle中已配置的EndpointDescription,加入到localLocatorServiceListener中
         */
        private void handleEndpointDescriptionPath(Bundle bundle,
            String descPath) {
            if (descPath == null || "".equals(descPath.trim()))
                return;
            Enumeration<URL> e = null;
            if (descPath.endsWith("/")) {
                e = bundle.findEntries(descPath, XML_FILE_PATTERN, false);
            } else {
                int index = descPath.lastIndexOf("/");
                if (index == -1) {
                    e = bundle.findEntries("/", descPath, false);
                } else {
                    String path = descPath.substring(0, index);
                    if ("".equals(path)) {
                        path = "/";
                    }
                    String filePattern = descPath.substring(index + 1);
                    e = bundle.findEntries(path, filePattern, false);
                }
            }
            Collection<EndpointDescription> eds = new ArrayList<EndpointDescription>();
            if (e != null) {
                while (e.hasMoreElements()) {
                    EndpointDescription[] endps = handleEndpointDescriptionFile(
                        bundle, e.nextElement());
                    if (endps != null) {
                        for (EndpointDescription endp : endps) {
                            eds.add(endp);
                        }
                    }
                }
            }
            if (eds.size() >= 1) {
                bundleDescriptionMap.put(new Long(bundle.getBundleId()), eds);
                for (EndpointDescription ed : eds)
                    localLocatorServiceListener.handleEndpointDescription(ed,
                        true);
            }
        }

        /**
         * 读取EndpointDescription
         * @return
         */
        private EndpointDescription[] handleEndpointDescriptionFile(Bundle bundle, URL url) {
            EndpointDescriptionReader reader=  getEndpointDescriptionReader();
            InputStream is=null;
            try {
                if(reader==null){
                    throw new NullPointerException(
                        "No endpointDescriptionReader available for handleEndpointDescriptionFile fileURL="+url);
                }
                 is= url.openStream();
                 return reader.readEndpointDescriptions(is);
            } catch (Exception e) {
                LOG.error("Exception creating endpoint descriptions from fileURL="  + url,e);
            }finally{
                if(is!=null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        LOG.error("Exception close endpoint descriptions from fileURL="  + url,e);
                    }
            }
            return null;
        }

        /**
         * 
         */
        public void close() {
            synchronized (endpointDescriptionReaderTrackerLock) {
                if (endpointDescriptionReaderTracker != null) {
                      endpointDescriptionReaderTracker.close();
                      endpointDescriptionReaderTracker = null;
                }
          }
          bundleDescriptionMap.clear();
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event,
            Bundle object) {
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event,
            Bundle object) {
            Collection<EndpointDescription> endpointDescriptions = bundleDescriptionMap.remove(new Long(
                bundle.getBundleId()));
            if (endpointDescriptions != null) {
                for (EndpointDescription endpointDescription : endpointDescriptions) {
                    localLocatorServiceListener.handleEndpointDescription(
                        endpointDescription, false);
                }

            }
        }
        
    }
   
}
