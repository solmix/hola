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

package org.solmix.hola.rt.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.core.model.DiscoveryInfo;
import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.RemoteManagerProtocol;
import org.solmix.hola.rs.service.GenericService;
import org.solmix.hola.rt.ServiceExportor;
import org.solmix.runtime.Container;



/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月5日
 */

public class ServiceConfig<T> extends AbstractServiceConfig
{

    private static final long serialVersionUID = -7539697586814177467L;

    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfig.class);
    
    private static final Map<String, Integer> RANDOM_PORT_MAP = new HashMap<String, Integer>();

    private String[] interfaceNames;

    protected Integer delay;

    // 服务名称
    private String path;
    
    private ServiceExportor serviceExportor;

    /**
     * 一个服务可以通过多种协议发布
     */
    private List<ServerConfig> servers;

    /**
     * 接口实现类引用
     */
    private T ref;

    private Class<?>[] interfaceClass;

    private List<MethodConfig> methods;
    
    
    private boolean generic;
    /**
     * default instance.
     */
    public ServiceConfig(Container container){
    	super(container);
    }
    
    public synchronized void prepareExport() {
        mergeConfiguration();
        
        if (interfaceNames == null || interfaceNames.length == 0) {
            throw new IllegalStateException("<hola:service interface=\"\" /> interface not allow null!");
        }
        //通用服务
        if(ref instanceof GenericService){
        	 interfaceClass = new Class<?>[]{ GenericService.class};
        	 generic=true;
        	 //TODO
        }else{
            interfaceClass=new Class<?>[interfaceNames.length];
            for(int i=0;i<interfaceNames.length;i++){
                String interfaceName=interfaceNames[i];
	        //check interface config.
	        try {
	            interfaceClass[i] = Class.forName(interfaceName, true, 
	            		Thread.currentThread().getContextClassLoader());
	        } catch (ClassNotFoundException e) {
	            throw new IllegalStateException(e.getMessage(), e);
	        }
	        checkInterfaceAndMethods(interfaceClass[i],methods);
	        checkRef(interfaceClass[i]);
            }
        }
        //TODO sub mock local
        checkApplication();
        checkDiscovery();
        checkServer();
        appendDefault(this);
        //path
        if((path == null || path.length() == 0)&&interfaceNames.length==1){
            path = interfaceNames[0];
        }
    }
    
    private void mergeConfiguration() {
        if(module!=null){
            if(discoveries==null) discoveries=module.getDiscoveries();
            if(monitor==null)     monitor=module.getMonitor();
        }
        if(application!=null){
            if(discoveries==null) discoveries=application.getDiscoveries();
            if(monitor==null)     monitor=application.getMonitor();
        }
       
    }
    protected void checkServer(){
        if(servers==null||servers.size()==0){
            if(getProtocol()==null){
                if(LOG.isInfoEnabled())
                    LOG.info("not set <hola:server .../> neither <hola:service protocol=.../>,set default protocol:hola ");
                setProtocol("hola");
            }
            ServerConfig mock = new ServerConfig(container,getProtocol());
            setServer(mock);
        }
        for(ServerConfig server:servers){
            appendDefault(server);
        }
    }
    
    protected void checkRef(Class<?> type){
    	if (ref == null) {
            throw new IllegalStateException("<hola:service ref=\"\" /> interface not allow null!");
        }
        if (! type.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
    }
	
    public String getInterface() {
        return StringUtils.join(interfaceNames,',');
    }
    public void setInterface(String interfaceName) {
       String[] classes= StringUtils.split(interfaceName, ",");
       setInterfaces(classes);
    }
    public String[] getInterfaces() {
        return interfaceNames;
    }
    public void setInterfaces(String[] interfaceNames) {
        this.interfaceNames = interfaceNames;
        if ((id == null || id.length() == 0)&&interfaceNames.length==0) {
            id = interfaceNames[0];
        }
    }
    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && ! interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = new Class<?>[]{interfaceClass};
        setInterface(interfaceClass == null ? (String) null : interfaceClass.getName());
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }
    @Property(excluded=true)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        checkPathName("path", path);
        this.path = path;
    }
	/**
	 * @return the methods
	 */
	public List<MethodConfig> getMethods() {
		return methods;
	}
	/**
	 * @param methods the methods to set
	 */
	public void setMethods(List<MethodConfig> methods) {
		this.methods = methods;
	}
	
	
    
    /**
     * @return the delay
     */
    public Integer getDelay() {
        return delay;
    }

    
    /**
     * @param delay the delay to set
     */
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    
    /**
     * @return the servers
     */
    public List<ServerConfig> getServers() {
        //如果没有配置server,可以根据protocol,和系统默认值生成一个server
        return servers;
    }

    
    /**
     * @param servers the servers to set
     */
    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }

    public ServerConfig getServer() {
        return servers == null || servers.size() == 0 ? null : servers.get(0);
    }

    public void setServer(ServerConfig server) {
        this.servers = Arrays.asList(new ServerConfig[] {server});
    }

    
    /**
     * @return the serviceExportor
     */
    public ServiceExportor getServiceExportor() {
        return serviceExportor;
    }

    
    /**
     * @param serviceExportor the serviceExportor to set
     */
    public void setServiceExportor(ServiceExportor serviceExportor) {
        this.serviceExportor = serviceExportor;
    }
    /**
     * 根据配置信息生成EndpointInfo.
     * @return
     */
    public List<EndpointInfo> getEndpointInfo(){
      //加载合并整理参数
      prepareExport();
      
      List<EndpointInfo> endpoints = new ArrayList<EndpointInfo>();
      List<DiscoveryInfo> discoveryInfos=getDiscoveryInfos();
      for(ServerConfig server:servers){
          List<EndpointInfo> eds= getEndpointForServer(server,discoveryInfos);
          endpoints.addAll(eds);
      }
      return endpoints;
    }
    
    
    private List<EndpointInfo> getEndpointForServer(ServerConfig server,  List<DiscoveryInfo> discoveryInfos) {
        List<EndpointInfo> endpoints= new ArrayList<EndpointInfo>();
        //protocol
        String protocol=server.getProtocol();
       if(protocol==null){
           server.setProtocol("hola");
       }
       //host
       String host =server.getHost();
       boolean anyhost = false;
       if (NetUtils.isInvalidLocalHost(host)) {
           anyhost = true;
           try {
               host = InetAddress.getLocalHost().getHostAddress();
           } catch (UnknownHostException e) {
               logger.warn(e.getMessage(), e);
           }
           if (NetUtils.isInvalidLocalHost(host)) {
               if (discoveryInfos != null && discoveryInfos.size() > 0) {
                   for (DiscoveryInfo info : discoveryInfos) {
                       try {
                           Socket socket = new Socket();
                           try {
                               SocketAddress addr = new InetSocketAddress(info.getHost(), info.getPort());
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
     //port
       Integer port = server.getPort();
        if (port == null || port <= 0) {
            int defaultPort = container.getExtensionLoader(
                RemoteManagerProtocol.class).getExtension(protocol).getDefaultPort();
            if (defaultPort <= 0) {
                port = getRandomPort(protocol);
                if (port == null || port < 0) {
                    port = NetUtils.getAvailablePort(defaultPort);
                    putRandomPort(protocol, port);
                }
                logger.warn("Use random available port(" + port + ") for protocol :" + protocol);
            } else {
                port = defaultPort;
            }
        }
        //path
        String contextpath= server.getContextpath();
        if(contextpath!=null){
           if(contextpath.endsWith("/")){
               path=contextpath+path;
           }else{
               path=contextpath+"/"+path;
           }
        }
        //properties
        DataTypeMap map = new DataTypeMap();
        if (anyhost) {
            map.put("anyhost", "true");
        }
       map.put("hola", ConfigUtils.getVersion());
       map.put(EndpointInfo.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
       appendProperties(map, application);
       appendProperties(map, module);
       appendProperties(map, server);
       appendProperties(map, this);
       if(methods!=null && methods.size()>0){
           for(MethodConfig method:methods){
               appendProperties(map, method,method.getName());
           }
       }
       if(generic){
           map.put("generic", true);
           map.put("methods", "*");
       }else{
          String rivision= ConfigUtils.getVersion(interfaceClass[0]);
          if(rivision!=null&&rivision.length()>0){
              map.put("rivision", rivision);
          }
       }
     
       if("jvm".equalsIgnoreCase(map.get("protocol").toString())){
           server.setAdvertise(false);
           map.put(EndpointInfo.ADVERTISE_KEY, false);
       }
       RemoteInfo remoteInfo = new RemoteInfo(protocol,host,port,path,map);
       EndpointInfo endpoint=new EndpointInfo(remoteInfo);
       Boolean advertise= endpoint.getAdvertise(true);
        if ((advertise == null || advertise.booleanValue())
            && discoveryInfos != null && discoveryInfos.size() > 0) {
            endpoint.setDiscoveryInfos(discoveryInfos);
        }
        endpoints.add(endpoint);
        return endpoints;
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

