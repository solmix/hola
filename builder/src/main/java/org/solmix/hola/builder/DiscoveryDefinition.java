/**
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

package org.solmix.hola.builder;

import org.solmix.commons.annotation.ThreadSafe;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年9月14日
 */
@ThreadSafe
public class DiscoveryDefinition extends AbstractBeanDefinition {

    private static final long serialVersionUID = 7819222894033381016L;

    public static final String NO_AVAILABLE = "N/A";

    private String name;

    /**
     * 公告服务地址
     */
    private String address;

    /**
     * 登陆公告服务用户名
     */
    private String username;

    /**
     * 登陆公告服务密码
     */
    private String password;

    /**
     * 公告服务协议
     */
    private String protocol;

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
    private Boolean check;

    // 在该注册中心上注册是动态的还是静态的服务
    private Boolean dynamic;

    private Boolean advertise;

    // 在该注册中心上服务是否引用
    private Boolean subscribe;

    /** 本地缓存公告,启动时可以临时使用 */
    private String file;

    private Boolean isDefault;

    /**   */
    public String getFile() {
        return file;
    }

    /**   */
    public void setFile(String file) {
        checkPathLength("file", file);
        this.file = file;
    }

    /**   */
    public String getName() {
        return name;
    }

    /**   */
    public void setName(String name) {
        checkName("name", name);
        this.name = name;
        if (id == null || id.length() == 0) {
            id = name;
        }
    }

    /**   */
    public String getAddress() {
        return address;
    }

    /**   */
    public void setAddress(String address) {
        this.address = address;
    }

    /**   */
    public String getUsername() {
        return username;
    }

    /**   */
    public void setUsername(String username) {
        checkName("username", username);
        this.username = username;
    }

    /**   */
    public String getPassword() {
        return password;
    }

    /**   */
    public void setPassword(String password) {
        checkLength("password", password);
        this.password = password;
    }

    /**   */
    public Integer getPort() {
        return port;
    }

    /**   */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**   */
    public String getCluster() {
        return cluster;
    }

    /**   */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /**   */
    public String getGroup() {
        return group;
    }

    /**   */
    public void setGroup(String group) {
        this.group = group;
    }

    /**   */
    public String getVersion() {
        return version;
    }

    /**   */
    public void setVersion(String version) {
        this.version = version;
    }

    /**   */
    public Integer getTimeout() {
        return timeout;
    }

    /**   */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**   */
    public Boolean isCheck() {
        return check;
    }

    /**   */
    public void setCheck(Boolean check) {
        this.check = check;
    }

    /**   */
    public Boolean isDynamic() {
        return dynamic;
    }

    /**   */
    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**   */
    public Boolean getAdvertise() {
        return advertise;
    }

    /**   */
    public void setAdvertise(Boolean advertise) {
        this.advertise = advertise;
    }

    /**   */
    public Boolean isSubscribe() {
        return subscribe;
    }

    /**   */
    public void setSubscribe(Boolean subscribe) {
        this.subscribe = subscribe;
    }

    /**   */
    public String getProtocol() {
        return protocol;
    }

    /**   */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /** 是否为默认配置 */
    public Boolean isDefault() {
        return isDefault;
    }

    /** 设置为默认配置 */
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
