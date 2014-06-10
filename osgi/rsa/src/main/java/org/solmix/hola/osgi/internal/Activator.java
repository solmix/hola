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

package org.solmix.hola.osgi.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.solmix.hola.osgi.rsa.EndpointDescriptionLocator;
import org.solmix.hola.osgi.rsa.HolaRemoteServiceAdmin;
import org.solmix.hola.rs.RSProviderManager;
import org.solmix.hola.rs.RemoteServiceProviderDescription;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月2日
 */

public class Activator implements BundleActivator
{

    private static final String PROXY_BUNDLE_SYMBOLIC_ID = "org.solmix.hola.remoteserviceadmin.proxy";

    private static BundleContext context;

    private static Activator instance;

    private BundleContext proxyServiceFactoryBundleContext;

    private ServiceRegistration<?> remoteServiceAdminRegistration;
    
    private EndpointDescriptionLocator endpointDescriptionLocator;

    private final Map<Bundle, HolaRemoteServiceAdmin> remoteServiceAdmins = new HashMap<Bundle, HolaRemoteServiceAdmin>(1);

    private ServiceRegistration<?> serviceMetadataFactoryRegistration;

    private void initializeProxyServiceFactoryBundle() throws Exception {
        // First, find proxy bundle
        for (Bundle b : context.getBundles()) {
            if (PROXY_BUNDLE_SYMBOLIC_ID.equals(b.getSymbolicName())) {
                // first start it
                b.start();
                // then get its bundle context
                proxyServiceFactoryBundleContext = b.getBundleContext();
            }
        }
        if (proxyServiceFactoryBundleContext == null)
            throw new IllegalStateException("RSA Proxy bundle (symbolic id=='"
                + PROXY_BUNDLE_SYMBOLIC_ID
                + "') cannot be found, so RSA cannot be started");
    }

    private void stopProxyServiceFactoryBundle() {
        if (proxyServiceFactoryBundleContext != null) {
            // stop it
            try {
                proxyServiceFactoryBundleContext.getBundle().stop();
            } catch (Exception e) {
                // we don't care
            }
            proxyServiceFactoryBundleContext = null;
        }
    }

    public BundleContext getProxyServiceFactoryBundleContext() {
        return proxyServiceFactoryBundleContext;
    }
    
    /**
     * @return the context
     */
    public static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        Activator.instance = this;
        /**
         * 初始化代理Bundle,创建一个虚拟Bundle,代理注册RSA,
         * 无论是equinox还是felix都是通过检测ServiceFactory来扩展,
         * 如果BundleContext不是通过ServiceFacotory的ClassLoader来加载的
         * ,那么就可能受OSGI规范的限制而找不到包
         */
        initializeProxyServiceFactoryBundle();

        // make remote service admin available
        Hashtable<String,Object> rsaProps = new Hashtable<String,Object>();
        rsaProps.put(HolaRemoteServiceAdmin.RSA_SUPPORT_KEY, new Boolean(true));
        rsaProps.put(RemoteConstants.REMOTE_CONFIGS_SUPPORTED,getRemoteSupportedConfigs());
        rsaProps.put(RemoteConstants.REMOTE_INTENTS_SUPPORTED,getRemoteSupportedIntents());
        
        //注册OSGI RSA 服务,为每个使用RSA服务的Bundle创建RSA实例
        remoteServiceAdminRegistration = context.registerService(
            RemoteServiceAdmin.class.getName(),
            new ServiceFactory<RemoteServiceAdmin>() {

                @Override
                public RemoteServiceAdmin getService(Bundle bundle,
                    ServiceRegistration<RemoteServiceAdmin> registration) {
                    RemoteServiceAdmin result = null;
                    synchronized (remoteServiceAdmins) {
                        HolaRemoteServiceAdmin rsa = remoteServiceAdmins.get(bundle);
                        if (rsa == null) {
                            rsa = new HolaRemoteServiceAdmin(bundle);
                            remoteServiceAdmins.put(bundle, rsa);
                        }
                        result = rsa;
                    }
                    return result;
                }

                @Override
                public void ungetService(Bundle bundle,
                    ServiceRegistration<RemoteServiceAdmin> registration,
                    RemoteServiceAdmin service) {
                    synchronized (remoteServiceAdmins) {
                        HolaRemoteServiceAdmin rsa = remoteServiceAdmins.remove(bundle);
                        if (rsa != null)
                            rsa.close();
                    }
                }

            },  rsaProps);

        // 初始化EndpointDescriptionLocator,用于查找和发现EndpointDescription
        endpointDescriptionLocator = new EndpointDescriptionLocator(context);
        // // 启动EndpointDescription查找
        endpointDescriptionLocator.start();
    }

    /**
     * 服务提供者支持的实施意图
     * Service property identifying the intents supported by a distribution
     * provider. Registered by the distribution provider on one of its services
     * to indicate the vocabulary of implemented intents.
     * 
     * @return
     */
    private String[] getRemoteSupportedIntents() {
        List<RemoteServiceProviderDescription> descs = RSProviderManager.getDefault().getDescriptions();
        List<String> supportedIntents = new ArrayList<String>();
        for(RemoteServiceProviderDescription desc:descs){
            String[]  intents= desc.getSupportedIntents();
            if(intents!=null){
                for(String intent:intents){
                    supportedIntents.add(intent); 
                }
            }
        }
        return supportedIntents.toArray(new String[supportedIntents.size()]);
    }

    /**
     * 
     * 服务提供者支持的配置类型
     * NOTE:RemoteService 的启动等级需高于该bundle启动等级,才能在注册RSA时可以根据已经注册的RS生产对应参数
     * Service property identifying the configuration types supported by a distribution 
     * provider. Registered by the distribution provider on one of its services to indicate 
     * the supported configuration types.
     * @return
     */
    private String[] getRemoteSupportedConfigs() {
        List<RemoteServiceProviderDescription> descs = RSProviderManager.getDefault().getDescriptions();
        List<String> supportedConfigs = new ArrayList<String>();
        for(RemoteServiceProviderDescription desc:descs){
            String[]  configs= desc.getSupportedConfigs();
            if(configs!=null){
                for(String config:configs){
                    supportedConfigs.add(config); 
                }
            }
        }
        return supportedConfigs.toArray(new String[supportedConfigs.size()]);
    }

    /**
     * @return
     */
  /*  private String[][] getSupportedConfigsAndIntents() {
        IContainerManager containerManager = getContainerManager();
        Assert.isNotNull(containerManager,
            "Container manager must be present to start ECF Remote Service Admin");
        ContainerTypeDescription[] remoteServiceDescriptions = containerManager.getContainerFactory().getDescriptionsForContainerAdapter(
            IRemoteServiceContainerAdapter.class);
        List<String> supportedConfigs = new ArrayList<String>();
        List<String> supportedIntents = new ArrayList<String>();
        for (int i = 0; i < remoteServiceDescriptions.length; i++) {
            String[] descSupportedConfigs = remoteServiceDescriptions[i].getSupportedConfigs();
            if (descSupportedConfigs != null) {
                for (int j = 0; j < descSupportedConfigs.length; j++)
                    supportedConfigs.add(descSupportedConfigs[j]);
                String[] descSupportedIntents = remoteServiceDescriptions[i].getSupportedIntents();
                for (int j = 0; j < descSupportedIntents.length; j++)
                    supportedIntents.add(descSupportedIntents[j]);
            }
        }
        String[][] result = new String[2][];
        result[0] = supportedConfigs.toArray(new String[supportedConfigs.size()]);
        result[1] = supportedIntents.toArray(new String[supportedIntents.size()]);
        return result;
    }*/

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (endpointDescriptionLocator != null) {
            endpointDescriptionLocator.stop();
            endpointDescriptionLocator = null;
        }
        if (remoteServiceAdminRegistration != null) {
            remoteServiceAdminRegistration.unregister();
            remoteServiceAdminRegistration = null;
        }
        if (this.serviceMetadataFactoryRegistration != null) {
            serviceMetadataFactoryRegistration.unregister();
            serviceMetadataFactoryRegistration = null;
        }
        synchronized (remoteServiceAdmins) {
            for (Iterator<Entry<Bundle, HolaRemoteServiceAdmin>> i = remoteServiceAdmins.entrySet().iterator(); i.hasNext();) {
                Entry<Bundle, HolaRemoteServiceAdmin> entry = i.next();
                HolaRemoteServiceAdmin rsa = entry.getValue();
                rsa.close();
                i.remove();
            }
        }
        stopProxyServiceFactoryBundle();
        Activator.context = null;
        Activator.instance = null;
    }

    /**
     * @return
     */
    public static Activator getDefault() {
        return instance;
    }

    /**
     * Get osgi framework UUID
     * 
     * @return
     */
    public String getFrameworkUUID() {
        if (context == null)
            return null;
        synchronized ("org.osgi.framework.uuid") {
            String result = context.getProperty("org.osgi.framework.uuid");
            if (result == null) {
                UUID newUUID = UUID.randomUUID();
                result = newUUID.toString();
                System.setProperty("org.osgi.framework.uuid",
                    newUUID.toString());
            }
            return result;
        }
    }

}
