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
package org.solmix.hola.discovery.zk;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.common.HolaConstants;
import org.solmix.hola.common.config.DiscoveryInfo;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.ServiceInfo;
import org.solmix.hola.discovery.identity.ServiceID;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.FailbackDiscovery;
import org.solmix.hola.discovery.zk.identity.ZKNamespace;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月15日
 */

public class ZKDiscovery extends FailbackDiscovery
{
    private static final Logger LOG=LoggerFactory.getLogger(ZKDiscovery.class);
    private final static String DEFAULT_ROOT = "hola_root";
    private final static String DEFAULT_MUTI = "hola_muti";
    private boolean closed;
    
    private final ZKClient zkClient;
    private final String root;
    private final Set<ServiceInfo> knowMultiInterface = new CopyOnWriteArraySet<ServiceInfo>();

    /**
     * @param discoveryNamespace
     */
    public ZKDiscovery(DiscoveryInfo info ,Container container, ZKTransporter zkTransporter) throws DiscoveryException
    {
        super(ZKNamespace.NAME, container, info);
        if (info.getAddress() == null) {
            throw new IllegalStateException("discovery address is null!");
        }
        String group = info.getGroup(DEFAULT_ROOT);
        // zk路径必须以/开头
        if (!group.startsWith("/")) {
            group = "/" + group;
        }
        root = group;
        zkClient = zkTransporter.connect(info);
        zkClient.addStateListener(new StateListener() {

            @Override
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        recover();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        });
        mackupMultiService();
    }

  
    
    /**
     * 
     */
    private void mackupMultiService() {
        zkClient.getChildren(DEFAULT_MUTI);
          zkClient.addChildListener(DEFAULT_MUTI, new ChildListener() {
            
            @Override
            public void childChanged(String path, List<String> children) {
//                zkClient.
                
            }
        });
    }



    @Override
    public void close()throws IOException{
        super.close();
        if(closed){
            return;
        }
        try {
            zkClient.close();
        } catch (Exception e) {
            LOG.warn("Failed to close zookeeper client " + getInfo() + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getService(org.solmix.hola.discovery.identity.ServiceID)
     */
    @Override
    public ServiceInfo getService(ServiceID aServiceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices()
     */
    @Override
    public ServiceInfo[] getServices() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServices(org.solmix.hola.discovery.identity.ServiceType)
     */
    @Override
    public ServiceInfo[] getServices(ServiceType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#getServiceTypes()
     */
    @Override
    public ServiceType[] getServiceTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.DiscoveryLocator#purgeCache()
     */
    @Override
    public ServiceInfo[] purgeCache() {
        // TODO Auto-generated method stub
        return null;
    }



    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.support.FailbackDiscovery#doRegister(org.solmix.hola.discovery.ServiceInfo)
     */
    @Override
    protected void doRegister(ServiceInfo meta) {
        try {
            boolean dynamic=true;
            if(meta.getServiceProperties()!=null){
              String d=  meta.getServiceProperties().getPropertyString(HolaConstants.KEY_DYNAMIC);
               if(d!=null&&!Boolean.valueOf(d).booleanValue()){
                   dynamic=false;
               }
            }
            zkClient.create(toUrlPath(meta), dynamic);
        } catch (Throwable e) {
            throw new DiscoveryException("Failed to register " + meta + " to zookeeper " + getInfo() + ", cause: " + e.getMessage(), e);
        }
        
    }



    /**
     * 根据服务元数据描述生成zk需要的路径
     */
    private String toUrlPath(ServiceInfo info) {
      ServiceType type= info.getServiceID().getServiceType();
     String[] services= type.getServices();
        return null;
    }



    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.support.FailbackDiscovery#doUnregister(org.solmix.hola.discovery.ServiceInfo)
     */
    @Override
    protected void doUnregister(ServiceInfo meta) {
        try {
            zkClient.delete(toUrlPath(meta));
        } catch (Throwable e) {
            throw new DiscoveryException("Failed to unregister " + meta + " to zookeeper " + getInfo() + ", cause: " + e.getMessage(), e);
        }
    }

}
