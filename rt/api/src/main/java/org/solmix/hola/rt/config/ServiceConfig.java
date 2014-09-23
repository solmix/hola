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

import java.util.List;

import org.solmix.hola.rs.service.GenericService;
import org.solmix.runtime.Container;



/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月5日
 */

public class ServiceConfig<T> extends AbstractServiceConfig
{

    private static final long serialVersionUID = -7539697586814177467L;

    private String interfaceName;

    /**
     * 服务版本
     */
    protected String version;

    /**
     *  服务分组
     */
    protected String group;

    protected Integer delay;

    protected Boolean register;

    protected Integer weight;

    protected String document;

    protected Boolean dynamic;

    private Integer executes;

    private Boolean export;

    // 服务名称
    private String path;

    private ServerConfig server;

    /**
     * 接口实现类引用
     */
    private T ref;

    private transient volatile boolean unregistered;

    private transient volatile boolean registered;

    private Class<?> interfaceClass;

    private List<MethodConfig> methods;
    /**
     * default instance.
     */
    public ServiceConfig(Container container){
    	setContainer(container);
    }

    /**
     * a
     */
    public synchronized void register() {
        if(unregistered)
            throw new IllegalStateException("Service already unregistered!");
        //已经注册了,不重复注册
        if(registered)
            return;
        registered=true;
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
        }
        //path
        if(path == null && path.length() == 0){
            path = interfaceName;
        }
        doExport();
    }
    protected void checkInterface(){
    	
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
    /**
	 * 
	 */
	private void doExport() {
		// TODO Auto-generated method stub
		
	}
	
	
	public synchronized void unregister() {
        if (! registered) {
            return;
        }
        if (unregistered) {
            return;
        }
        unregistered = true;
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
     * @return the server
     */
    public ServerConfig getServer() {
        return server;
    }
    
    /**
     * @param server the server to set
     */
    public void setServer(ServerConfig server) {
        this.server = server;
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
    
}

