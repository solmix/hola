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
package org.solmix.hola.builder;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.Version;
import org.solmix.commons.timer.StopWatch;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.util.SystemPropertyAction;
import org.solmix.exchange.ProtocolNoFoundException;
import org.solmix.hola.builder.delegate.DelegateRemoteServiceFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.ContainerFactory;




/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年10月28日
 */

public class ServiceDefinition<T> extends AbstractServiceDefinition implements ContainerAware
{
    /**    */
    private static final long serialVersionUID = -6412855348674400197L;
    
    private static final Logger LOG =LoggerFactory.getLogger(ServiceDefinition.class);
    
    private String interfaceName;

  
    /**
     * 一个服务可以通过多种协议发布
     */
    private List<ProviderDefinition> providers;

    /**
     * 接口实现类引用
     */
    private T ref;

    private Class<T> interfaceClass;

    /**
     * 方法配置
     */
    private List<MethodDefinition> methods;
    
    /**
     * <li>上下文路径
     */
    private String path;
    
    
    private Container container;
    
    private boolean registered;
    
    private boolean unregistered;
    
    private final List<RemoteRegistration<T>> registrations = new ArrayList<RemoteRegistration<T>>();
    private static final Map<String, Integer> RANDOM_PORT_MAP = new HashMap<String, Integer>();
    public ServiceDefinition(){
        
    }
    public ServiceDefinition(T t){
        this(ContainerFactory.getThreadDefaultContainer(),t);
    }
    public ServiceDefinition(Container c,T t){
        this.container=c;
        this.ref=t;
    }
    
    public String getPath() {
        return path;
    }

    
    public void setPath(String path) {
        checkPathName("path", path);
        this.path = path;
    }

    
    public String getInterface() {
        return interfaceName;
    }
    public void setInterface( String interfaceName) {
        this.interfaceName = interfaceName;
        if (id == null || id.length() == 0) {
            id = interfaceName;
        }
    }
    public void setInterface(Class<T> interfaceClass) {
        if (interfaceClass != null && ! interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? (String) null : interfaceClass.getName());
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }
   
      /**
       * @return the methods
       */
      public List<MethodDefinition> getMethods() {
            return methods;
      }
      /**
       * @param methods the methods to set
       */
      public void setMethods(List<MethodDefinition> methods) {
            this.methods = methods;
      }
      
    /**
     * @return the servers
     */
    public List<ProviderDefinition> getProviders() {
        //如果没有配置server,可以根据protocol,和系统默认值生成一个server
        return providers;
    }

    
    /**
     * @param servers the servers to set
     */
    public void setProviders(List<ProviderDefinition> servers) {
        this.providers = servers;
    }

    public ProviderDefinition getProvider() {
        return providers == null || providers.size() == 0 ? null : providers.get(0);
    }

    public void setProvider(ProviderDefinition provider) {
        this.providers = Arrays.asList(new ProviderDefinition[] {provider});
    }
    
    @Override
    public void setContainer(Container container) {
        this.container=container;
    }
    
    public Container getContainer() {
        return container;
    }
    
    private void checkProviders() {
        if(getProviders()==null||getProviders().size()==0){
            ProviderDefinition provider = new ProviderDefinition();
            setProvider(provider);
        }
        for(ProviderDefinition pd:getProviders()){
            if(StringUtils.isEmpty(pd.getProtocol())){
                pd.setProtocol("hola");
            }
            appendSystemProperties(pd);
        }
        
    }
    
    private void checkRef() {
        // 检查引用不为空，并且引用必需实现接口
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (! interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
    }

    protected synchronized void doRegister(final ProviderDefinition provider,final StopWatch watch) {
        Boolean publish = isPublish();
        if(publish==null){
            publish = provider.isPublish();
        }
        
        if(publish!=null&&!publish.booleanValue()){
            //不允许发布
            return;
        }
        //延迟发布
        Integer delay = getDelay();
        if(delay==null){
            delay=provider.getDelay();
        }
        if (getPath() == null || getPath().length() == 0) {
            setPath(interfaceName);
        }
        if (delay != null && delay > 0) {
            final int pdelay = delay;
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(pdelay);
                    } catch (Throwable e) {
                    }
                    doExportInteral(provider,watch);
                }
            });
            thread.setDaemon(true);
            thread.setName("DelayRegisterServiceThread");
            thread.start();
        } else {
            doExportInteral(provider,watch);
        }
        
    }
    
    protected List<DiscoveryDefinition> checkDiscovery(ProviderDefinition provider) {
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
    
    protected List<Dictionary<String, ?>> getDiscoveryDictionaries(ProviderDefinition provider) {
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
    
 
    protected synchronized void doExportInteral(ProviderDefinition provider,StopWatch watch) {
        if(StringUtils.isEmpty(interfaceName)){
            throw new IllegalArgumentException("<hola:service interface=\"\"/> interface is null or empty");
        }
        ClassLoaderHolder origLoader = null;
        if(LOG.isDebugEnabled()) {
        	watch.markTimeBegin("checkInterfaceAndMethods");
        }
        try {
            ClassLoader loader = container.getExtension(ClassLoader.class);
            if (loader != null) {
                origLoader = ClassLoaderUtils.setThreadContextClassloader(loader);
            }
            try {
                interfaceClass = (Class<T>) ClassLoaderUtils.loadClass(interfaceName, ServiceDefinition.class);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } finally {
            if (origLoader != null) {
                origLoader.reset();
            }

        }
        checkInterfaceAndMethods(interfaceClass, methods);
        checkRef();
        if(LOG.isDebugEnabled()) {
        	watch.markTimeEnd("checkInterfaceAndMethods");
        }
        Dictionary<String, Object> dic = new Hashtable<String, Object>();
        List<Dictionary<String, ?>> discoveryDics = getDiscoveryDictionaries(provider);
        String protocol = getProtocol();
        if(protocol==null){
            protocol=provider.getProtocol();
        }
        if(LOG.isDebugEnabled()) {
        	watch.markTimeBegin("getPid");
        }
        int pid  = SystemPropertyAction.getPid();
        if(LOG.isDebugEnabled()) {
        	watch.markTimeEnd("getPid");
        }
        if(pid>0){
            dic.put(HOLA.PID_KEY, pid);
        }
        if(LOG.isDebugEnabled()) {
        	watch.markTimeBegin("appendDictionaries");
        }
        ApplicationDefinition app = getApplication();
        if(app==null){
            app=provider.getApplication();
        }
        appendDictionaries(dic,app );
        ModuleDefinition module = getModule();
        if(module==null){
           module = provider.getModule();
        }
        appendDictionaries(dic, module);
        appendDictionaries(dic,provider);
        appendDictionaries(dic,this);
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
                List<ArgumentDefinition> args = method.getArguments();
                if(args!=null&&args.size()>0){
                    for(ArgumentDefinition arg:args){
                        if(arg.getType() != null && arg.getType().length() >0){
                            Method[] methods = interfaceClass.getMethods();
                            if(methods != null && methods.length > 0){
                                for (int i = 0; i < methods.length; i++) {
                                    String methodName = methods[i].getName();
                                    if(methodName.equals(method.getName())){

                                        Class<?>[] argtypes = methods[i].getParameterTypes();
                                        //一个方法中单个callback
                                        if (arg.getIndex() != -1 ){
                                            if (argtypes[arg.getIndex()].getName().equals(arg.getType())){
                                                appendDictionaries(dic, arg, method.getName() + "." + arg.getIndex());
                                            }else {
                                                throw new IllegalArgumentException("argument config error : the index attribute and type attirbute not match :index :"+arg.getIndex() + ", type:" + arg.getType());
                                            }
                                        } else {
                                            //一个方法中多个callback
                                            for (int j = 0 ;j<argtypes.length ;j++) {
                                                Class<?> argclazz = argtypes[j];
                                                if (argclazz.getName().equals(arg.getType())){
                                                    appendDictionaries(dic, arg, method.getName() + "." + j);
                                                    if (arg.getIndex() != -1 && arg.getIndex() != j){
                                                        throw new IllegalArgumentException("argument config error : the index attribute and type attirbute not match :index :"+arg.getIndex() + ", type:" + arg.getType());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else if(arg.getIndex()!=-1){
                            appendDictionaries(dic, arg, method.getName() + "." + arg.getIndex());
                        }else{
                            throw new IllegalArgumentException("argument config must set index or type attribute.eg: <hola:argument index='0' .../> or <hola:argument type=xxx .../>");
                        }
                    }
                }
            }
        }//end methods
        if(LOG.isDebugEnabled()) {
        	watch.markTimeEnd("appendDictionaries");
        }
        String revision = Version.getVersion(interfaceClass, version);
        if(!StringUtils.isEmpty(revision)){
            dic.put(HOLA.VERSION, revision);
        }
        String contextPath = provider.getContextpath();
        String  path = getPath();
        if(!StringUtils.isEmpty(contextPath)){
            if(contextPath.endsWith("/")){
                contextPath=contextPath.substring(0, contextPath.length()-1);
            }
            if(path.startsWith("/")){
                path=path.substring(1, path.length());
            }
            path=contextPath+"/"+path;
        }
        dic.put(HOLA.PATH_KEY, path);
        
        if(LOG.isDebugEnabled()) {
        	watch.markTimeBegin("validAddress");
        }
        //RECHECK
        String host = PropertiesUtils.getString(dic, HOLA.HOST_KEY);
        if (NetUtils.isInvalidLocalHost(host)) {
            host = NetUtils.getLocalHost();
            if (NetUtils.isInvalidLocalHost(host)) {
                if (discoveryDics != null && discoveryDics.size() > 0) {
                    for (Dictionary<String, ?> dis : discoveryDics) {
                        try {
                            Socket socket = new Socket();
                            try {
                                String dhost = PropertiesUtils.getString(dis, HOLA.HOST_KEY);
                                int port = PropertiesUtils.getInt(dis, HOLA.PORT_KEY);
                                SocketAddress addr = new InetSocketAddress(dhost, port);
                                socket.connect(addr, 1000);
                                host = socket.getLocalAddress().getHostAddress();
                                break;
                            } finally {
                                try {
                                    socket.close();
                                } catch (Throwable e) {}
                            }
                        } catch (Exception e) {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                }
                if (NetUtils.isInvalidLocalHost(host)) {
                    host = NetUtils.getLocalHost();
                }
            }
        }//end host
        
        Integer port =PropertiesUtils.getInt(dic, HOLA.PORT_KEY);
        if(port==null||port<0){
            port = getRandomPort(protocol);
            if (port == null || port < 0) {
                port = NetUtils.getAvailablePort();
                putRandomPort(protocol, port);
            }
            logger.warn("Use random available port(" + port + ") for protocol " + protocol);
        }
        if(LOG.isDebugEnabled()) {
        	watch.markTimeEnd("validAddress");
        }
        
        dic.put(HOLA.HOST_KEY, host);
        dic.put(HOLA.PORT_KEY, port);
        dic.put(HOLA.CATEGORY_KEY, HOLA.PROVIDER_CATEGORY);
        dic.put(HOLA.TIMESTAMP_KEY, System.currentTimeMillis());
        if(LOG.isDebugEnabled()) {
        	watch.markTimeBegin("register");
        }
        String scope = PropertiesUtils.getString(dic,HOLA.SCOPE_KEY);
        if(!"none".equalsIgnoreCase(scope)){
            //默认在注册远程服务的时候依旧会注册本地服务,如果已在scope中指定了remote则不本地注册.
            if(!"remote".equalsIgnoreCase(scope)){
                registerLocal(dic);
            }
            //默认都注册为远程服务,如配置指定为local则不注册远程
            if(!"local".equalsIgnoreCase(scope)){
                if (logger.isInfoEnabled()) {
                    logger.info("Register Remote service " + interfaceClass.getName() + " to url " + PropertiesUtils.toAddress(dic));
                }
                if (DataUtils.isNotNullAndEmpty(discoveryDics)) {
                    for (Dictionary<String, ?> dis : discoveryDics) {
                        PropertiesUtils.putIfAbsent(dic, HOLA.DYNAMIC_KEY, dis.get(HOLA.DYNAMIC_KEY));
                        Dictionary<String, ?> monitor = getMonitorDictionary(provider);
                        if (monitor != null) {
                            dic.put(HOLA.MONITOR_KEY, monitor);
                            PropertiesUtils.putIfExitAsArray(dic, HOLA.FILTER_KEY, "monitor");
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("advertise Remote service " + interfaceClass.getName() + " to discovery " + PropertiesUtils.toAddress(dis));
                        }
                        dic.put(HOLA.DISCOVERY_KEY, dis);
                        RemoteServiceFactory factory= container.getExtensionLoader(RemoteServiceFactory.class).getExtension(DelegateRemoteServiceFactory.NAME);
                        if(factory==null){
                            throw new ProtocolNoFoundException("Can't lookup RemoteServiceFactory for protocol:"+DelegateRemoteServiceFactory.NAME);
                        }
                        RemoteRegistration<T> reg= factory.register(interfaceClass, ref, dic);
                        registrations.add(reg);
                    }
                }else{
                    RemoteServiceFactory factory= container.getExtensionLoader(RemoteServiceFactory.class).getExtension(protocol);
                    if(factory==null){
                        throw new IllegalStateException("Can't lookup RemoteServiceFactory for protocol:"+protocol);
                    }
                    RemoteRegistration<T> reg= factory.register(interfaceClass, ref, dic);
                    registrations.add(reg);
                }
            }
        }
        if(LOG.isDebugEnabled()) {
        	watch.markTimeEnd("register");
        	LOG.debug("Register Service Used:"+watch.toString());
        }
        
       
    }
    //在OSGI环境下同时注册服务到本地OSGI中
    protected void registerLocal(Dictionary<String, Object> dic) {
        BundleContext context=    container.getExtension(BundleContext.class);
        if(context!=null){
            context.registerService(interfaceClass, ref, dic);
        }
    }
    
   
    
    public synchronized void register(){
        if(unregistered){
            throw new IllegalArgumentException("Aready Unregistered");
        }
        if(registered){
            return;
        }
        registered=true;
        StopWatch watch = new StopWatch();
        if(LOG.isDebugEnabled()) {
        	watch.markTimeBegin("appendSystemProperties");
        }
        appendSystemProperties(this);
        if(LOG.isDebugEnabled()) {
        	watch.markTimeEnd("appendSystemProperties");
        }
        checkProviders();
        List<ProviderDefinition> providers= getProviders();
        for(ProviderDefinition provider:providers){
            doRegister(provider,watch);
        }
    }
    
    public synchronized void unregister(){
        if(!registered){
            return;
        }
        if(unregistered){
            return;
        }
        
        if (registrations != null && registrations.size() > 0) {
            for (RemoteRegistration<T> reg : registrations) {
                  try {
                    reg.unregister();
                } catch (Throwable t) {
                    logger.warn("unexpected err when unexport" + reg, t);
                }
            }
            registrations.clear();
      }
        unregistered=true;
    }
   
    private static Integer getRandomPort(String protocol) {
        protocol = protocol.toLowerCase();
        if (RANDOM_PORT_MAP.containsKey(protocol)) {
            return RANDOM_PORT_MAP.get(protocol);
        }
        return Integer.MIN_VALUE;
    }

    private static void putRandomPort(String protocol, Integer port) {
        protocol = protocol.toLowerCase();
        if (!RANDOM_PORT_MAP.containsKey(protocol)) {
            RANDOM_PORT_MAP.put(protocol, port);
        }
    }
    
}
