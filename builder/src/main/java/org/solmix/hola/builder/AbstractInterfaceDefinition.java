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

    /**
     * 代理实现
     */
    protected String proxy;

    /** 集群方式 */
    protected String cluster;

    /** 服务注册和引用的范围,local为本地,remote为远程 */
    protected String scope;

    protected ApplicationDefinition application;

    protected ModuleDefinition module;

    protected List<DiscoveryDefinition> discoveries;

    protected MonitorDefinition monitor;
    /**
     * <li>心跳间隔
     */
    private Integer heartbeat;
    

    /**
     * <li>心跳超时
     */
    private Integer heartbeatTimeout;
    /**
     * 编码实现名称
     */
    private String codec;
    /**
     * <li>有效负载，数据报文长度
     */
    private Integer palyload;
    
    /**
     * <li>连接超时时间
     */
    private Integer connectTimeout;
    /**
     * <li>序列化方法
     */
    private String serial;
    
    /**
     * <li>通信协议
     */
    private String protocol;
    /**
     * <li>主机
     */
    private String host;

    /**
     * <li>服务端口
     * 服务路径,默认为interface名称,如果设置了{@link ProviderDefinition#getContextpath()
     * contextpath}为 contextpath/path.
     */
    private Integer port;

    /**
     * <li>上下文路径
     */
    private String path;
    

    /**
     * <li>网络传输方式
     */
    private String transporter;

    /**
     * <li>缓存区大小
     */
    private Integer buffer;
    
    
    
    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    
    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    
    public String getCodec() {
        return codec;
    }

    
    public void setCodec(String codec) {
        this.codec = codec;
    }

    
    public Integer getPalyload() {
        return palyload;
    }

    
    public void setPalyload(Integer palyload) {
        this.palyload = palyload;
    }

    
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    
    public String getSerial() {
        return serial;
    }

    
    public void setSerial(String serial) {
        this.serial = serial;
    }

    
    public String getProtocol() {
        return protocol;
    }

    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    
    public String getHost() {
        return host;
    }

    
    public void setHost(String host) {
        this.host = host;
    }

    
    public Integer getPort() {
        return port;
    }

    
    public void setPort(Integer port) {
        this.port = port;
    }

    
    public String getPath() {
        return path;
    }

    
    public void setPath(String path) {
        checkPathName("path", path);
        this.path = path;
    }

    
    public String getTransporter() {
        return transporter;
    }

    
    public void setTransporter(String transporter) {
        this.transporter = transporter;
    }

    
    public Integer getBuffer() {
        return buffer;
    }

    
    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
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
        if(application!=null){
            appendSystemProperties(application);
        }
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
        if(monitor!=null){
            appendSystemProperties(monitor);
        }
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
    

    /**   */
    public Integer getHeartbeat() {
        return heartbeat;
    }

    /**   */
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }
    
    protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodDefinition> methods) {
        // 接口不能为空
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        // 检查接口类型必需为接口
        if(! interfaceClass.isInterface()) { 
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        // 检查方法是否在接口中存在
        if (methods != null && methods.size() > 0) {
            for (MethodDefinition methodBean : methods) {
                String methodName = methodBean.getName();
                if (methodName == null || methodName.length() == 0) {
                    throw new IllegalStateException("<hola:method> name attribute is required! Please check: <dubbo:service interface=\"" + interfaceClass.getName() + "\" ... ><dubbo:method name=\"\" ... /></<dubbo:reference>");
                }
                boolean hasMethod = false;
                for (java.lang.reflect.Method method : interfaceClass.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        hasMethod = true;
                        break;
                    }
                }
                if (!hasMethod) {
                    throw new IllegalStateException("The interface " + interfaceClass.getName() + " not found method " + methodName);
                }
            }
        }
    }
    
    
    

}
