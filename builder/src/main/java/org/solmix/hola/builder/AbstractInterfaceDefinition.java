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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月28日
 */

public class AbstractInterfaceDefinition extends AbstractMethodDefinition {

    private static final long serialVersionUID = -5387102076550844738L;

    public static final String SCOPE_NONE = "none";

    public static final String SCOPE_REMOTE = "remote";

    public static final String SCOPE_LOCAL = "local";

    private String protocol;

    /**
     * 代理实现
     */
    protected String proxy;

    /** 集群方式 */
    protected String cluster;

    /** 服务注册和引用的范围,local为本地,remote为远程 */
    protected String scope;

    private ApplicationDefinition application;

    private ModuleDefinition module;

    private List<DiscoveryDefinition> discoveries;

    private MonitorDefinition monitor;

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
    public ApplicationDefinition getApplication() {
        return application;
    }

    /**   */
    public void setApplication(ApplicationDefinition application) {
        this.application = application;
    }

    /**   */
    public ModuleDefinition getModule() {
        return module;
    }

    /**   */
    public void setModule(ModuleDefinition module) {
        this.module = module;
    }

    /**   */
    public List<DiscoveryDefinition> getDiscoveries() {
        return discoveries;
    }

    /**   */
    public void setDiscoveries(List<DiscoveryDefinition> discoveries) {
        this.discoveries = discoveries;
    }

    /**   */
    public MonitorDefinition getMonitor() {
        return monitor;
    }

    /**   */
    public void setMonitor(MonitorDefinition monitor) {
        this.monitor = monitor;
    }

    public DiscoveryDefinition getDiscovery() {
        return discoveries == null || discoveries.size() == 0 ? null
            : discoveries.get(0);
    }

    public void setDiscovery(DiscoveryDefinition registry) {
        List<DiscoveryDefinition> discoveries = new ArrayList<DiscoveryDefinition>(1);
        discoveries.add(registry);
        this.discoveries = discoveries;
    }

}
