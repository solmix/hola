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

package org.solmix.hola.osgi.rsa.support;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.osgi.rsa.AbstractSelector;
import org.solmix.hola.osgi.rsa.ConsumerSelector;
import org.solmix.hola.osgi.rsa.HolaEndpointDescription;
import org.solmix.hola.osgi.rsa.PropertiesUtil;
import org.solmix.hola.rs.ProviderCreateException;
import org.solmix.hola.rs.RemoteServiceProvider;
import org.solmix.hola.rs.RemoteServiceProviderDescription;
import org.solmix.hola.rs.RemoteServiceProviderManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月21日
 */

public class ConsumerSelectorImpl extends AbstractSelector implements
    ConsumerSelector
{

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerSelectorImpl.class.getName());

    private final boolean autoCreate = true;

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.ConsumerSelector#selectConsumer(org.osgi.service.remoteserviceadmin.EndpointDescription)
     */
    @Override
    public RemoteServiceProvider selectConsumer(
        HolaEndpointDescription endpointDescription) throws Exception {
        List<String> sic = PropertiesUtil.getStringPlusProperty(
            endpointDescription.getProperties(),
            RemoteConstants.SERVICE_IMPORTED_CONFIGS);
        String[] serviceImportedConfigs = sic.toArray(new String[sic.size()]);
        ID providerID = endpointDescription.getProviderID();
        ID targetID = endpointDescription.getConnectTargetID();
        RemoteServiceProvider consumer = selectExistingProviders(providerID,
            targetID, serviceImportedConfigs);
        
        if (consumer == null && autoCreate) {
            consumer=createProvider(serviceImportedConfigs,endpointDescription.getProperties());
        }
        connectToTarget(consumer,targetID);
        return consumer;
    }

    /**
     * @param consumer
     * @param targetID
     */
    private void connectToTarget(RemoteServiceProvider consumer, ID targetID) {
        if(consumer==null)
            return;
       ID target= consumer.getTargetID();
       //targetID 表示还未连接
       if(target==null){
           try {
            consumer.connect(targetID, null);
        } catch (ConnectException e) {
           LOG.error("Connect Consumer Provider failed",e);
        }
       }
        
    }

    private RemoteServiceProvider createProvider(
        String[] remoteSupportedConfigs, Map<String, Object> properties) {
        if(remoteSupportedConfigs==null||remoteSupportedConfigs.length==0)
            return null;
        List<RemoteServiceProviderDescription>  descs=   getRemoteServiceProviderManager().getDescriptions();
        if(descs==null)
            return null;
        for(RemoteServiceProviderDescription desc:descs){
            String[] localConfigs=  desc.getImportedConfigs(remoteSupportedConfigs);
            //取第一个配置参数作为RemoteServiceProviderDescription name.
            String descriptionName=localConfigs[0];
            if(localConfigs!=null){
                RemoteServiceProviderManager manager= getRemoteServiceProviderManager();
                try {
                    RemoteServiceProvider provider=null;
                    if(properties==null)
                        provider=manager.createProvider(descriptionName);
                    else
                        provider=manager.createProvider(descriptionName,properties);
                    if(provider!=null)
                        return provider;
                } catch (ProviderCreateException e) {
                    LOG.error("create provider failed", e);
                }
            }
        }
        return null;
    }

    private RemoteServiceProvider selectExistingProviders(ID providerID,
        ID targetID, String[] serviceImportedConfigs) {
        RemoteServiceProvider[] providers = getRemoteServiceProviderManager().getProviders();
        if (providers == null || providers.length == 0) {
            return null;
        }
        for (RemoteServiceProvider provider : providers) {
            if (matchProviderID(provider, providerID)) {
                continue;
            }
            if (matchNamespace(provider, providerID, targetID)
                && matchSupportedConfigs(provider, serviceImportedConfigs)
                && matchConnected(provider, providerID, targetID)) {

                if (LOG.isTraceEnabled()) {
                    LOG.trace("MATCH of existing remote service container id="
                        + provider.getID()
                        + " endpointID="
                        + providerID
                        + " remoteSupportedConfigs="
                        + ((serviceImportedConfigs == null) ? "[]"
                            : Arrays.asList(serviceImportedConfigs).toString()));
                }
                return provider;
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("No match of existing remote service container id="
                        + provider.getID()
                        + " endpointID="
                        + providerID
                        + " remoteSupportedConfigs="
                        + ((serviceImportedConfigs == null) ? "[]"
                            : Arrays.asList(serviceImportedConfigs).toString()));
                }
            }
        }
        return null;
    }

    protected boolean matchConnected(RemoteServiceProvider provider,
        ID endpointID, ID connectTargetID) {
        // if the container is not connected, OR it's connected to the desired
        // endpointID already then we've got a match
        ID connectedID = provider.getTargetID();
        if (connectedID == null || connectedID.equals(endpointID)
            || connectedID.equals(connectTargetID))
            return true;
        return false;
    }

    protected boolean matchSupportedConfigs(RemoteServiceProvider provider,
        String[] remoteSupportedConfigs) {
        if (remoteSupportedConfigs == null)
            return false;
        RemoteServiceProviderDescription description = getProviderDescription(provider);
        if (description == null)
            return false;
        return description.getImportedConfigs(remoteSupportedConfigs) != null;
    }

    private RemoteServiceProviderDescription getProviderDescription(
        RemoteServiceProvider provider) {
        return getRemoteServiceProviderManager().getProviderDescription(
            provider.getID());
    }

    protected boolean matchNamespace(RemoteServiceProvider provider,
        ID endpointID, ID targetID) {
        if (targetID != null) {
            return targetID.getNamespace().getName().equals(
                provider.getRemoteServiceNamespace().getName());
        }
        if (endpointID == null)
            return false;
        return endpointID.getNamespace().getName().equals(
            provider.getRemoteServiceNamespace().getName());
    }

    private boolean matchProviderID(RemoteServiceProvider provider,
        ID providerID) {
        if (providerID == null)
            return false;
        return providerID.equals(provider.getID());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.ConsumerSelector#close()
     */
    @Override
    public void close() {
    }

}
