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

package org.solmix.hola.discovery.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.collections.ConcurrentHashSet;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.common.model.ServiceType;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月4日
 */
@ThreadSafe
public abstract class AbstractDiscovery implements Discovery
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDiscovery.class);

    // URL地址分隔符，用于文件缓存中，服务提供者URL分隔
    private static final char URL_SEPARATOR = ' ';

    // URL地址分隔正则表达式，用于解析文件缓存中服务提供者URL列表
    private static final String URL_SPLIT = "\\s+";

    protected File file;

    private final Properties properties = new Properties();

    private final boolean syncSaveFile;

    private final AtomicLong lastCacheChanged = new AtomicLong();

    protected final Set<DiscoveryInfo> registered = new ConcurrentHashSet<DiscoveryInfo>();

    private final ConcurrentMap<ServiceType, Set<ServiceTypeListener>> typeListeners = new ConcurrentHashMap<ServiceType, Set<ServiceTypeListener>>();

    protected Dictionary<String, ?> serviceProperties;

    private final ConcurrentMap<ServiceType, Map<String, List<DiscoveryInfo>>> notified = new ConcurrentHashMap<ServiceType, Map<String, List<DiscoveryInfo>>>();

    private final ExecutorService executor = Executors.newFixedThreadPool(1, new NamedThreadFactory("DiscoveryExecutor", true));

    protected Container container;

    protected String address;

    public AbstractDiscovery(Dictionary<String, ?> properties, Container container)
    {
        Assert.isNotNull(container);
        this.container = container;
        this.serviceProperties = properties;
        this.address = PropertiesUtils.toAddress(serviceProperties);
        syncSaveFile = PropertiesUtils.getBoolean(properties, HOLA.DISCOVERY_SYNC_SAVE_FILE, false);
        String cacheFile = PropertiesUtils.getString(properties, HOLA.DISCOVERY_CACHE_FILE);
        if (cacheFile == null) {
            String host = PropertiesUtils.getString(properties, HOLA.HOST_KEY);
            if (System.getProperty("karaf.home") != null) {
                cacheFile = System.getProperty("user.home") + "/data/.hola/discovery-" + host + ".cache";
            } else {
                cacheFile = System.getProperty("user.home") + "/.hola/discovery-" + host + ".cache";
            }
        }
        File file = new File(cacheFile);
        if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IllegalArgumentException(
                    "Invalid registry store file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
            }
        }
        this.file = file;
        loadProperties();
        String backs = PropertiesUtils.getString(properties, HOLA.BACKUP_KEY);
        List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
        infos.add(new DiscoveryInfoImpl(properties));
        
        String[] backups = backs==null?null:HOLA.SPLIT_COMMA_PATTERN.split(backs);
        if(backups!=null&&backups.length>0){
            for(String back:backups){
                if(back.indexOf("://")>0){
                    back=back.substring(back.indexOf("://")+3);
                }
                int i = back.lastIndexOf(':');
                String host;
                int port = -1;
                if (i >= 0) {
                    host = back.substring(0, i);
                    port = Integer.parseInt(back.substring(i + 1));
                } else {
                    host = back;
                }
                Dictionary<String, Object> newInfo = new Hashtable<String, Object>();
                newInfo.put(HOLA.HOST_KEY, host);
                if(port!=-1){
                    newInfo.put(HOLA.PORT_KEY, port);
                }
                
                PropertiesUtils.copyNotExist((Dictionary<String, Object>)properties,newInfo);
                infos.add(new DiscoveryInfoImpl(newInfo));
            }
        }
        notify(infos);
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    protected void notify(List<DiscoveryInfo> urls) {
        if (urls == null || urls.isEmpty())
            return;
        for (Entry<ServiceType, Set<ServiceTypeListener>> entry : getTypeListeners().entrySet()) {
            ServiceType type = entry.getKey();
            if (!isMatch(type, urls.get(0))) {
                continue;
            }
            Set<ServiceTypeListener> listeners = entry.getValue();
            if (listeners != null) {
                for (ServiceTypeListener listener : listeners) {
                    try {
                        notify(type, listener, urls,DiscoveryTypeEvent.REGISTER);
                    } catch (Throwable t) {
                        LOG.error("Failed to notify registry event, urls: " + urls + ", cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    protected void notify(ServiceType type, ServiceTypeListener listener, List<DiscoveryInfo> urls,int etype) {
        if (type == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if ((urls == null || urls.size() == 0) && !HOLA.ANY_VALUE.equals(type.getServiceInterface())) {
            LOG.warn("Ignore empty notify urls for subscribe url " + type);
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Notify service properties for discovery type：" + type + ", urls: " + urls);
        }
        Map<String, List<DiscoveryInfo>> result = new HashMap<String, List<DiscoveryInfo>>();
        for (DiscoveryInfo url : urls) {
            if (isMatch(type, url)) {
                
                String category = PropertiesUtils.getString(url.getServiceProperties(), HOLA.CATEGORY_KEY, HOLA.DEFAULT_CATEGORY);
                List<DiscoveryInfo> list = result.get(category);
                if (list == null) {
                    list = new ArrayList<DiscoveryInfo>();
                    result.put(category, list);
                }
                list.add(url);
            }
        }
        if (result.size() == 0) {
            return;
        }
        Map<String, List<DiscoveryInfo>> categoryNotified = notified.get(type);
        if (categoryNotified == null) {
            notified.putIfAbsent(type, new ConcurrentHashMap<String, List<DiscoveryInfo>>());
            categoryNotified = notified.get(type);
        }
        for (Map.Entry<String, List<DiscoveryInfo>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<DiscoveryInfo> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
            saveProperties(type);
            listener.handle(new DiscoveryTypeEvent(this, type, categoryList,etype));
        }
    }

    private void saveProperties(ServiceType type) {
        if (file == null) {
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            Map<String, List<DiscoveryInfo>> categoryNotified = notified.get(type);
            if (categoryNotified != null) {
                for (List<DiscoveryInfo> us : categoryNotified.values()) {
                    for (DiscoveryInfo u : us) {
                        if (buf.length() > 0) {
                            buf.append(URL_SEPARATOR);
                        }
                        buf.append(PropertiesUtils.toAddress(u.getServiceProperties()));
                    }
                }
            }
            properties.setProperty(type.getIdentityName(), buf.toString());
            long version = lastCacheChanged.incrementAndGet();
            if (syncSaveFile) {
                doSaveProperties(version);
            } else {
                executor.execute(new SaveProperties(version));
            }
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }

    public void doSaveProperties(long version) {
        if (version < lastCacheChanged.get()) {
            return;
        }
        if (file == null) {
            return;
        }
        Properties newProperties = new Properties();
        // 保存之前先读取一遍，防止多个注册中心之间冲突
        InputStream in = null;
        try {
            if (file.exists()) {
                in = new FileInputStream(file);
                newProperties.load(in);
            }
        } catch (Throwable e) {
            LOG.warn("Failed to load registry store file, cause: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.warn(e.getMessage(), e);
                }
            }
        }
        // 保存
        try {
            newProperties.putAll(properties);
            File lockfile = new File(file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                lockfile.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
            try {
                FileChannel channel = raf.getChannel();
                try {
                    FileLock lock = channel.tryLock();
                    if (lock == null) {
                        throw new IOException("Can not lock the registry cache file " + file.getAbsolutePath()
                            + ", ignore and retry later, maybe multi java process use the file, please config: hola.registry.file=xxx.properties");
                    }
                    // 保存
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream outputFile = new FileOutputStream(file);
                        try {
                            newProperties.store(outputFile, "Hola Discovery Cache");
                        } finally {
                            outputFile.close();
                        }
                    } finally {
                        lock.release();
                    }
                } finally {
                    channel.close();
                }
            } finally {
                raf.close();
            }
        } catch (Throwable e) {
            if (version < lastCacheChanged.get()) {
                return;
            } else {
                executor.execute(new SaveProperties(lastCacheChanged.incrementAndGet()));
            }
            LOG.warn("Failed to save registry store file, cause: " + e.getMessage(), e);
        }
    }

    private class SaveProperties implements Runnable
    {

        private long version;

        private SaveProperties(long version)
        {
            this.version = version;
        }

        @Override
        public void run() {
            doSaveProperties(version);
        }
    }

    public AtomicLong getLastCacheChanged() {
        return lastCacheChanged;
    }

    protected boolean isMatch(ServiceType type, DiscoveryInfo info) {
        String is = type.getServiceInterface();
        Dictionary<String, ?> properties= info.getServiceProperties();
        String iis = PropertiesUtils.getServiceInterface(properties);
        if (!(HOLA.ANY_VALUE.equals(is) || StringUtils.isEquals(is, iis))) {
            return false;
        }
        String ic = type.getCategory();
        String iic = PropertiesUtils.getString(properties, HOLA.CATEGORY_KEY, HOLA.DEFAULT_CATEGORY);
        if (!isMatchCategory(ic, iic)) {
            return false;
        }
        String ig = type.getGroup();
        String iig = PropertiesUtils.getString(properties, HOLA.GROUP_KEY);
        return HOLA.ANY_VALUE.equals(ig) || StringUtils.isEquals(ig, iig) || StringUtils.isContains(iig, ig);

    }

    protected boolean isMatchCategory(String ic, String iic) {
        if (DataUtils.isEmpty(ic)) {
            return HOLA.DEFAULT_CATEGORY.equals(iic);
        } else if (ic.contains(HOLA.ANY_VALUE)) {
            return true;
        } else {
            return ic.contains(iic);
        }
    }

    public Map<ServiceType, Set<ServiceTypeListener>> getTypeListeners() {
        return typeListeners;
    }

    private void loadProperties() {
        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                properties.load(in);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Load registry store file " + file + ", data: " + properties);
                }
            } catch (Throwable e) {
                LOG.warn("Failed to load registry store file " + file, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public void register(DiscoveryInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Register: " + info);
        }
        registered.add(info);
    }
    
    @Override
    public void unregister(DiscoveryInfo serviceInfo){
        Assert.assertNotNull(serviceInfo);
        if (LOG.isInfoEnabled()){
            LOG.info("Unregister: " + serviceInfo);
        }
        registered.remove(serviceInfo);
    }


    @Override
    public void unregisterAll() {
        throw new java.lang.UnsupportedOperationException();

    }
    @Override
    public void addTypeListener(final ServiceType type, final ServiceTypeListener listener) {
        Assert.isNotNull(type);
        Assert.isNotNull(listener);
        if (LOG.isInfoEnabled()) {
            LOG.info("Add Type Listener: " + type);
        }
        Set<ServiceTypeListener> listeners = typeListeners.get(type);
        if (listeners == null) {
            typeListeners.putIfAbsent(type, new ConcurrentHashSet<ServiceTypeListener>());
            listeners = typeListeners.get(type);
        }
        listeners.add(listener);
    }

    @Override
    public void removeTypeListener(ServiceType type, ServiceTypeListener listener) {
        Assert.isNotNull(type);
        Assert.isNotNull(listener);
        if (LOG.isInfoEnabled()) {
            LOG.info("Remove Type Listener: " + type);
        }
        Set<ServiceTypeListener> listeners = typeListeners.get(type);
        if(listeners!=null){
            listeners.remove(listeners);
        }
    }

    @Override
    public Future<ServiceType[]> getAsyncServiceTypes() {
        return executor.submit(new Callable<ServiceType[]>() {

            @Override
            public ServiceType[] call() throws Exception {
                return getServiceTypes();
            }

        });
    }

    @Override
    public Future<DiscoveryInfo[]> getAsyncServices() {
        return executor.submit(new Callable<DiscoveryInfo[]>() {

            @Override
            public DiscoveryInfo[] call() throws Exception {
                return getServices();
            }

        });
    }

    @Override
    public Future<DiscoveryInfo> getAsyncService(final ServiceID serviceID) {
        return executor.submit(new Callable<DiscoveryInfo>() {

            @Override
            public DiscoveryInfo call() throws Exception {
                return getService(serviceID);
            }
        });
    }

    @Override
    public Future<DiscoveryInfo[]> getAsyncServices(final ServiceType type) {
        return executor.submit(new Callable<DiscoveryInfo[]>() {

            @Override
            public DiscoveryInfo[] call() throws Exception {
                return getServices(type);
            }
        });
    }

    public void destroy() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Destroy registry:" + PropertiesUtils.toAddress(serviceProperties));
        }
        Set<DiscoveryInfo> destroyRegistered = new HashSet<DiscoveryInfo>(getRegistered());
        if (!destroyRegistered.isEmpty()) {
            for (DiscoveryInfo di : destroyRegistered) {
                boolean dynamic = PropertiesUtils.getBoolean(di.getServiceProperties(), HOLA.DYNAMIC_KEY, true);
                if (dynamic) {
                    try {
                        unregister(di);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Destroy unregister service: " + di);
                        }
                    } catch (Throwable t) {
                        LOG.warn("Failed to unregister service " + di + 
                            " destroy on " + getDiscoveryAddress() + 
                            " cause: " + t.getMessage(),t);
                    }
                }
            }
        }
        Map<ServiceType, Set<ServiceTypeListener>> typeListeners = new HashMap<ServiceType, Set<ServiceTypeListener>>(getTypeListeners());
        if(!typeListeners.isEmpty()){
            for(Map.Entry<ServiceType, Set<ServiceTypeListener>> entry:typeListeners.entrySet()){
                ServiceType type = entry.getKey();
                for(ServiceTypeListener listener:entry.getValue()){
                    try {
                        removeTypeListener(type, listener);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Remove type listener for: " + type);
                        }
                    } catch (Throwable t) {
                        LOG.warn("Failed Remove type listener for:" + type + 
                            " on" + getDiscoveryAddress()+ " on destroy, cause: " +t.getMessage(), t);
                    }
                }
            }
        }
    }
    /**本地文件存储的*/
    public List<DiscoveryInfo> getCachedDiscoveryInfos(ServiceType type) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key != null && key.length() > 0 && key.equals(type.getIdentityName())
                    && (Character.isLetter(key.charAt(0)) || key.charAt(0) == '_')
                    && value != null && value.length() > 0) {
                String[] arr = value.trim().split(URL_SPLIT);
                List<DiscoveryInfo> urls = new ArrayList<DiscoveryInfo>();
                for (String u : arr) {
                    urls.add(new DiscoveryInfoImpl(PropertiesUtils.toProperties(u)));
                }
                return urls;
            }
        }
        return null;
    }

    public String getDiscoveryAddress() {
        return address;
    }
    
    @Override
    public String toString(){
        return getDiscoveryAddress();
    }

    public Set<DiscoveryInfo> getRegistered() {
        return registered;
    }
}
