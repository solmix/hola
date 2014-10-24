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

package org.solmix.hola.core.model;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月3日
 */

public class MonitorInfo extends AbstractHolaInfo
{

    private static final long serialVersionUID = 2315183160517205446L;

    private String address;

    private String protocol;

    private String username;

    private String password;

    private String group;

    private String version;
    
    private Boolean isDefault;

    
    public String getAddress() {
        return address;
    }

    
    public void setAddress(String address) {
        this.address = address;
    }

    
    public String getProtocol() {
        return protocol;
    }

    
    /**   */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    
    /**   */
    public String getUsername() {
        return username;
    }

    
    /**   */
    public void setUsername(String username) {
        this.username = username;
    }

    
    /**   */
    public String getPassword() {
        return password;
    }

    
    /**   */
    public void setPassword(String password) {
        this.password = password;
    }

    
    /** 分组  */
    public String getGroup() {
        return group;
    }

    
    /** 分组  */
    public void setGroup(String group) {
        this.group = group;
    }

    
    /** 版本号  */
    public String getVersion() {
        return version;
    }

    
    /**版本号*/
    public void setVersion(String version) {
        this.version = version;
    }

    /** 是否为默认配置  */
    public Boolean isDefault() {
        return isDefault;
    }

    
    /** 设置为默认配置  */
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
}
