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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月28日
 */

public class AbstractInterfaceInfo extends AbstractMethodInfo
{

    private static final long serialVersionUID = -5387102076550844738L;

    private String protocol;
    /**
     * 代理实现
     */
    protected String proxy;

    /** 集群方式 */
    protected String cluster;


    /** 服务注册和引用的范围,local为本地,remote为远程 */
    protected String scope;

    private ApplicationInfo application;

    private ModuleInfo module;

    private List<DiscoveryInfo> discoveries;

    private MonitorInfo monitor;

    
    /**   */
    public String getProtocol() {
        return protocol;
    }

    
    /**   */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**   */
    public String getProxy() {
        return proxy;
    }

    /**   */
    public void setProxy(String proxy) {
        this.proxy = proxy;
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
    public String getScope() {
        return scope;
    }

    /**   */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**   */
    public ApplicationInfo getApplication() {
        return application;
    }

    /**   */
    public void setApplication(ApplicationInfo application) {
        this.application = application;
    }

    /**   */
    public ModuleInfo getModule() {
        return module;
    }

    /**   */
    public void setModule(ModuleInfo module) {
        this.module = module;
    }

    /**   */
    public List<DiscoveryInfo> getDiscoveries() {
        return discoveries;
    }

    /**   */
    public void setDiscoveries(List<DiscoveryInfo> discoveries) {
        this.discoveries = discoveries;
    }

    /**   */
    public MonitorInfo getMonitor() {
        return monitor;
    }

    /**   */
    public void setMonitor(MonitorInfo monitor) {
        this.monitor = monitor;
    }

    public DiscoveryInfo getDiscovery() {
        return discoveries == null || discoveries.size() == 0 ? null
            : discoveries.get(0);
    }

    public void setDiscovery(DiscoveryInfo registry) {
        List<DiscoveryInfo> discoveries = new ArrayList<DiscoveryInfo>(1);
        discoveries.add(registry);
        this.discoveries = discoveries;
    }

}
