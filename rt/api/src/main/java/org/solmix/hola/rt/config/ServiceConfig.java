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
package org.solmix.hola.rt.config;

import java.util.Arrays;
import java.util.List;

import org.solmix.commons.util.StringUtils;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.ContainerFactory;




/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年10月28日
 */

public class ServiceConfig<T> extends AbstractServiceConfig implements ContainerAware
{
    /**    */
    private static final long serialVersionUID = -6412855348674400197L;
    private String[] interfaceNames;

    /**
     * 延迟发布时间(ms)
     */
    protected Integer delay;

    /**
     * 服务路径,默认为interface名称,如果设置了{@link ServerConfig#getContextpath()
     * contextpath}为 contextpath/path.
     */
    private String path;
    /**
     * 一个服务可以通过多种协议发布
     */
    private List<ServerConfig> servers;

    /**
     * 接口实现类引用
     */
    private T ref;

    private Class<?>[] interfaceClass;

    /**
     * 方法配置
     */
    private List<MethodConfig> methods;
    
    
    private boolean generic;
    
    private Container container;
    
    public ServiceConfig(T t){
        this(ContainerFactory.getThreadDefaultContainer(),t);
    }
    public ServiceConfig(Container c,T t){
        this.container=c;
        this.ref=t;
    }
    /**   */
    public boolean isGeneric() {
        return generic;
    }
    
    /**   */
    public void setGeneric(boolean generic) {
        this.generic = generic;
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
    
    @Override
    public void setContainer(Container container) {
        this.container=container;
        
    }
    
    public Container getContainer() {
        return container;
    }

}
