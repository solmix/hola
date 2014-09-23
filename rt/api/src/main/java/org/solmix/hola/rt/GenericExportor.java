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
package org.solmix.hola.rt;

import org.solmix.hola.rt.config.ApplicationConfig;
import org.solmix.hola.rt.config.ModuleConfig;
import org.solmix.hola.rt.config.ServerConfig;
import org.solmix.hola.rt.config.ServiceConfig;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月10日
 */

public class GenericExportor implements ServiceExportor
{

    protected   ServiceConfig<?> config;
    private  volatile boolean unexported;

    private  volatile boolean exported;
    private Class<?> interfaceClass;
    public GenericExportor(){
        
    }
    public GenericExportor(ServiceConfig<?> type){
        this.config=type;
    }
  
    @Override
    public void setConfig(ServiceConfig<?> config) {
        this.config=config;
    }
   
    @Override
    public ServiceConfig<?> getConfig() {
        return config;
    }
   
    @Override
    public synchronized void export() {
        if(unexported)
            throw new IllegalStateException("Service already unexported!");
        if(exported)
            return;
        exported=true;
        config=prepareConfig(config);
        prepareLoad();
        if(config.getExport()!=null&&!config.getExport().booleanValue()){
            return;
        }
        final Integer delay=config.getDelay();
        if(delay != null && delay > 0){
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (Throwable e) {
                    }
                    doExport();
                }
            });
            thread.setDaemon(true);
            thread.setName("DelayExportThread");
            thread.start();
        }else{
            doExport();
        }
        
    }
   
    /**
     * 
     */
    protected void doExport() {
    	config.register();
    }
    private void prepareLoad() {
        try {
            interfaceClass = Class.forName(config.getInterface(), true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        if (config.getRef() == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (! interfaceClass.isInstance(config.getRef())) {
            throw new IllegalStateException("The class "
                    + config.getRef().getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
        //path
        if(config.getPath()==null || config.getPath().length()==0){
            config.setPath(config.getInterface());
        }
        
    }
    @Override
    public synchronized void unexport() {
        if (! exported) {
            return;
        }
        if (unexported) {
            return;
        }
        
    }
    
    @Override
    public boolean isExported() {
        return exported;
    }

    @Override
    public boolean isUnexported() {
        return unexported;
    }
    
    protected  ServiceConfig<?> prepareConfig(ServiceConfig<?> s){
        if(s==null){
            throw new IllegalArgumentException("ServiceConfig is null");
        }
        if (s.getInterface() == null || s.getInterface().length() == 0) {
            throw new IllegalStateException("<hola:service interface=\"\" /> interface not allow null!");
        }
        if (s.getServer() != null) {
           ServerConfig server= s.getServer();
           if(s.getExport()==null){
               s.setExport(server.getExport());
           }
           if(s.getDelay()==null){
               s.setDelay(server.getDelay());
           }
           if(s.getApplication()==null){
               s.setApplication(server.getApplication());
           }
           if(s.getModule()==null){
               s.setModule(server.getModule());
           }
           if(s.getDiscoveries()==null){
               s.setDiscoveries(server.getDiscoveries());
           }
           if(s.getMonitor()==null){
               s.setMonitor(server.getMonitor());
           }
           /*if(s.getProtocols()==null){
               s.setProtocols(server.getProtocols());
           }*/
        }
        if(s.getModule()!=null){
            ModuleConfig module= s.getModule();
            if(s.getDiscoveries()==null){
                s.setDiscoveries(module.getDiscoveries());
            }
            if(s.getMonitor()==null){
                s.setMonitor(module.getMonitor());
            }
        }
        if(s.getApplication()!=null){
            ApplicationConfig app= s.getApplication();
            if(s.getDiscoveries()==null){
                s.setDiscoveries(app.getDiscoveries());
            }
            if(s.getMonitor()==null){
                s.setMonitor(app.getMonitor());
            }
        }
        return s;
    }
}
