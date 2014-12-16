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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.common.ConnectException;
import org.solmix.hola.common.identity.ID;
import org.solmix.hola.common.identity.Namespace;
import org.solmix.hola.common.identity.support.DefaultIDFactory;
import org.solmix.hola.common.security.ConnectSecurityContext;
import org.solmix.hola.osgi.rsa.AbstractSelector;
import org.solmix.hola.osgi.rsa.HolaRemoteConstants;
import org.solmix.hola.osgi.rsa.ProviderSelector;
import org.solmix.hola.rs.ProtocolException;
import org.solmix.hola.rs.RemoteServiceProvider;
import org.solmix.hola.rs.RemoteServiceProviderDescription;
import org.solmix.hola.rs.RemoteServiceProviderManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月21日
 */

public class ProviderSelectorImpl extends AbstractSelector implements
    ProviderSelector
{

    private static final Logger LOG = LoggerFactory.getLogger(ProviderSelectorImpl.class.getName());

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.ProviderSelector#selectProvider(org.osgi.framework.ServiceReference,
     *      java.util.Map, java.lang.String[], java.lang.String[],
     *      java.lang.String[])
     */
    @Override
    public RemoteServiceProvider[] selectProvider(
        ServiceReference<?> serviceReference,
        Map<String, ?> overridingProperties, String[] exportedInterfaces,
        String[] exportedConfigs, String[] serviceIntents) throws Exception {
        Collection<RemoteServiceProvider> rsProvider = selectExistingProviders(
            serviceReference, overridingProperties, exportedInterfaces,
            exportedConfigs, serviceIntents);
        if (rsProvider == null || rsProvider.size() == 0) {
            // finding/creating/configuring/connecting
            rsProvider = createProviders(serviceReference,
                overridingProperties, exportedInterfaces, exportedConfigs,
                serviceIntents);
        }
        Object target = overridingProperties.get(HolaRemoteConstants.ENDPOINT_CONNECTTARGET_ID);
        if (target != null) {

            for (RemoteServiceProvider provider : rsProvider) {
                ID targetID = null;
                if (target instanceof String)
                    targetID = DefaultIDFactory.getDefault().createID(
                        provider.getRemoteServiceNamespace(), (String) target);
                else
                    targetID = DefaultIDFactory.getDefault().createID(
                        provider.getRemoteServiceNamespace(),
                        new Object[] { target });
                Object security = overridingProperties.get("hola.endpoint.securitycontext");
                ConnectSecurityContext scontext = null;
                if (security instanceof ConnectSecurityContext)
                    scontext = (ConnectSecurityContext) security;
                provider.connect(targetID, scontext);
            }
        }
        return rsProvider.toArray(new RemoteServiceProvider[] {});
    }

    private Collection<RemoteServiceProvider> createProviders(
        ServiceReference<?> serviceReference,
        Map<String, ?> overridingProperties, String[] exportedInterfaces,
        String[] requireConfigs, String[] serviceIntents)
        throws ProtocolException {
        List<RemoteServiceProviderDescription> descs = getRemoteServiceProviderManager().getDescriptions();
        if (descs == null)
            return Collections.emptyList();
        List<RemoteServiceProvider> results = new ArrayList<RemoteServiceProvider>();
        if (requireConfigs == null || requireConfigs.length == 0) {
            createDefaultProvider(serviceReference, overridingProperties,
                exportedInterfaces, serviceIntents, results, descs);
        } else {
            for (RemoteServiceProviderDescription desc : descs) {
                RemoteServiceProvider match = createMatchingProvider(desc,
                    serviceReference, overridingProperties, exportedInterfaces,
                    requireConfigs, serviceIntents);
                if (match != null)
                    results.add(match);
            }
        }
        return results;
    }

    private RemoteServiceProvider createMatchingProvider(
        RemoteServiceProviderDescription desc,
        ServiceReference<?> serviceReference, Map<String, ?> properties,
        String[] exportedInterfaces, String[] requireConfigs,
        String[] serviceIntents) throws ProtocolException {
        if (matchSupportedConfigTypes(requireConfigs, desc)
            && matchSupportedIntents(serviceIntents, desc)) {
            return createProvider(serviceReference, properties, desc);
        }
        return null;
    }

    private RemoteServiceProvider createProvider(
        ServiceReference<?> serviceReference, Map<String, ?> properties,
        RemoteServiceProviderDescription desc) throws ProtocolException {
        String configNamePrefix = desc.getName();
        Map<String, Object> results = new HashMap<String, Object>();
        for (String origKey : properties.keySet()) {
            if (origKey.startsWith(configNamePrefix + ".")) {
                String key = origKey.substring(configNamePrefix.length() + 1);
                if (key != null)
                    results.put(key, properties.get(origKey));
            }
        }
        RemoteServiceProviderManager manager = getRemoteServiceProviderManager();

        return manager.createProvider(desc.getName(), results);
    }

    private void createDefaultProvider(ServiceReference<?> serviceReference,
        Map<String, ?> properties, String[] exportedInterfaces,
        String[] serviceIntents, List<RemoteServiceProvider> results,
        List<RemoteServiceProviderDescription> descs)
        throws ProtocolException {
        RemoteServiceProviderDescription[] defaultDescs = getDefaultDescriptions(descs);

        for (RemoteServiceProviderDescription desc : defaultDescs) {
            RemoteServiceProvider pro = createMatchingProvider(desc,
                serviceReference, properties, exportedInterfaces, null,
                serviceIntents);
            if (pro != null)
                results.add(pro);
        }

    }

    /**
     * @param descs
     * @return
     */
    private RemoteServiceProviderDescription[] getDefaultDescriptions(
        List<RemoteServiceProviderDescription> descs) {

        String[] defaultConfigTypes = getDefaultConfigTypes();
        if (defaultConfigTypes == null || defaultConfigTypes.length == 0)
            return null;
        List<RemoteServiceProviderDescription> results = new ArrayList<RemoteServiceProviderDescription>();
        for (RemoteServiceProviderDescription desc : descs) {
            String[] support = desc.getSupportedConfigs();
            if (support != null
                && matchDefaultConfigs(defaultConfigTypes, support)) {
                results.add(desc);
            }
        }
        return results.toArray(new RemoteServiceProviderDescription[] {});
    }

    /**
     * @param defaultConfigTypes
     * @param support
     * @return
     */
    private boolean matchDefaultConfigs(String[] defaultConfigTypes,
        String[] support) {
        List<String> supportedConfigTypesList = Arrays.asList(support);
        for (int i = 0; i < defaultConfigTypes.length; i++) {
            if (supportedConfigTypesList.contains(defaultConfigTypes[i]))
                return true;
        }
        return false;
    }

    /**
     * @return
     */
    public String[] getDefaultConfigTypes() {
        // XXX 通过配置实现
        return new String[]{"hola"};
    }

    private Collection<RemoteServiceProvider> selectExistingProviders(
        ServiceReference<?> serviceReference,
        Map<String, ?> overridingProperties, String[] exportedInterfaces,
        String[] exportedConfigs, String[] serviceIntents) {
        RemoteServiceProvider[] providers = getRemoteServiceProviderManager().getProviders();
        List<RemoteServiceProvider> results = new ArrayList<RemoteServiceProvider>();
        if (providers == null || providers.length == 0)
            return results;
        for (RemoteServiceProvider provider : providers) {
            RemoteServiceProviderDescription desc = getRemoteServiceProviderManager().getProviderDescription(
                provider.getID());
            if (desc == null)
                continue;
            if (!desc.isServer())
                continue;

            if (matchExistingProvider(serviceReference, overridingProperties,
                provider, desc, exportedConfigs, serviceIntents)) {
                if (LOG.isTraceEnabled())
                    LOG.trace("selectExistingHostContainers",
                        "INCLUDING containerID="
                            + provider.getID()
                            + " configs="
                            + ((exportedConfigs == null) ? "null"
                                : Arrays.asList(exportedConfigs).toString())
                            + " intents="
                            + ((serviceIntents == null) ? "null"
                                : Arrays.asList(serviceIntents).toString()));
                results.add(provider);
            } else {
                if (LOG.isTraceEnabled())
                    LOG.trace("selectExistingHostContainers",
                        "EXCLUDING containerID="
                            + provider.getID()
                            + " configs="
                            + ((exportedConfigs == null) ? "null"
                                : Arrays.asList(exportedConfigs).toString())
                            + " intents="
                            + ((serviceIntents == null) ? "null"
                                : Arrays.asList(serviceIntents).toString()));
            }
        }
        return results;
    }

    private boolean matchExistingProvider(ServiceReference<?> serviceReference,
        Map<String, ?> overridingProperties, RemoteServiceProvider provider,
        RemoteServiceProviderDescription desc, String[] exportedConfigs,
        String[] serviceIntents) {
        return matchSupportedConfigTypes(exportedConfigs, desc)
            && matchSupportedIntents(serviceIntents, desc)
            && matchID(serviceReference, overridingProperties, provider)
            && matchTargetID(serviceReference, overridingProperties, provider);
    }

    private boolean matchTargetID(ServiceReference<?> serviceReference,
        Map<String, ?> properties, RemoteServiceProvider provider) {
        String target = (String) properties.get(HolaRemoteConstants.ENDPOINT_CONNECTTARGET_ID);
        if (target == null)
            return true;
        // If a targetID is specified, make sure it either matches what the
        // container
        // is already connected to, or that we connect an unconnected container
        ID connectedID = provider.getTargetID();
        // If the container is not already connected to anything
        // then we connect it to the given target
        if (connectedID == null) {
            // connect to the target and we have a match
            try {
                connectProvider(serviceReference, properties, provider, target);
            } catch (Exception e) {
                LOG.error("doConnectContainer containerID=" + provider.getID()
                    + " target=" + target, e);
                return false;
            }
            return true;
        } else {
            ID targetID = createTargetID(provider.getRemoteNamespace(), target);
            // We check here if the currently connectedID equals the target.
            // If it does we have a match
            if (connectedID.equals(targetID))
                return true;
        }
        return false;
    }

    /**
     * @param remoteNamespace
     * @param target
     * @return
     */
    private ID createTargetID(Namespace remoteNamespace, String target) {
        return DefaultIDFactory.getDefault().createID(remoteNamespace, target);
    }

    private void connectProvider(ServiceReference<?> serviceReference,
        Map<String, ?> properties, RemoteServiceProvider provider, String target)
        throws ConnectException {
        ID targetID = DefaultIDFactory.getDefault().createID(
            provider.getRemoteNamespace(), target);
        Object context = properties.get(HolaRemoteConstants.SERVICE_EXPORTED_SECURITY_CONTEXT);
        ConnectSecurityContext connectContext = null;
        if (context instanceof ConnectSecurityContext) {
            connectContext = (ConnectSecurityContext) context;
        }
        // connect the container
        provider.connect(targetID, connectContext);

    }

    private boolean matchID(ServiceReference<?> serviceReference,
        Map<String, ?> overridingProperties, RemoteServiceProvider provider) {
        ID id = provider.getID();
        if (id == null)
            return false;
        ID requiredContainerID = (ID) overridingProperties.get(HolaRemoteConstants.SERVICE_EXPORTED_PROVIDER_ID);
        if (requiredContainerID != null) {
            return requiredContainerID.equals(id);
        }

        return false;
    }

    private boolean matchSupportedIntents(String[] serviceIntents,
        RemoteServiceProviderDescription desc) {
        if (serviceIntents == null)
            return true;

        String[] supportedIntents = desc.getSupportedIntents();

        if (supportedIntents == null)
            return false;
        List<String> supportedIntentsList = Arrays.asList(supportedIntents);
        boolean result = true;
        for (int i = 0; i < serviceIntents.length; i++)
            result = result && supportedIntentsList.contains(serviceIntents[i]);

        return result;
    }

    private boolean matchSupportedConfigTypes(String[] exportedConfigs,
        RemoteServiceProviderDescription desc) {
        if (exportedConfigs == null || exportedConfigs.length == 0)
            return true;
        String[] supportedConfigTypes = desc.getSupportedConfigs();
        if (supportedConfigTypes == null || supportedConfigTypes.length == 0)
            return false;
        List<String> supportedConfigTypesList = Arrays.asList(supportedConfigTypes);
        List<String> requiredConfigTypesList = Arrays.asList(exportedConfigs);
        // We check all of the required config types and make sure
        // that they are present in the supportedConfigTypes
        boolean result = true;
        for (Iterator<String> i = requiredConfigTypesList.iterator(); i.hasNext();)
            result &= supportedConfigTypesList.contains(i.next());
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.osgi.rsa.ProviderSelector#close()
     */
    @Override
    public void close() {
    }

}
