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
package org.solmix.hola.discovery.support;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.common.config.DiscoveryInfo;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.ServiceInfo;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月16日
 */

public abstract class FailbackDiscovery extends AbstractDiscovery implements
    Discovery
{
    private static final Logger LOG=LoggerFactory.getLogger(FailbackDiscovery.class);

    public static final int RETRY_PERIOD=5000;
    
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DiscoveryFailedRetryTimer", true));
   
    private final Set<ServiceInfo> failedRegistered = new CopyOnWriteArraySet<ServiceInfo>();

    private final Set<ServiceInfo> failedUnregistered = new CopyOnWriteArraySet<ServiceInfo>();
    private final Set<ServiceInfo> registered = new CopyOnWriteArraySet<ServiceInfo>();
    /*private final ConcurrentMap<ServiceListener, ServiceType> failedAddListener = new ConcurrentHashMap<ServiceListener, ServiceType>(4);
   
    private final ConcurrentMap<ServiceListener, ServiceType> failedRemoveListener = new ConcurrentHashMap<ServiceListener, ServiceType>(4);

    private final Set<ServiceTypeListener> failedAddTypeListener = new CopyOnWriteArraySet<ServiceTypeListener>();
   
    private final Set<ServiceTypeListener> failedRemoveTypeListener = new CopyOnWriteArraySet<ServiceTypeListener>();

*/
    private final ScheduledFuture<?> retryFuture;
    
    private final DiscoveryInfo info;
   
    public FailbackDiscovery(String discoveryNamespace, Container container,DiscoveryInfo info)
    {
        super(discoveryNamespace, container);
        this.info=info;
        int retryPeriod=info.getRetyPeriod(RETRY_PERIOD);
        this.retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    retry();
                } catch (Throwable t) { 
                    LOG.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
                }
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }

  
    @Override
    public void register(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            throw new IllegalArgumentException("register serviceMetadata is null");
        }
        if (LOG.isInfoEnabled()){
            LOG.info("Register: " + serviceInfo);
        }
        registered.add(serviceInfo);
        failedRegistered.remove(serviceInfo);
        failedUnregistered.remove(serviceInfo);
        try{
            doRegister(serviceInfo);
        }catch(Exception e){
           if( info.getCheck(true)){
               throw new IllegalStateException("Failed to register server "+serviceInfo,e);
           }else{
               LOG.error("Failed to register server "+serviceInfo+" wait retry",e);
           }
           failedRegistered.add(serviceInfo);
        }
    }

   /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryAdvertiser#unregister(org.solmix.hola.discovery.ServiceInfo)
     */
    @Override
    public void unregister(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (LOG.isInfoEnabled()){
            LOG.info("Unregister: " + serviceInfo);
        }
        registered.remove(serviceInfo);
        registered.add(serviceInfo);
        failedRegistered.remove(serviceInfo);
        failedUnregistered.remove(serviceInfo);
        try{
            doUnregister(serviceInfo);
        }catch(Exception e){
           if( info.getCheck(true)){
               throw new IllegalStateException("Failed to unregister server "+serviceInfo,e);
           }else{
               LOG.error("Failed to register server "+serviceInfo+" wait retry",e);
           }
           failedUnregistered.add(serviceInfo);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#purgeCache()
     */
    @Override
    public ServiceInfo[] purgeCache() {
        return new ServiceInfo[] {};
       
    }
    @Override
    public void close() throws IOException{
        super.close();
        try {
            retryFuture.cancel(true);
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }
    protected void recover() throws Exception {
        Set<ServiceInfo> recoverRegistered = new HashSet<ServiceInfo>(registered);
        if (! recoverRegistered.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Recover register url " + recoverRegistered);
            }
            for (ServiceInfo url : recoverRegistered) {
                failedRegistered.add(url);
            }
        }
    }
    /**
     * 
     */
    protected void retry() {
        if (! failedRegistered.isEmpty()) {
            Set<ServiceInfo> failed = new HashSet<ServiceInfo>(failedRegistered);
            if (failed.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry register " + failed);
                }
                try {
                    for (ServiceInfo meta : failed) {
                        try {
                            doRegister(meta);
                            failedRegistered.remove(meta);
                        } catch (Throwable t) { 
                            LOG.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { 
                    LOG.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if(! failedUnregistered.isEmpty()) {
            Set<ServiceInfo> failed = new HashSet<ServiceInfo>(failedUnregistered);
            if (failed.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry unregister " + failed);
                }
                try {
                    for (ServiceInfo meta : failed) {
                        try {
                            doUnregister(meta);
                            failedUnregistered.remove(meta);
                        } catch (Throwable t) {
                            LOG.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { 
                    LOG.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        /*if(!failedAddListener.isEmpty()){
            Map<ServiceListener, ServiceType> listeners= new HashMap<>(failedAddListener);
            if (listeners.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry add servicelistener " + listeners);
                }
                try {
                    for (ServiceListener listener : listeners.keySet()) {
                        ServiceType type= listeners.get(listener);
                        try {
                            if(type==null){
                                doAddServiceListener(listener);
                            }else{
                                doAddServiceListener(type,listener);
                            }
                            failedAddListener.remove(listener);
                        } catch (Throwable t) {
                            LOG.warn("Failed to retry add servicelistener  " + listener 
                                + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { 
                    LOG.warn("Failed to retry add servicelistener  " + listeners 
                        + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if(!failedRemoveListener.isEmpty()){
            Map<ServiceListener, ServiceType> listeners= new HashMap<>(failedRemoveListener);
            if (listeners.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry remove servicelistener " + listeners);
                }
                try {
                    for (ServiceListener listener : listeners.keySet()) {
                        ServiceType type= listeners.get(listener);
                        try {
                            if(type==null){
                                doRemoveServiceListener(listener);
                            }else{
                                doRemoveServiceListener(type,listener);
                            }
                            failedRemoveListener.remove(listener);
                        } catch (Throwable t) {
                            LOG.warn("Failed to retry remove servicelistener  " + listener 
                                + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { 
                    LOG.warn("Failed to retry remove servicelistener  " + listeners 
                        + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if(!failedAddTypeListener.isEmpty()){
            Set<ServiceTypeListener> failed = new HashSet<ServiceTypeListener>(failedAddTypeListener);
            if (failed.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry add typelistener " + failed);
                }
                try {
                    for (ServiceTypeListener meta : failed) {
                        try {
                            doAddTypeListener(meta);
                            failedUnregistered.remove(meta);
                        } catch (Throwable t) {
                            LOG.warn("Failed to retry add typelistener  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { 
                    LOG.warn("Failed to retry add typelistener  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if(!failedRemoveTypeListener.isEmpty()){
            Set<ServiceTypeListener> failed = new HashSet<ServiceTypeListener>(failedRemoveTypeListener);
            if (failed.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry unregister " + failed);
                }
                try {
                    for (ServiceTypeListener meta : failed) {
                        try {
                            doRemoveTypeListener(meta);
                            failedUnregistered.remove(meta);
                        } catch (Throwable t) {
                            LOG.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { 
                    LOG.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }*/
    }
 public DiscoveryInfo   getInfo(){
        return info;
    }
    /**
     * @param meta
     */
    protected abstract void doRegister(ServiceInfo meta);
    protected abstract void doUnregister(ServiceInfo meta);
//    protected abstract void doAddServiceListener(ServiceListener listener);
//    protected abstract void doAddServiceListener(ServiceType type,ServiceListener listener);
//    protected abstract void doRemoveServiceListener(ServiceListener listener);
//    protected abstract void doRemoveServiceListener(ServiceType type,ServiceListener listener);
//    protected abstract void doRemoveTypeListener(ServiceTypeListener listener);
//    protected abstract void doAddTypeListener(ServiceTypeListener listener);
    
}
