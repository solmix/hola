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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.model.EndpointInfo;
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
    private String interfaceName;

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

    private Class<?> interfaceClass;

    private List<MethodConfig> methods;
    /**
     * default instance.
     */
    public ServiceConfig(Container container){
    	setContainer(container);
    }
    
    public synchronized void prepareExport() {
        mergeConfiguration();
        //通用服务
        if(ref instanceof GenericService){
        	 interfaceClass = GenericService.class;
        	 //TODO
        }else{
	        if (interfaceName == null || interfaceName.length() == 0) {
	            throw new IllegalStateException("<hola:service interface=\"\" /> interface not allow null!");
	        }
	        //check interface config.
	        try {
	            interfaceClass = Class.forName(interfaceName, true, 
	            		Thread.currentThread().getContextClassLoader());
	        } catch (ClassNotFoundException e) {
	            throw new IllegalStateException(e.getMessage(), e);
	        }
	        checkInterfaceAndMethods(interfaceClass,methods);
	        checkRef();
        }
        //TODO sub mock local
        checkApplication();
        checkDiscovery();
        checkServer();
        //path
        if(path == null || path.length() == 0){
            path = interfaceName;
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
            setServer(new ServerConfig(getProtocol()));
        }
    }
    
    protected void checkRef(){
    	if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (! interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
    }
	
    public String getInterface() {
        return interfaceName;
    }
    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (id == null || id.length() == 0) {
            id = interfaceName;
        }
    }
    public void setInterface(Class<?> interfaceClass) {
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
      //加载合并整理参数,准备
      prepareExport();
      Map<String,Object> prop= new HashMap<String,Object>();
      return null;
        
    }

}

