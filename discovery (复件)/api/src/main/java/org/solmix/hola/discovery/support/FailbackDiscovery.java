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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.ConcurrentHashSet;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.identity.DiscoveryType;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年9月16日
 */

public abstract class FailbackDiscovery extends AbstractDiscovery implements Discovery
{

    private static final Logger LOG = LoggerFactory.getLogger(FailbackDiscovery.class);

    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1,
        new NamedThreadFactory("DiscoveryFailedRetryTimer", true));

    private final Set<DiscoveryInfo> failedRegistered = new CopyOnWriteArraySet<DiscoveryInfo>();

    private final Set<DiscoveryInfo> failedUnregistered = new CopyOnWriteArraySet<DiscoveryInfo>();

    private final ConcurrentMap<DiscoveryType, Set<DiscoveryTypeListener>> failedAddTypeListener = new ConcurrentHashMap<DiscoveryType, Set<DiscoveryTypeListener>>(4);

    private final ConcurrentMap<DiscoveryType, Set<DiscoveryTypeListener>> failedRemoveTypeListener = new ConcurrentHashMap<DiscoveryType, Set<DiscoveryTypeListener>>(4);
    private final ConcurrentMap<DiscoveryType, Map<DiscoveryTypeListener, List<DiscoveryInfo>>> failedNotified = new ConcurrentHashMap<DiscoveryType, Map<DiscoveryTypeListener, List<DiscoveryInfo>>>();

    private final ScheduledFuture<?> retryFuture;

    public FailbackDiscovery(Dictionary<String, ?> properties, Container container)
    {
        super(properties, container);
        int retryPeriod = PropertiesUtils.getInt(properties, HOLA.RETRY_PERIOD_KEY, HOLA.DEFAULT_RETRY_PERIOD);
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
    public Future<?> getRetryFuture() {
        return retryFuture;
    }
    

    
    public Set<DiscoveryInfo> getFailedRegistered() {
        return failedRegistered;
    }
    
    public Set<DiscoveryInfo> getFailedUnregistered() {
        return failedUnregistered;
    }
    
    public ConcurrentMap<DiscoveryType, Set<DiscoveryTypeListener>> getFailedAddTypeListener() {
        return failedAddTypeListener;
    }
    
    public ConcurrentMap<DiscoveryType, Set<DiscoveryTypeListener>> getFailedRemoveTypeListener() {
        return failedRemoveTypeListener;
    }
    
    private void addFailedTypeListener(DiscoveryType type, DiscoveryTypeListener listener) {
        Set<DiscoveryTypeListener> listeners = failedAddTypeListener.get(type);
        if (listeners == null) {
            failedAddTypeListener.putIfAbsent(type, new ConcurrentHashSet<DiscoveryTypeListener>());
            listeners = failedAddTypeListener.get(type);
        }
        listeners.add(listener);
    }
    
    private void removeFailedTypeListener(DiscoveryType type, DiscoveryTypeListener listener) {
        Set<DiscoveryTypeListener> listeners = failedAddTypeListener.get(type);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedRemoveTypeListener.get(type);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<DiscoveryTypeListener, List<DiscoveryInfo>> notified = failedNotified.get(type);
        if (notified != null) {
            notified.remove(listener);
        }
    }
    @Override
    public void register(DiscoveryInfo serviceInfo) {
        super.register(serviceInfo);
        failedRegistered.remove(serviceInfo);
        failedUnregistered.remove(serviceInfo);
        try {
            doRegister(serviceInfo);
        } catch (Exception e) {
            if (PropertiesUtils.getBoolean(serviceProperties, HOLA.CHECK_KEY, true)
                &&PropertiesUtils.getBoolean(serviceInfo.getServiceProperties(), HOLA.CHECK_KEY, true)
                &&!HOLA.CONSUMER_CATEGORY.equals(PropertiesUtils.getBoolean(serviceProperties, HOLA.PROTOCOL_KEY))) {
                throw new IllegalStateException("Failed to register server " + serviceInfo, e);
            } else {
                LOG.error("Failed to register server " + serviceInfo + " wait retry", e);
            }
            failedRegistered.add(serviceInfo);
        }
    }

    @Override
    public void unregister(DiscoveryInfo serviceInfo) {
       super.unregister(serviceInfo);
        failedRegistered.remove(serviceInfo);
        failedUnregistered.remove(serviceInfo);
        try {
            doUnregister(serviceInfo);
        } catch (Exception e) {
            if (PropertiesUtils.getBoolean(serviceProperties, HOLA.CHECK_KEY, true)
                &&PropertiesUtils.getBoolean(serviceInfo.getServiceProperties(), HOLA.CHECK_KEY, true)
                &&!HOLA.CONSUMER_CATEGORY.equals(PropertiesUtils.getBoolean(serviceProperties, HOLA.PROTOCOL_KEY))) {
                throw new IllegalStateException("Failed to unregister server " + serviceInfo, e);
            } else {
                LOG.error("Failed to register server " + serviceInfo + " wait retry", e);
            }
            // 将失败的取消注册请求记录到失败列表，定时重试
            failedUnregistered.add(serviceInfo);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            retryFuture.cancel(true);
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }
    
    @Override
    public void addTypeListener(final DiscoveryType type, final DiscoveryTypeListener listener) {
        super.addTypeListener(type, listener);
        removeFailedTypeListener(type, listener);
        try {
            // 向服务器端发送订阅请求，通过订阅来返回信息处理回调。
            doSubscribe(type, listener);
        } catch (Exception e) {
            Throwable t = e;
            List<DiscoveryInfo> infos = getCachedDiscoveryInfos(type);
            if(infos!=null&&infos.size()>0){
                notify(type,listener,infos);
                LOG.error("Failed to notify " + type + ", Using cached list: " + infos + " from cache file: " +file.getAbsolutePath() + ", cause: " + t.getMessage(), t);
            }else{
                if (PropertiesUtils.getBoolean(serviceProperties, HOLA.CHECK_KEY, true)) {
                    throw new IllegalStateException("Failed to add listener for " + type, e);
                } else {
                    LOG.error("Failed to add listener for" + type + " wait retry", e);
                }
            }
            addFailedTypeListener(type, listener);
        }
    }

    @Override
    public void removeTypeListener(DiscoveryType type, DiscoveryTypeListener listener) {
        super.removeTypeListener(type, listener);
        removeFailedTypeListener(type, listener);
        try {
            // 向服务器端发送取消订阅请求
            doUnsubscribe(type, listener);
        } catch (Exception e) {
            if (PropertiesUtils.getBoolean(serviceProperties, HOLA.CHECK_KEY, true)) {
                throw new IllegalStateException("Failed to remove listener for " + type, e);
            } else {
                LOG.error("Failed to remove listener for" + type + " wait retry", e);
            }
            Set<DiscoveryTypeListener> listeners = failedRemoveTypeListener.get(type);
            if (listeners == null) {
                failedRemoveTypeListener.putIfAbsent(type, new ConcurrentHashSet<DiscoveryTypeListener>());
                listeners = failedRemoveTypeListener.get(type);
            }
            listeners.add(listener);
        }
        
    }
    protected void recover() throws Exception {
        Set<DiscoveryInfo> recoverRegistered = new HashSet<DiscoveryInfo>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Recover register info " + recoverRegistered);
            }
            for (DiscoveryInfo url : recoverRegistered) {
                failedRegistered.add(url);
            }
        }
        Map<DiscoveryType, Set<DiscoveryTypeListener>>  recoverListeners= new HashMap<DiscoveryType, Set<DiscoveryTypeListener>>(getTypeListeners());
        if(!recoverListeners.isEmpty()){
            if (LOG.isInfoEnabled()) {
                LOG.info("Recover add listener " + recoverRegistered);
            }
            for (Map.Entry<DiscoveryType, Set<DiscoveryTypeListener>> entry : recoverListeners.entrySet()) {
                DiscoveryType url = entry.getKey();
                for (DiscoveryTypeListener listener : entry.getValue()) {
                    addFailedTypeListener(url, listener);
                }
            }
        }
        
    }
    @Override
    protected void notify(DiscoveryType type, DiscoveryTypeListener listener, List<DiscoveryInfo> urls) {
        try {
            doNotify(type, listener, urls);
        } catch (Exception t) {
            // 将失败的通知请求记录到失败列表，定时重试
            Map<DiscoveryTypeListener, List<DiscoveryInfo>> listeners = failedNotified.get(type);
            if (listeners == null) {
                failedNotified.putIfAbsent(type, new ConcurrentHashMap<DiscoveryTypeListener, List<DiscoveryInfo>>());
                listeners = failedNotified.get(type);
            }
            listeners.put(listener, urls);
            LOG.error("Failed to notify for subscribe " + type + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }
    
    protected void doNotify(DiscoveryType type, DiscoveryTypeListener listener, List<DiscoveryInfo> infos) {
        super.notify(type, listener, infos);
    }
    /**
     * 
     */
    protected void retry() {
        if (!failedRegistered.isEmpty()) {
            Set<DiscoveryInfo> failed = new HashSet<DiscoveryInfo>(failedRegistered);
            if (failed.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry register " + failed);
                }
                try {
                    for (DiscoveryInfo meta : failed) {
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
        if (!failedUnregistered.isEmpty()) {
            Set<DiscoveryInfo> failed = new HashSet<DiscoveryInfo>(failedUnregistered);
            if (failed.size() > 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry unregister " + failed);
                }
                try {
                    for (DiscoveryInfo meta : failed) {
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
        
        if(!failedAddTypeListener.isEmpty()){
            Map<DiscoveryType, Set<DiscoveryTypeListener>> failed = new HashMap<DiscoveryType, Set<DiscoveryTypeListener>>(failedAddTypeListener);
            for(Map.Entry<DiscoveryType, Set<DiscoveryTypeListener>> entry: failed.entrySet()){
                if(entry.getValue()==null||entry.getValue().size()==0){
                    failed.remove(entry.getKey());
                }
            }
            if(failed.size()>0){
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry add listener " + failed);
                }
                try {
                    for (Map.Entry<DiscoveryType, Set<DiscoveryTypeListener>> entry : failed.entrySet()) {
                        DiscoveryType type = entry.getKey();
                        Set<DiscoveryTypeListener> listeners = entry.getValue();
                        for (DiscoveryTypeListener listener : listeners) {
                            try {
                                doSubscribe(type, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                LOG.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    LOG.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        
        if(!failedRemoveTypeListener.isEmpty()){
            Map<DiscoveryType, Set<DiscoveryTypeListener>> failed = new HashMap<DiscoveryType, Set<DiscoveryTypeListener>>(failedRemoveTypeListener);
            for(Map.Entry<DiscoveryType, Set<DiscoveryTypeListener>> entry: failed.entrySet()){
                if(entry.getValue()==null||entry.getValue().size()==0){
                    failed.remove(entry.getKey());
                }
            }
            if(failed.size()>0){
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry remove listener " + failed);
                }
                try {
                    for (Map.Entry<DiscoveryType, Set<DiscoveryTypeListener>> entry : failed.entrySet()) {
                        DiscoveryType type = entry.getKey();
                        Set<DiscoveryTypeListener> listeners = entry.getValue();
                        for (DiscoveryTypeListener listener : listeners) {
                            try {
                                doUnsubscribe(type, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                LOG.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    LOG.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        
        if(!failedNotified.isEmpty()){
            Map<DiscoveryType, Map<DiscoveryTypeListener, List<DiscoveryInfo>>> failed = new HashMap<DiscoveryType, Map<DiscoveryTypeListener,List<DiscoveryInfo>>>();
            for(Map.Entry<DiscoveryType, Map<DiscoveryTypeListener, List<DiscoveryInfo>>> entry: failed.entrySet()){
                if(entry.getValue()==null||entry.getValue().size()==0){
                    failed.remove(entry.getKey());
                }
            }
            if(failed.size()>0){
                if (LOG.isInfoEnabled()) {
                    LOG.info("Retry notify listener " + failed);
                }
                try {
                    for (DiscoveryType type : failed.keySet()) {
                        Map<DiscoveryTypeListener, List<DiscoveryInfo>> value = failed.get(type);
                        for(Map.Entry<DiscoveryTypeListener, List<DiscoveryInfo>> entry:value.entrySet()){
                            try {
                                DiscoveryTypeListener listener = entry.getKey();
                                List<DiscoveryInfo> urls = entry.getValue();
                                listener.handle(new DiscoveryTypeEvent(this, type, urls));
                                value.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                LOG.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    LOG.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
    }
    protected abstract void doRegister(DiscoveryInfo meta);

    protected abstract void doUnregister(DiscoveryInfo meta);

    protected abstract void doSubscribe(DiscoveryType type, DiscoveryTypeListener listener);

    protected abstract void doUnsubscribe(DiscoveryType type, DiscoveryTypeListener listener);
}
