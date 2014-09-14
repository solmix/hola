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


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月5日
 */

public class ServiceConfig<T> extends AbstractServiceConfig
{

    /**
     * 
     */
    private static final long serialVersionUID = -7539697586814177467L;

    private String interfaceName;

    // 服务版本
    protected String version;

    // 服务分组
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

    // 接口实现类引用
    private T ref;

    private transient volatile boolean unregistered;

    private transient volatile boolean registered;

    private Class<?> interfaceClass;


    /**
     * default instance.
     */
    public ServiceConfig(){
    }
    /**
     * 注册服务
     */
    public synchronized void register() {
        if (server != null) {
            export = export == null ? server.getExport() : export;
            delay = delay == null ? server.getDelay() : delay;
        }
        // default export=true;
        if (export != null && !export.booleanValue())
            return;
        if (delay != null && delay > 0) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (Throwable e) {
                    }
                    doRegister();
                }
            });
            thread.setDaemon(true);
            thread.setName("DelayRegisteThread");
            thread.start();
        } else {
            doRegister();
        }

    }

    /**
     * a
     */
    protected void doRegister() {
        if(unregistered)
            throw new IllegalStateException("Service already unregistered!");
        //已经注册了,不重复注册
        if(registered)
            return;
        registered=true;
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<hola:service interface=\"\" /> interface not allow null!");
        }
        mergeConfiguration();
        //check interface config.
        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (! interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
        //path
        if(path==null&&path.length()==0){
            path=interfaceName;
        }
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

    /**
     * @param server2
     */
    private void mergeConfiguration() {
        if(server!=null){
           if( application  == null)application=server.getApplication();
           if( module == null )     module =server.getModule() ;
           if( discoveries==null)   discoveries=server.getDiscoveries();
           if( monitor==null)       monitor=server.getMonitor();
           if( protocols==null)     protocols=server.getProtocols();
        }
        if(module!=null){
            if(discoveries==null) discoveries=module.getDiscoveries();
            if(monitor==null)     monitor=module.getMonitor();
        }
        if(application!=null){
            if(discoveries==null) discoveries=application.getDiscoveries();
            if(monitor==null)     monitor=application.getMonitor();
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
    
}

