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

import java.util.Map;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月5日
 */

public class DiscoveryConfig extends AbstractConfig
{

    private static final long serialVersionUID = 7819222894033381016L;

    public static final String NO_AVAILABLE = "N/A";

    /**
     * 公告服务地址
     */
    private String address;

    /**
     * 登陆公告服务用户名
     */
    private String username;

    /**
     *  登陆公告服务密码
     */
    private String password;

    /**
     * 公告服务端口
     */
    private Integer port;

    private String cluster;

    private String group;

    private String version;

    // 注册中心请求超时时间(毫秒)
    private Integer timeout;
    
    // 启动时检查注册中心是否存在
    private Boolean           check;

    // 在该注册中心上注册是动态的还是静态的服务
    private Boolean           dynamic;
    
    // 在该注册中心上服务是否暴露
    private Boolean           register;
    
    // 在该注册中心上服务是否引用
    private Boolean           subscribe;
    
    private Map<String, Object> properties;
    
    public DiscoveryConfig(){
    	
    }

    public DiscoveryConfig(String address){
    	setAddress(address);
    }
    /**
     * 公告服务实现
     */
    private String provider;
    
    
    /**
	 * @return the provider
	 */
	public String getProvider() {
		return provider;
	}


	/**
	 * @param provider the provider to set
	 */
	public void setProvider(String provider) {
		checkName("provider", provider);
		this.provider = provider;
	}


	/**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    
    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    
    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
    	checkName("username", username);
        this.username = username;
    }

    
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
    	checkLength("password", password);
        this.password = password;
    }

    
    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    
    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    
    /**
     * @return the cluster
     */
    public String getCluster() {
        return cluster;
    }

    
    /**
     * @param cluster the cluster to set
     */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    
    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    
    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    
    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    
    /**
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    
    /**
     * @return the check
     */
    public Boolean getCheck() {
        return check;
    }

    
    /**
     * @param check the check to set
     */
    public void setCheck(Boolean check) {
        this.check = check;
    }

    
    /**
     * @return the dynamic
     */
    public Boolean getDynamic() {
        return dynamic;
    }

    
    /**
     * @param dynamic the dynamic to set
     */
    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    
    /**
     * @return the register
     */
    public Boolean getRegister() {
        return register;
    }

    
    /**
     * @param register the register to set
     */
    public void setRegister(Boolean register) {
        this.register = register;
    }

    
    /**
     * @return the subscribe
     */
    public Boolean getSubscribe() {
        return subscribe;
    }

    
    /**
     * @param subscribe the subscribe to set
     */
    public void setSubscribe(Boolean subscribe) {
        this.subscribe = subscribe;
    }

    
    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    
    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
