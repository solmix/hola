/**
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

package org.solmix.hola.builder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.Version;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.util.SystemPropertyAction;
import org.solmix.hola.builder.delegate.DelegateRemoteServiceFactory;
import org.solmix.hola.cluster.Cluster;
import org.solmix.hola.cluster.directory.PreparedDirectory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.service.GenericService;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.rs.RemoteProxyFactory;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.ContainerFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月30日
 */

public class ReferenceDefinition<T> extends AbstractReferenceDefinition implements ContainerAware {

    /**    */
    private static final long serialVersionUID = 2328539426745902984L;
    
    private static final Logger LOG  = LoggerFactory.getLogger(ReferenceDefinition.class);

    private String interfaceName;

    private Class<?> interfaceClass;

    private String url;

    private ConsumerDefinition consumer;
    
    private Container container;
    
    /**
     * 方法配置
     */
    private List<MethodDefinition> methods;
    
    private transient volatile T ref;
    private transient volatile boolean initialized;

    private transient volatile boolean destroyed;
    private transient volatile RemoteService<?> remoteService;
    
    public ReferenceDefinition(){
        this(ContainerFactory.getThreadDefaultContainer());
    }
    
    public ReferenceDefinition(Container container){
        this.container=container;
    }
    /**   */
    public ConsumerDefinition getConsumer() {
        return consumer;
    }

    /**   */
    public void setConsumer(ConsumerDefinition client) {
        this.consumer = client;
    }

    @Property(excluded = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (id == null || id.length() == 0) {
            id = interfaceName;
        }
    }

    public String getInterface() {
        return interfaceName;
    }
    
    public List<MethodDefinition> getMethods() {
        return methods;
    }

    
    public void setMethods(List<MethodDefinition> methods) {
        this.methods = methods;
    }

    public void setInterface(Class<T> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class "
                + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? (String) null
            : interfaceClass.getName());
    }

    public Class<?> getInterfaceClass() {
        if (interfaceClass != null) {
            return interfaceClass;
        }
        if (isGeneric() || (getConsumer() != null && getConsumer().isGeneric())) {
            return GenericService.class;
        }
        try {
            if (interfaceName != null && interfaceName.length() > 0) {
                this.interfaceClass =  Class.forName(interfaceName, true,
                    Thread.currentThread().getContextClassLoader());
            }
        } catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return interfaceClass;
    }
    
    private void checkConsumer() {
        if(consumer==null){
            consumer = new ConsumerDefinition();
        }
        appendSystemProperties(consumer);
    }
    
    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<hola:reference interface=\"\" /> interface not allow null!");
        }
        checkConsumer();
        appendSystemProperties(this);
        ClassLoaderHolder origLoader = null;
        try {
            ClassLoader loader = container.getExtension(ClassLoader.class);
            if (loader != null) {
                origLoader = ClassLoaderUtils.setThreadContextClassloader(loader);
            }
            try {
                interfaceClass = ClassLoaderUtils.loadClass(interfaceName, ServiceDefinition.class);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } finally {
            if (origLoader != null) {
                origLoader.reset();
            }

        }
        
        checkInterfaceAndMethods(interfaceClass, methods);
        if (consumer != null) {
            if (application == null) {
                application = consumer.getApplication();
            }
            if (module == null) {
                module = consumer.getModule();
            }
            if (discoveries == null) {
                discoveries = consumer.getDiscoveries();
            }
            if (monitor == null) {
                monitor = consumer.getMonitor();
            }
        }
        if (module != null) {
            if (discoveries == null) {
                discoveries = consumer.getDiscoveries();
            }
            if (monitor == null) {
                monitor = module.getMonitor();
            }
        }
        if (application != null) {
            if (discoveries == null) {
                discoveries = consumer.getDiscoveries();
            }
            if (monitor == null) {
                monitor = application.getMonitor();
            }
        }
        appendSystemProperties(application);
        Dictionary<String, Object> dic  = new Hashtable<String, Object>();
        dic.put(HOLA.TIMESTAMP_KEY, System.currentTimeMillis());
        dic.put(HOLA.CATEGORY_KEY, HOLA.CONSUMER_CATEGORY);
        int pid  = SystemPropertyAction.getPid();
        if(pid>0){
            dic.put(HOLA.PID_KEY, pid);
        }
        String revision = Version.getVersion(interfaceClass, version);
        if(!StringUtils.isEmpty(revision)){
            dic.put(HOLA.VERSION, revision);
        }
        dic.put(HOLA.INTERFACE_KEY, interfaceName);
        appendSystemProperties(application);
        appendDictionaries(dic, application);
        appendSystemProperties(module);
        appendDictionaries(dic, module);
        appendSystemProperties(consumer);
        appendDictionaries(dic, consumer);
        appendDictionaries(dic, this);
        if(methods!=null&&methods.size()>0){
            for(MethodDefinition method:methods){
                appendDictionaries(dic, method, method.getName());
                String retryKey = method.getName() + ".retry";
                if (dic.get(retryKey)!=null) {
                    Object retryValue = dic.remove(retryKey);
                    if ("false".equals(retryValue)) {
                        dic.put(method.getName() + ".retries", "0");
                    }
                }
            }
        }
        ref=createProxy(dic);
    }
    
    protected List<DiscoveryDefinition> checkDiscovery(ConsumerDefinition provider) {
        List<DiscoveryDefinition> discoveries = getDiscoveries();
        if(discoveries==null){
            discoveries= provider.getDiscoveries();
        }
        if(discoveries==null&&application!=null){
            discoveries=application.getDiscoveries();
        }
        if(discoveries!=null){
            for(DiscoveryDefinition def:discoveries){
                appendSystemProperties(def);
            }
        }
        return discoveries;
    }
    protected List<Dictionary<String, ?>> getDiscoveryDictionaries(ConsumerDefinition provider) {
        List<DiscoveryDefinition> discoveries= checkDiscovery( provider);
        if(DataUtils.isNotNullAndEmpty(discoveries)){
            List<Dictionary<String, ?>> dicList = new ArrayList<Dictionary<String, ?>>();
            for(DiscoveryDefinition dis:discoveries){
                String address = dis.getAddress();
                if (address == null || address.length() == 0) {
                    address = HOLA.ANYHOST_VALUE;
                  }
                if(!StringUtils.isEmpty(address)&&!HOLA.NO_AVAILABLE.equalsIgnoreCase(address)){
                    Dictionary<String, Object> dic  = new Hashtable<String, Object>();
                    ApplicationDefinition app = getApplication();
                    if(app==null){
                        app=provider.getApplication();
                    }
                    appendSystemProperties(app);
                    appendDictionaries(dic, app);
                    appendDictionaries(dic, dis);
                    dic.put(HOLA.PATH_KEY, Discovery.class.getName());
                    dic.put(HOLA.TIMESTAMP_KEY, System.currentTimeMillis());
                    int pid  = SystemPropertyAction.getPid();
                    if(pid>0){
                        dic.put(HOLA.PID_KEY, pid);
                    }
                    if(dic.get(HOLA.PROTOCOL_KEY)==null){
                        dic.put(HOLA.PROTOCOL_KEY, "hola");
                    }
                    List<Dictionary<String, ?>> urls= PropertiesUtils.parseURLs(address, dic);
                    for(Dictionary<String, ?> url:urls){
                        dicList.add(url);
                    }
                }
            }
            return dicList;
        }else{
            return null;
        }
      }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private T createProxy(Dictionary<String, Object> dic) {
        Boolean isLocal=null;
        String scope = PropertiesUtils.getString(dic,HOLA.SCOPE_KEY);
        if("local".equalsIgnoreCase(scope)){
            isLocal=true;
        }
     
       final boolean local = isLocal==null?false:isLocal.booleanValue();
       if(local){
           return referLocal(dic);
       }else{
           List<Dictionary<String, ?>> urlinfos = new ArrayList<Dictionary<String,?>>();
           if(!StringUtils.isEmpty(url)){
                urlinfos.addAll(  PropertiesUtils.parseURLs(url, dic));
           }else{
                 urlinfos.add(dic);
           }
           for(Dictionary<String, ?> info :urlinfos){
               Dictionary<String, Object> url =(Dictionary<String, Object>) info;
               String host = PropertiesUtils.getString(url, HOLA.HOST_KEY);
               if (NetUtils.isInvalidLocalHost(host)) {
                   host = NetUtils.getLocalHost();
                   if (NetUtils.isInvalidLocalHost(host)) {
                       if (NetUtils.isInvalidLocalHost(host)) {
                           host = NetUtils.getLocalHost();
                       }
                   }
               }
               url.put(HOLA.HOST_KEY, host);
               //设置默认协议
               if(url.get(HOLA.PROTOCOL_KEY)==null){
                   url.put(HOLA.PROTOCOL_KEY, "hola");
               }
               //没有设置path使用interface
               if(url.get(HOLA.PATH_KEY)==null){
                   url.put(HOLA.PATH_KEY, url.get(HOLA.INTERFACE_KEY));
               }
           }
           List<Dictionary<String, ?>> discoveryDics = getDiscoveryDictionaries(consumer);
           //通过公告集群
           if(DataUtils.isNotNullAndEmpty(discoveryDics)){
                   RemoteServiceFactory factory= container.getExtensionLoader(RemoteServiceFactory.class).getExtension(DelegateRemoteServiceFactory.NAME);
                   if(urlinfos.size()==1&&discoveryDics.size()==1){
                       Dictionary<String, Object> info =(Dictionary<String, Object>) urlinfos.get(0);
                       info.put(HOLA.DISCOVERY_KEY, discoveryDics.get(0));
                       RemoteReference<?> reference= factory.getReference(interfaceClass, info);
                       remoteService= factory.getRemoteService(reference);
                       if(LOG.isInfoEnabled()){
                           LOG.info("Reference service :"+PropertiesUtils.toAddress(info));
                       }
                   }else{
                       List<RemoteService<?>> services = new ArrayList<RemoteService<?>>();
                       String clusterType = getCluster();
                       if(StringUtils.isEmpty(clusterType)){
                           clusterType=consumer.getCluster();
                       }
                       for(Dictionary<String, ?> info:urlinfos){
                           Dictionary<String, Object> url =(Dictionary<String, Object>) info;
                           for(Dictionary<String, ?> dis:discoveryDics){
                               Dictionary<String, ?> monitor = getMonitorDictionary(consumer);
                               if (monitor != null) {
                                   dic.put(HOLA.MONITOR_KEY, monitor);
                                   PropertiesUtils.putIfExitAsArray(dic, HOLA.FILTER_KEY, "monitor");
                               }
                               url.put(HOLA.DISCOVERY_KEY, dis);
                               RemoteReference<?> reference= factory.getReference(interfaceClass, info);
                               RemoteService<?> remoteService= factory.getRemoteService(reference);
                               services.add(remoteService);
                           }
                       }
                       Cluster cluster = container.getExtensionLoader(Cluster.class).getExtension(clusterType);
                       remoteService=cluster.join(new PreparedDirectory(container, services, null));
                   }
           }else{
               if(urlinfos.size()==1){
                   Dictionary<String, ?> info = urlinfos.get(0);
                   String protocol = PropertiesUtils.getString(info, HOLA.PROTOCOL_KEY);
                   RemoteServiceFactory factory=   container.getExtensionLoader(RemoteServiceFactory.class).getExtension(protocol);
                   if(factory==null){
                       throw new IllegalStateException("Can't lookup RemoteServiceFactory for protocol:"+protocol);
                   }
                   RemoteReference<?> reference=factory.getReference(interfaceClass, info);
                   remoteService=factory.getRemoteService(reference);
                   if(LOG.isInfoEnabled()){
                       LOG.info("Reference service :"+PropertiesUtils.toAddress(info));
                   }
               }else{
                   List<RemoteService<?>> services = new ArrayList<RemoteService<?>>();
                   String clusterType = getCluster();
                   if(StringUtils.isEmpty(clusterType)){
                       clusterType=consumer.getCluster();
                   }
                   
                   for(Dictionary<String, ?> info:urlinfos){
                       String protocol = PropertiesUtils.getString(info, HOLA.PROTOCOL_KEY);
                       RemoteServiceFactory factory=   container.getExtensionLoader(RemoteServiceFactory.class).getExtension(protocol);
                       RemoteReference<?> reference=factory.getReference(interfaceClass, info);
                       RemoteService<?> remoteService=factory.getRemoteService(reference);
                       services.add(remoteService);
                   }
                       
                   Cluster cluster = container.getExtensionLoader(Cluster.class).getExtension(clusterType);
                   remoteService=cluster.join(new PreparedDirectory(container, services, null));
               }
           }
          
       }
       if(remoteService!=null){
           return (T) RemoteProxyFactory.getProxy(remoteService,container);
       }
       return null;
    }

    private T referLocal(Dictionary<String, Object> dic) {
        return null;
    }

    public synchronized T refer(){
        if(destroyed){
            throw new IllegalStateException("Already destroyed!");
        }
        if(ref==null){
            init();
        }
        return ref;
        
    }
    

    public synchronized void destroy() {
        if (ref == null) {
            return;
        }
        if (destroyed){
            return;
        }
        destroyed = true;
        try {
            remoteService.destroy();
        } catch (Throwable t) {
            logger.warn("Unexpected err when destroy remoteService(" + url + ").", t);
        }
        remoteService = null;
        ref = null;
    }

    @Override
    public void setContainer(Container container) {
        this.container=container;
    }
}
