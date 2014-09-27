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
package org.solmix.hola.rt.config;

import java.util.ArrayList;
import java.util.List;

import org.solmix.commons.util.DataUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.discovery.ServiceInfo;
import org.solmix.hola.rs.RemoteManagerProtocol;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月6日
 */

public class AbstractClassConfig extends AbstractMethodConfig {

	private static final long serialVersionUID = -3515821709848299465L;
	/**
	 * 代理实现
	 */
	protected String proxy;

	protected String cluster;

	protected Integer connections;

	protected String payload;

	protected String scope;

	protected ApplicationConfig application;

	protected ModuleConfig module;
	/**
	 * 监控中心
	 */
	protected MonitorConfig monitor;

	protected List<DiscoveryConfig> discoveries;

	/**
	 * 远程服务实现
	 */
	protected String protocol;

	/**
	 * @return the proxy
	 */
	public String getProxy() {
		return proxy;
	}

	/**
	 * @param proxy
	 *            the proxy to set
	 */
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	/**
	 * @return the cluster
	 */
	public String getCluster() {
		return cluster;
	}

	/**
	 * @param cluster
	 *            the cluster to set
	 */
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	/**
	 * @return the connections
	 */
	public Integer getConnections() {
		return connections;
	}

	/**
	 * @param connections
	 *            the connections to set
	 */
	public void setConnections(Integer connections) {
		this.connections = connections;
	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the application
	 */
	public ApplicationConfig getApplication() {
		return application;
	}

	/**
	 * @param application
	 *            the application to set
	 */
	public void setApplication(ApplicationConfig application) {
		this.application = application;
	}

	/**
	 * @return the module
	 */
	public ModuleConfig getModule() {
		return module;
	}

	/**
	 * @param module
	 *            the module to set
	 */
	public void setModule(ModuleConfig module) {
		this.module = module;
	}

	/**
	 * @return the monitor
	 */
	public MonitorConfig getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor
	 *            the monitor to set
	 */
	public void setMonitor(MonitorConfig monitor) {
		this.monitor = monitor;
	}

	/**
	 * @return the discoveries
	 */
	public List<DiscoveryConfig> getDiscoveries() {
		return discoveries;
	}

	/**
	 * @param discoveries
	 *            the discoveries to set
	 */
	public void setDiscoveries(List<DiscoveryConfig> discoveries) {
		this.discoveries = discoveries;
	}

	public DiscoveryConfig getDiscovery() {
		return discoveries == null || discoveries.size() == 0 ? null
				: discoveries.get(0);
	}

	public void setDiscovery(DiscoveryConfig registry) {
		List<DiscoveryConfig> registries = new ArrayList<DiscoveryConfig>(1);
		registries.add(registry);
		this.discoveries = registries;
	}

	/**
	 * @return the provider
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
		checkExtension(RemoteManagerProtocol.class, "protocol", protocol);
	}
	
	/**
	 * 从公告牌加载服务信息
	 * 
	 * @param server
	 * @return
	 */
    protected List<ServiceInfo> loadServiceInfo(boolean server) {
        checkDiscovery();
        List<ServiceInfo> discoveried = new ArrayList<ServiceInfo>();
        if (DataUtils.isNotNullAndEmpty(discoveried)) {
            for (DiscoveryConfig discovery : discoveries) {
                String address = discovery.getAddress();
                if (address == null || address.length() == 0) {
                    //为空先从系统配置中查找
                    String sysaddress = System.getProperty("hola.discovery.address");
                    if (sysaddress != null && sysaddress.length() > 0) {
                        address = sysaddress;
                    }
                }
                
                if (address == null || address.length() == 0) {
                    address = HolaConstants.ANYHOST_VALUE;
                }
                
                if(address!=null
                    && address.length() >0
                    && DiscoveryConfig.NO_AVAILABLE.equalsIgnoreCase(address)){
                    
                }
            }
        }
        return discoveried;
    }

	 protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodConfig> methods) {
		 if (interfaceClass == null) {
	            throw new IllegalStateException("interface not allow null!");
	        }
		 if(! interfaceClass.isInterface()) { 
	            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
	        }
		 if (methods != null && methods.size() > 0) {
	            for (MethodConfig methodBean : methods) {
	                String methodName = methodBean.getName();
	                if (methodName == null || methodName.length() == 0) {
	                    throw new IllegalStateException("method name attribute is required! ");
	                }
	                boolean hasMethod = false;
	                for (java.lang.reflect.Method method : interfaceClass.getMethods()) {
	                    if (method.getName().equals(methodName)) {
	                        hasMethod = true;
	                        break;
	                    }
	                }
	                if (!hasMethod) {
	                    throw new IllegalStateException("The interface " + interfaceClass.getName()
	                            + " not found method " + methodName);
	                }
	            }
	        }
	 }
	 protected void checkApplication(){
		 if(application==null)
			 application=new ApplicationConfig();
	 }
	 
	 protected void checkDiscovery(){
		 if(discoveries==null||discoveries.size()==0){
			 setDiscovery(new DiscoveryConfig(DiscoveryConfig.NO_AVAILABLE));
		 }
	 }
	
}
