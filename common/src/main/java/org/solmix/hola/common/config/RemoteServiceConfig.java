/**
 * Copyright (c) 2014 The Solmix Project
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

package org.solmix.hola.common.config;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月17日
 */

public class RemoteServiceConfig {

    private ServiceConfig<?> service;
    
    private ServerConfig server;

    private String address;

    public ServiceConfig<?> getService() {
        return service;
    }

    public void setService(ServiceConfig<?> service) {
        this.service = service;
    }

    public String getAddress() {
        return address;
    }
    
    
    /** 简化参数的设置 */
    public void setAddress(String address) {
        this.address = address;
    }

    /**   */
    public ServerConfig getServer() {
        return server;
    }
    
    /**   */
    public void setServer(ServerConfig server) {
        this.server = server;
    }

}
