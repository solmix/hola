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

import java.util.List;

import org.solmix.hola.core.model.EndpointInfo;
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
   
    public GenericExportor(ServiceConfig<?> type){
        this.config=type;
        this.config.setServiceExportor(this);
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
        config=checkConfig(config);
        //可以设置为普通服务,不发布服务.
        if(config.getExport()!=null&&!config.getExport().booleanValue()){
            return;
        }
        //延迟启动
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
        
    	List<EndpointInfo> endpoints= config.getEndpointInfo();
    	
    	//发布服务
    	for(EndpointInfo endpoint: endpoints){
    	    doExport(endpoint);
    	}
    	
    	
    }
   
    /**
     * @param server
     * @param endpoints
     */
    private void doExport(EndpointInfo endpoints) {
        // TODO Auto-generated method stub
        
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
    
    protected  ServiceConfig<?> checkConfig(ServiceConfig<?> s){
        if(s==null){
            throw new IllegalArgumentException("ServiceConfig is null");
        }
        if (s.getInterface() == null || s.getInterface().length() == 0) {
            throw new IllegalStateException("<hola:service interface=\"\" /> interface not allow null!");
        }
       
        return s;
    }
}
