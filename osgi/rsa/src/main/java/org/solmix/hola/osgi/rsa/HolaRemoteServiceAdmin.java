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

package org.solmix.hola.osgi.rsa;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointPermission;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.HolaException;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.osgi.internal.Activator;
import org.solmix.hola.osgi.rsa.support.ConsumerSelectorImpl;
import org.solmix.hola.osgi.rsa.support.ProviderSelectorImpl;
import org.solmix.hola.rm.RemoteListener;
import org.solmix.hola.rs.RemoteConnectException;
import org.solmix.hola.rs.RemoteConstants;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceProvider;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.event.RemoteEvent;
import org.solmix.hola.rs.identity.RemoteServiceID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月2日
 */

public class HolaRemoteServiceAdmin implements RemoteServiceAdmin
{

    /**
     * 是否支持OSGI-RSA
     */
    public static final String RSA_SUPPORT_KEY = "org.solmix.hola.rsa";

    private static Logger LOG = LoggerFactory.getLogger(HolaRemoteServiceAdmin.class);

    private ServiceRegistration<?> eventListenerHookRegistration;

    private final Bundle clientBundle;

    private ProviderSelector defaultProviderSelector;

    private ServiceRegistration<?> providerSelectorRegistration;

    private ConsumerSelector defaultConsumerSelector;

    private ServiceRegistration<?> consumerSelectorRegistration;

    private final Collection<ExportRegistrationImpl> exportedRegistrations = new ArrayList<ExportRegistrationImpl>();

    private final Collection<ImportRegistrationImpl> importedRegistrations = new ArrayList<ImportRegistrationImpl>();

    /**
     * 为每一个Bundle创建RemoteServiceAdmin服务.
     * 
     * @param clientBundle
     */
    public HolaRemoteServiceAdmin(Bundle bundle)
    {
        this.clientBundle = bundle;
        Assert.isNotNull(this.clientBundle);
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(org.osgi.framework.Constants.SERVICE_RANKING, new Integer(
            Integer.MIN_VALUE));
        ServiceReference<?>[] providerSelectorRefers = null;
        BundleContext context = getHostBundleContext();
        try {
            providerSelectorRefers = context.getServiceReferences(
                ProviderSelector.class.getName(), null);
        } catch (InvalidSyntaxException e) {// Ignore
        }
        // 如果容器中没有其他已经注册的服务,则注册默认的服务供使用
        if (providerSelectorRefers == null
            || providerSelectorRefers.length == 0) {
            defaultProviderSelector = new ProviderSelectorImpl();
            providerSelectorRegistration = context.registerService(
                ProviderSelector.class.getName(), defaultProviderSelector,
                props);
        }
        ServiceReference<?>[] consumerSelectorRefers = null;
        try {
            consumerSelectorRefers = context.getServiceReferences(
                ConsumerSelector.class.getName(), null);
        } catch (InvalidSyntaxException e) {// Ignore
        }
        // 如果容器中没有其他已经注册的服务,则注册默认的服务供使用
        if (consumerSelectorRefers == null
            || consumerSelectorRefers.length == 0) {
            defaultConsumerSelector = new ConsumerSelectorImpl();
            consumerSelectorRegistration = context.registerService(
                ConsumerSelector.class.getName(), defaultConsumerSelector,
                props);
        }
        eventListenerHookRegistration = context.registerService(
            EventListenerHook.class.getName(), new EventListenerHooker(), null);

    }

    /**
     * {@link org.solmix.hola.osgi.topology.DefaultTopologyComponent#event(
     * ServiceEvent, Map<BundleContext, Collection<ListenerInfo>>)}监听所有注册的服务
     * 检测ServiceProperties中是否包含service.exported.interfaces参数,由此来判断是否导出为远程服务.<br>
     * 如需暴露远程服务,就调用该方法.<br>
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.RemoteServiceAdmin#exportService(org.osgi.framework.ServiceReference,
     *      java.util.Map)
     */
    @Override
    public Collection<ExportRegistration> exportService(
        final ServiceReference reference, Map<String, ?> properties) {
        if (LOG.isTraceEnabled())
            LOG.trace("serviceReference=" + reference + ",properties="
                + properties);
        @SuppressWarnings("unchecked")
        final Map<String, Object> overridingProperties = PropertiesUtil.mergeProperties(
            reference, properties == null ? Collections.EMPTY_MAP : properties);
        final String[] exportedInterfaces = PropertiesUtil.getExportedInterfaces(
            reference, overridingProperties);
        if (exportedInterfaces == null)
            throw new IllegalArgumentException(
                org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES
                    + " not set");
        // 验证接口,发布的接口必须是服务已经实现的接口
        if (!validExportedInterfaces(reference, exportedInterfaces))
            return Collections.emptyList();
        // Get optional exported configs
        String[] ecs = PropertiesUtil.getStringArrayFromPropertyValue(overridingProperties.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
        if (ecs == null) {
            ecs = PropertiesUtil.getStringArrayFromPropertyValue(reference.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
        }
        final String[] exportedConfigs = ecs;
        // Get all intents (service.intents, service.exported.intents,
        // service.exported.intents.extra)
        final String[] serviceIntents = PropertiesUtil.getServiceIntents(
            reference, overridingProperties);

        final ProviderSelector selected = getProviderSelector();
        RemoteServiceProvider[] providers = null;
        try {
            providers = AccessController.doPrivileged(new PrivilegedExceptionAction<RemoteServiceProvider[]>() {

                @Override
                public RemoteServiceProvider[] run() throws Exception {
                    return selected.selectProvider(reference,
                        overridingProperties, exportedInterfaces,
                        exportedConfigs, serviceIntents);
                }
            });
        } catch (PrivilegedActionException e) {
            // If exception, create error export registration
            ExportRegistrationImpl errorRegistration = createErrorExportRegistration(
                reference, overridingProperties,
                "Error selecting or creating host container for serviceReference="
                    + reference + " properties=" + overridingProperties,
                e.getException());
            // Add to exportedRegistrations
            synchronized (exportedRegistrations) {
                exportedRegistrations.add(errorRegistration);
            }
            // Publish export event
            publishExportEvent(errorRegistration);
            // Return collection
            Collection<ExportRegistration> result = new ArrayList<ExportRegistration>();
            result.add(errorRegistration);
            return result;
        }
        if (providers == null || providers.length == 0) {
            if (LOG.isWarnEnabled())
                LOG.warn("No remoteservice provider found,service Not EXPORT");
            return Collections.emptySet();
        }
        Collection<ExportRegistrationImpl> exportRegistrations = new ArrayList<ExportRegistrationImpl>();
        synchronized (exportedRegistrations) {
            for (RemoteServiceProvider provider : providers) {
                ExportRegistrationImpl exportRegistration = null;
                // export是幂等的,也就是export两次和export一次是一样的.
                ExportEndpoint exportEndpoint = findExistingExportEndpoint(
                    reference, provider.getID());
                // 如果找到了对应的Endpoint,直接返回
                if (exportEndpoint != null)
                    exportRegistration = new ExportRegistrationImpl(
                        exportEndpoint);
                else {
                    Map<String, Object> endpointDescriptionProperties = createExportEndpointDescriptionProperties(
                        reference, overridingProperties, exportedInterfaces,
                        serviceIntents, provider);
                    // otherwise, actually export the service to create a new
                    // ExportEndpoint and use it to create a new
                    // ExportRegistration
                    HolaEndpointDescription endpointDescription = new HolaEndpointDescription(
                        reference, endpointDescriptionProperties);

                    checkEndpointPermission(endpointDescription,
                        EndpointPermission.EXPORT);
                    try {
                        exportRegistration = exportService(reference,
                            overridingProperties, exportedInterfaces, provider,
                            endpointDescriptionProperties);
                    } catch (Exception e) {
                        exportRegistration = new ExportRegistrationImpl(e,
                            endpointDescription);
                    }
                }
                addExportRegistration(exportRegistration);
                // We add it to the results in either case
                exportRegistrations.add(exportRegistration);
            }

        }
        // publish all activeExportRegistrations
        for (ExportRegistrationImpl exportReg : exportRegistrations)
            publishExportEvent(exportReg);
        // and return
        return new ArrayList<ExportRegistration>(exportRegistrations);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.RemoteServiceAdmin#getExportedServices()
     */
    @Override
    public Collection<ExportReference> getExportedServices() {
        Collection<ExportReference> results = new ArrayList<ExportReference>();
        synchronized (exportedRegistrations) {
            if (exportedRegistrations.isEmpty())
                checkRSAReadAccess();
            for (ExportRegistration reg : exportedRegistrations) {
                ExportReference eRef = reg.getExportReference();
                if (eRef != null) {
                    try {
                        checkEndpointPermission(eRef.getExportedEndpoint(),
                            EndpointPermission.READ);
                        results.add(eRef);
                    } catch (SecurityException e) {
                        LOG.error(
                            "permission check failed for read access to endpointDescription="
                                + eRef.getExportedEndpoint(), e);
                    }
                }
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.RemoteServiceAdmin#getImportedEndpoints()
     */
    @Override
    public Collection<ImportReference> getImportedEndpoints() {
        Collection<ImportReference> results = new ArrayList<ImportReference>();
        synchronized (importedRegistrations) {
            // XXX The spec doesn't specify what is supposed to happen
            // when the registrations is empty...but the TCK test method:
            // RemoteServiceAdminSecure.testNoPermissions()
            // assumes that a SecurityException is thrown when accessed without
            // READ permission
            if (importedRegistrations.isEmpty())
                checkRSAReadAccess();
            for (ImportRegistration reg : importedRegistrations) {
                org.osgi.service.remoteserviceadmin.ImportReference iRef = reg.getImportReference();
                if (iRef != null
                    && checkEndpointPermissionRead("getImportedEndpoints",
                        iRef.getImportedEndpoint()))
                    results.add(iRef);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.service.remoteserviceadmin.RemoteServiceAdmin#importService(org.osgi.service.remoteserviceadmin.EndpointDescription)
     */
    @Override
    public ImportRegistration importService(EndpointDescription endpoint) {
        checkEndpointPermission(endpoint, EndpointPermission.IMPORT);
        final HolaEndpointDescription desc = (endpoint instanceof HolaEndpointDescription) ? (HolaEndpointDescription) endpoint
            : new HolaEndpointDescription(endpoint.getProperties());
        final ConsumerSelector consumerSelector = getConsumerSelector();
        if (consumerSelector == null) {
            LOG.error("No ConsumerSelector available.");
            return null;
        }
        // Select the rsContainer to handle the endpoint description
        RemoteServiceProvider rsProvider = null;
        try {
            rsProvider = AccessController.doPrivileged(new PrivilegedExceptionAction<RemoteServiceProvider>() {

                @Override
                public RemoteServiceProvider run() throws Exception {
                    return consumerSelector.selectConsumer(desc);
                }
            });
        } catch (PrivilegedActionException e) {
            LOG.error("Unexpected exception in selectConsumerContainer",
                e.getException());
            // As specified in section 122.5.2, return null
            return null;
        }
        // If none found, LOG an error and return null
        if (rsProvider == null) {
            LOG.error("No RemoteServiceProvider selected for endpoint=" + desc
                + ".Remote service NOT IMPORTED");
            return null;
        }
        ImportRegistrationImpl importRegistration = null;
        synchronized (importedRegistrations) {

            ImportEndpoint importEndpoint = findImportEndpoint(desc);
            if (importEndpoint != null) {
                // 已经导入幂等,即导入多次和导入一次是相等的
                importRegistration = new ImportRegistrationImpl(importEndpoint);
            } else {
                importRegistration = importService(desc, rsProvider);
            }
            addImportRegistration(importRegistration);
        }
        publishImportEvent(importRegistration);
        return importRegistration;
    }

    private boolean checkEndpointPermissionRead(
        String methodName,
        org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
        try {
            checkEndpointPermission(endpointDescription,
                EndpointPermission.READ);
            return true;
        } catch (SecurityException e) {
            LOG.error(
                "permission check failed for read access to endpointDescription="
                    + endpointDescription, e);
            return false;
        }
    }

    private void checkRSAReadAccess() {
        Map<String, Object> props = new HashMap();
        props.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
            UUID.randomUUID().toString());
        props.put(org.osgi.framework.Constants.OBJECTCLASS,
            new String[] { UUID.randomUUID().toString() });
        props.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
            UUID.randomUUID().toString());
        checkEndpointPermission(
            new org.osgi.service.remoteserviceadmin.EndpointDescription(props),
            org.osgi.service.remoteserviceadmin.EndpointPermission.READ);
    }

    /**
     * @param desc
     * @param rsProvider
     * @return
     */
    private ImportRegistrationImpl importService(HolaEndpointDescription desc,
        final RemoteServiceProvider rsProvider) {
        Collection<String> interfaces = desc.getInterfaces();
        if (DataUtils.isNullOrEmpty(interfaces)) {
            throw new IllegalArgumentException(
                "endpoint inteface must be not null or empty");
        }
        ID connectID = desc.getConnectTargetID();
        ID providerID = desc.getProviderID();
        if (connectID == null) {
            connectID = providerID;
        }
        final ID targetID = connectID;
        // 过滤服务提供者
        final ID[] idFilter = getIdFilter(desc, providerID);
        // SERVICE_ID osgi filter
        final String rsFilter = getRemoteServiceFilter(desc);
        Collection<RemoteReference<?>> rsRefs = new ArrayList<RemoteReference<?>>();
        ID rsProviderID = rsProvider.getID();
        try {
            // Get first interface name for service reference
            // lookup
            final String interf = interfaces.iterator().next();
            // Get/lookup remote service references
            RemoteReference<?>[] refs = AccessController.doPrivileged(new PrivilegedExceptionAction<RemoteReference<?>[]>() {

                @Override
                public RemoteReference<?>[] run()
                    throws ConnectException, InvalidSyntaxException,
                    RemoteConnectException {
                    return rsProvider.getRemoteServiceReferences(targetID,
                        idFilter, interf, rsFilter);
                }
            });
            if (refs == null) {
                if (LOG.isWarnEnabled())
                    LOG.warn("getRemoteServiceReferences return null for targetID="
                        + targetID
                        + ",idFilter="
                        + idFilter
                        + ",intf="
                        + interf
                        + ",rsFilter="
                        + rsFilter
                        + " on rsContainerID=" + rsProviderID);
            } else
                for (int i = 0; i < refs.length; i++)
                    rsRefs.add(refs[i]);
            // If there are several refs resulting (should not be)
            // we select the one to use
            RemoteReference<?> selectedRsReference = selectRemoteServiceReference(
                rsRefs, targetID, idFilter, interfaces, rsFilter, rsProvider);
            // If none found, we obviously can't continue
            if (selectedRsReference == null)
                throw new RemoteReferenceNotFoundException(targetID, idFilter,
                    interfaces, rsFilter);

            return new ImportRegistrationImpl(createAndRegisterProxy(desc,
                rsProvider, selectedRsReference));
        } catch (PrivilegedActionException e) {
            LOG.error("selectRemoteServiceReference returned null for rsRefs="
                + rsRefs + ",targetID=" + targetID + ",idFilter=" + idFilter
                + ",interfaces=" + interfaces + ",rsFilter=" + rsFilter
                + ",rsContainerID=" + rsProviderID, e.getException());
            return new ImportRegistrationImpl(desc, e.getException());
        } catch (Exception e) {
            LOG.error("selectRemoteServiceReference returned null for rsRefs="
                + rsRefs + ",targetID=" + targetID + ",idFilter=" + idFilter
                + ",interfaces=" + interfaces + ",rsFilter=" + rsFilter
                + ",rsContainerID=" + rsProviderID, e);
            return new ImportRegistrationImpl(desc, e);
        }
    }

    private RemoteReference<?> selectRemoteServiceReference(
        Collection<RemoteReference<?>> rsRefs, ID targetID,
        ID[] idFilter, Collection<String> interfaces, String rsFilter,
        RemoteServiceProvider rsContainer) {
        if (rsRefs.size() == 0)
            return null;
        if (rsRefs.size() > 1) {
            LOG.warn("selectRemoteServiceReference",
                "rsRefs=" + rsRefs + ",targetID=" + targetID + ",idFilter="
                    + idFilter + ",interfaces=" + interfaces + ",rsFilter="
                    + rsFilter + ",rsContainer=" + rsContainer.getID()
                    + " has " + rsRefs.size()
                    + " values.  Selecting the first element");
        }
        return rsRefs.iterator().next();
    }

    private ImportEndpoint createAndRegisterProxy(
        final HolaEndpointDescription desc,
        final RemoteServiceProvider rsProvider,
        final RemoteReference<?> selectedRsReference) {
        final BundleContext proxyServiceFactoryContext = getProxyServiceFactoryContext(desc);
        if (proxyServiceFactoryContext == null)
            throw new IllegalStateException(
                "getProxyServiceFactoryContext returned null.  Cannot register proxy service factory");
        final RemoteService rs = rsProvider.getRemoteService(selectedRsReference);
        if (rs == null)
            throw new NullPointerException(
                "getRemoteService return null for selectedRsReference="
                    + selectedRsReference);
        final Map<String, Object> proxyProperties = createProxyProperties(
            rsProvider.getID(), desc, selectedRsReference, rs);

        // sync sref props with endpoint props
        desc.setPropertiesOverrides(proxyProperties);
        final List<String> originalTypes = desc.getInterfaces();
        final List<String> asyncServiceTypes = desc.getAsyncInterfaces();

        final List<String> serviceTypes = new ArrayList<String>(originalTypes);
        if (asyncServiceTypes != null)
            for (String ast : asyncServiceTypes)
                if (ast != null && !serviceTypes.contains(ast))
                    serviceTypes.add(ast);

        ServiceRegistration<?> proxyRegistration = AccessController.doPrivileged(new PrivilegedAction<ServiceRegistration<?>>() {

            @Override
            public ServiceRegistration<?> run() {
                return proxyServiceFactoryContext.registerService(
                    serviceTypes.toArray(new String[serviceTypes.size()]),
                    createProxyServiceFactory(desc, rsProvider,
                        selectedRsReference, rs),
                    PropertiesUtil.createDictionaryFromMap(proxyProperties));
            }
        });

        return new ImportEndpoint(rsProvider.getID(), rsProvider,
            selectedRsReference, rs, new RemoteListener() {

                @Override
                public void onHandle(RemoteEvent event) {
                    if (event instanceof RemoteServiceUnregisteredEvent)
                        unimportService(event.getRemoteServiceReference().getID());
                }

            }, proxyRegistration, desc);
    }

    private Map<String,Object> createProxyProperties(ID importContainerID,
        EndpointDescription endpointDescription,
        RemoteReference<?> rsReference, RemoteService remoteService) {

        Map<String, Object> resultProperties = new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER);
        PropertiesUtil.copyNonReservedProperties(rsReference, resultProperties);
        PropertiesUtil.copyNonReservedProperties(
            endpointDescription.getProperties(), resultProperties);
        // remove OBJECTCLASS
        resultProperties.remove(RemoteConstants.OBJECTCLASS);
        // remove remote service id
        resultProperties.remove(RemoteConstants.SERVICE_ID);
        // Set intents if there are intents
        Object intentsValue = PropertiesUtil.convertToStringPlusValue(endpointDescription.getIntents());
        if (intentsValue != null)
            resultProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
                intentsValue);

        // Set service.imported to IRemoteService unless
        // SERVICE_IMPORTED_VALUETYPE is
        // set
        String serviceImportedType = (String) endpointDescription.getProperties().get(
            HolaRemoteConstants.SERVICE_IMPORTED_VALUETYPE);
        if (serviceImportedType == null
            || serviceImportedType.equals(RemoteService.class.getName()))
            resultProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
                remoteService);
        else
            resultProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
                new Boolean(true));

        String[] exporterSupportedConfigs = (String[]) endpointDescription.getProperties().get(
            org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
        String[] importedConfigs = getImportedConfigs(importContainerID,
            exporterSupportedConfigs);
        // Set service.imported.configs
        resultProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
            importedConfigs);

        // Set endpoint.id and endpoint.service.id
        String endpointId = endpointDescription.getId();
        resultProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
            endpointId);
        Long endpointServiceId = new Long(endpointDescription.getServiceId());
        resultProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
            endpointServiceId);

        return resultProperties;
    }

    private void unimportService(RemoteServiceID remoteServiceID) {
        List<ImportRegistration> removedRegistrations = new ArrayList<ImportRegistration>();
        synchronized (importedRegistrations) {
            for (Iterator<ImportRegistrationImpl> i = importedRegistrations.iterator(); i.hasNext();) {
                ImportRegistrationImpl importRegistration = i.next();
                if (importRegistration != null
                    && importRegistration.match(remoteServiceID))
                    removedRegistrations.add(importRegistration);
            }
        }
        // Now close all of them
        for (ImportRegistration removedReg : removedRegistrations) {
            LOG.trace("closing importRegistration=" + removedReg);
            removedReg.close();
        }
    }

    /**
     * @param desc
     * @param rsProvider
     * @param selectedRsReference
     * @param rs
     * @return
     */
    protected ProxyServiceFactory createProxyServiceFactory(
        HolaEndpointDescription desc, RemoteServiceProvider rsProvider,
        RemoteReference<?> selectedRsReference, RemoteService rs) {
        return new ProxyServiceFactory(desc.getInterfaceVersions(), rsProvider,
            selectedRsReference, rs);
    }

    /**
     * @param desc
     * @return
     */
    private BundleContext getProxyServiceFactoryContext(
        HolaEndpointDescription desc) {
        Activator a = Activator.getDefault();
        return a.getProxyServiceFactoryBundleContext();
    }

    /**
     * 根据SERVICE_ID生成OSGI filter.
     * 
     * @param desc
     * @return
     */
    private String getRemoteServiceFilter(HolaEndpointDescription endpoint) {
        long rsId = 0;
        // if the ECF remote service id is present in properties, allow it to
        // override
        Long l = endpoint.getRemoteServiceId();
        if (l != null)
            rsId = l.longValue();
        // if rsId is still zero, use the endpoint.service.id from
        // endpoint description
        if (rsId == 0)
            rsId = endpoint.getServiceId();
        // If it's *still* zero, then just use the raw filter
        if (rsId == 0) {
            // It's not known...so we just return the 'raw' remote service
            // filter
            return endpoint.getRemoteServiceFilter();
        } else {
            String edRsFilter = endpoint.getRemoteServiceFilter();
            // It's a real remote service id...so we return
            StringBuffer result = new StringBuffer("(&(").append(
                RemoteConstants.SERVICE_ID).append("=").append(rsId).append(")");
            if (edRsFilter != null)
                result.append(edRsFilter);
            result.append(")");
            return result.toString();
        }
    }

    /**
     * @param desc
     * @param providerID
     * @return
     */
    private ID[] getIdFilter(HolaEndpointDescription desc, ID providerID) {
        ID[] idFilter = desc.getIDFilter();
        // If it is null,
        return (idFilter == null) ? new ID[] { providerID } : idFilter;
    }

    /**
     * @param importRegistration
     */
    private void addImportRegistration(ImportRegistrationImpl importRegistration) {
        synchronized (importedRegistrations) {
            importedRegistrations.add(importRegistration);
        }
    }

    /**
     * @param desc
     * @return
     */
    private ImportEndpoint findImportEndpoint(HolaEndpointDescription desc) {
        for (ImportRegistrationImpl reg : importedRegistrations) {
            ImportEndpoint endpoint = reg.getImportEndpoint(desc);
            if (endpoint != null)
                return endpoint;
        }
        return null;
    }

    /**
     * 
     */
    public void close() {
        synchronized (remoteServiceAdminListenerTrackerLock) {
            if (remoteServiceAdminListenerTracker != null) {
                remoteServiceAdminListenerTracker.close();
                remoteServiceAdminListenerTracker = null;
            }
        }
        synchronized (eventAdminTrackerLock) {
            if (eventAdminTracker != null) {
                eventAdminTracker.close();
                eventAdminTracker = null;
            }
        }
        if (defaultConsumerSelector != null) {
            defaultConsumerSelector.close();
            defaultConsumerSelector = null;
        }
        if (defaultProviderSelector != null) {
            defaultProviderSelector.close();
            defaultProviderSelector = null;
        }
        if (consumerSelectorRegistration != null) {
            consumerSelectorRegistration.unregister();
            consumerSelectorRegistration = null;
        }
        if (providerSelectorRegistration != null) {
            providerSelectorRegistration.unregister();
            providerSelectorRegistration = null;
        }
        if (eventListenerHookRegistration != null) {
            eventListenerHookRegistration.unregister();
            eventListenerHookRegistration = null;
        }
        synchronized (consumerSelectorTrackerLock) {
            if (consumerSelectorTracker != null) {
                consumerSelectorTracker.close();
                consumerSelectorTracker = null;
            }
        }
        synchronized (providerSelectorTrackerLock) {
            if (providerSelectorTracker != null) {
                providerSelectorTracker.close();
                providerSelectorTracker = null;
            }
        }

    }

    private void handleServiceUnregistering(ServiceReference<?> serviceReference) {
        List<ExportRegistrationImpl> ers = getExportedRegistrations();
        for (ExportRegistrationImpl exportedRegistration : ers) {
            if (exportedRegistration.match(serviceReference)) {
                if (LOG.isTraceEnabled())
                    LOG.trace("closing exportRegistration for serviceReference="
                        + serviceReference);
                exportedRegistration.close();
            }
        }
    }

    List<ExportRegistrationImpl> getExportedRegistrations() {
        synchronized (exportedRegistrations) {
            return new ArrayList<ExportRegistrationImpl>(exportedRegistrations);
        }
    }

    private final Object consumerSelectorTrackerLock = new Object();

    private ServiceTracker<ConsumerSelector, ConsumerSelector> consumerSelectorTracker;

    private final Object providerSelectorTrackerLock = new Object();

    private ServiceTracker<ProviderSelector, ProviderSelector> providerSelectorTracker;

    /**
     * Tracked ProviderSelector in OSGI container
     * 
     * @return ProviderSelector
     */
    protected ProviderSelector getProviderSelector() {
        return AccessController.doPrivileged(new PrivilegedAction<ProviderSelector>() {

            @Override
            public ProviderSelector run() {
                synchronized (providerSelectorTrackerLock) {
                    if (providerSelectorTracker == null) {
                        providerSelectorTracker = new ServiceTracker<ProviderSelector, ProviderSelector>(
                            getHostBundleContext(),
                            ProviderSelector.class.getName(), null);
                        providerSelectorTracker.open();
                    }
                }
                return providerSelectorTracker.getService();
            }
        });
    }

    /**
     * Tracked consumerSelector in OSGI container
     * 
     * @return ConsumerSelector
     */
    protected ConsumerSelector getConsumerSelector() {
        return AccessController.doPrivileged(new PrivilegedAction<ConsumerSelector>() {

            @Override
            public ConsumerSelector run() {
                synchronized (consumerSelectorTrackerLock) {
                    if (consumerSelectorTracker == null) {
                        consumerSelectorTracker = new ServiceTracker<ConsumerSelector, ConsumerSelector>(
                            getHostBundleContext(),
                            ConsumerSelector.class.getName(), null);
                        consumerSelectorTracker.open();
                    }
                }
                return consumerSelectorTracker.getService();
            }
        });
    }

    private boolean validExportedInterfaces(
        ServiceReference<?> serviceReference, String[] exportedInterfaces) {
        if (exportedInterfaces == null || exportedInterfaces.length == 0)
            return false;
        List<String> objectClassList = Arrays.asList((String[]) serviceReference.getProperty(org.osgi.framework.Constants.OBJECTCLASS));
        for (int i = 0; i < exportedInterfaces.length; i++)
            if (!objectClassList.contains(exportedInterfaces[i]))
                return false;
        return true;
    }

    private void checkEndpointPermission(
        EndpointDescription endpointDescription, String permissionType)
        throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null)
            return;
        sm.checkPermission(new EndpointPermission(endpointDescription,
            Activator.getDefault().getFrameworkUUID(), permissionType));
    }

    private String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf(".");
        if (lastDotIndex == -1)
            return "";
        return className.substring(0, lastDotIndex);
    }

    private Version getPackageVersion(
        final ServiceReference<?> serviceReference, String serviceInterface,
        String packageName) {
        Object service = AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                return getHostBundleContext().getService(serviceReference);
            }
        });
        if (service == null)
            return null;
        Class<?>[] interfaceClasses = service.getClass().getInterfaces();
        if (interfaceClasses == null)
            return null;
        Class<?> interfaceClass = null;
        for (int i = 0; i < interfaceClasses.length; i++)
            if (interfaceClasses[i].getName().equals(serviceInterface))
                interfaceClass = interfaceClasses[i];
        if (interfaceClass == null)
            return null;
        Bundle providingBundle = FrameworkUtil.getBundle(interfaceClass);
        if (providingBundle == null)
            return null;
        return getVersionForPackage(providingBundle, packageName);
    }

    private Version getVersionForPackage(final Bundle providingBundle,
        String packageName) {
        Version result = null;
        BundleRevision providingBundleRevision = AccessController.doPrivileged(new PrivilegedAction<BundleRevision>() {

            @Override
            public BundleRevision run() {
                return providingBundle.adapt(BundleRevision.class);
            }
        });
        if (providingBundleRevision == null)
            return null;
        List<BundleCapability> providerCapabilities = providingBundleRevision.getDeclaredCapabilities(BundleRevision.PACKAGE_NAMESPACE);
        for (BundleCapability c : providerCapabilities) {
            result = getVersionForMatchingCapability(packageName, c);
            if (result != null)
                return result;
        }
        return result;
    }

    private Version getVersionForMatchingCapability(String packageName,
        BundleCapability capability) {
        // If it's a package namespace (Import-Package)
        Map<String, Object> attributes = capability.getAttributes();
        // Then we get the package attribute
        String p = (String) attributes.get(BundleRevision.PACKAGE_NAMESPACE);
        // And compare it to the package name
        if (p != null && packageName.equals(p))
            return (Version) attributes.get(Constants.VERSION_ATTRIBUTE);
        return null;
    }

    protected ExportRegistrationImpl exportService(
        final ServiceReference<?> reference,
        Map<String, Object> overridingProperties, String[] exportedInterfaces,
        RemoteServiceProvider provider,
        Map<String, Object> endpointDescriptionProperties) {
        Map<String, Object> remoteServiceProperties = new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER);
        PropertiesUtil.copyNonReservedProperties(reference,
            remoteServiceProperties);
        PropertiesUtil.copyNonReservedProperties(overridingProperties,
            remoteServiceProperties);
        Object service = AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                return getClientBundleContext().getService(reference);
            }
        });
        RemoteRegistration<?> registration = provider.registerRemoteService(
            exportedInterfaces, service, remoteServiceProperties);
        // endpointDescriptionProperties.put(RemoteConstants.SERVICE_ID,
        // registration.getRelativeId)

        return new ExportRegistrationImpl(new ExportEndpoint(reference,
            new HolaEndpointDescription(reference,
                endpointDescriptionProperties), registration));
    }

    private void addExportRegistration(ExportRegistrationImpl exportRegistration) {
        synchronized (exportedRegistrations) {
            exportedRegistrations.add(exportRegistration);
        }
    }

    /**
     * 已经暴露的服务中是否已经包含了Endpoint
     * 
     * @param serviceReference
     * @param ProviderID
     * @return
     */
    private ExportEndpoint findExistingExportEndpoint(
        ServiceReference<?> serviceReference, ID providerID) {
        for (ExportRegistrationImpl eReg : exportedRegistrations) {
            ExportEndpoint exportEndpoint = eReg.getExportEndpoint(
                serviceReference, providerID);
            if (exportEndpoint != null)
                return exportEndpoint;
        }
        return null;
    }

    /**
     * @param reference
     * @param overridingProperties
     * @param string
     * @param exception
     * @return
     */
    private ExportRegistrationImpl createErrorExportRegistration(
        ServiceReference<?> reference,
        Map<String, Object> overridingProperties, String string,
        Exception exception) {
        overridingProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
            "noendpoint");
        overridingProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
            "noconfig");
        return new ExportRegistrationImpl(exception, new EndpointDescription(
            reference, overridingProperties));
    }

    private BundleContext getHostBundleContext() {
        return Activator.getContext();
    }

    /**
     * RemoteServiceAdmin
     * 宿主BundleContext,每一个Bundle创建的RSA都通过该BundleContext选择RemoteServiceProvider
     * 
     * @return
     */
    private BundleContext getClientBundleContext() {
        return clientBundle.getBundleContext();
    }

    /**
     * RemoteServiceAdmin
     * 宿主Bundle,每一个Bundle创建的RSA都通过该Bundle选择RemoteServiceProvider
     * 
     * @return
     */
    private Bundle getHostBundle() {
        return getHostBundleContext().getBundle();
    }

    private boolean removeExportRegistration(
        ExportRegistrationImpl exportRegistration) {
        synchronized (exportedRegistrations) {
            return exportedRegistrations.remove(exportRegistration);
        }
    }

    private boolean removeImportRegistration(
        ImportRegistrationImpl importRegistration) {
        synchronized (importedRegistrations) {
            return importedRegistrations.remove(importRegistration);
        }
    }

    List<ImportRegistrationImpl> getImportedRegistrations() {
        synchronized (importedRegistrations) {
            return new ArrayList<ImportRegistrationImpl>(importedRegistrations);
        }
    }

    private Map<String, Object> createExportEndpointDescriptionProperties(
        ServiceReference<?> reference,
        Map<String, Object> overridingProperties, String[] exportedInterfaces,
        String[] serviceIntents, RemoteServiceProvider provider) {
        ID providerId = provider.getID();
        Map<String, Object> endpointDescriptionProperties = new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER);
        // OSGi properties
        // OBJECTCLASS set to exportedInterfaces
        endpointDescriptionProperties.put(
            org.osgi.framework.Constants.OBJECTCLASS, exportedInterfaces);
        // Service interface versions
        for (int i = 0; i < exportedInterfaces.length; i++) {
            String packageName = getPackageName(exportedInterfaces[i]);
            String packageVersionKey = org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_
                + packageName;
            // If it's pre-set...by registration or by overridingProperties,
            // then use that value
            String packageVersion = (String) PropertiesUtil.getPropertyValue(
                reference, overridingProperties, packageVersionKey);
            if (packageVersion == null) {
                Version version = getPackageVersion(reference,
                    exportedInterfaces[i], packageName);
                if (version != null && !version.equals(Version.emptyVersion))
                    packageVersion = version.toString();
            }
            // Only set the package version if we have a non-null value
            if (packageVersion != null)
                endpointDescriptionProperties.put(packageVersionKey,
                    packageVersion);
        }
        // ENDPOINT_ID
        String endpointId = (String) PropertiesUtil.getPropertyValue(reference,
            overridingProperties,
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
        if (endpointId == null)
            endpointId = UUID.randomUUID().toString();
        endpointDescriptionProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
            endpointId);

        // ECF ENDPOINT ID
        String ecfEndpointId = (String) PropertiesUtil.getPropertyValue(
            reference, overridingProperties, HolaRemoteConstants.ENDPOINT_ID);
        if (ecfEndpointId == null)
            ecfEndpointId = providerId.getName();
        endpointDescriptionProperties.put(HolaRemoteConstants.ENDPOINT_ID,
            ecfEndpointId);

        // ENDPOINT_SERVICE_ID
        // This is always set to the value from serviceReference as per 122.5.1
        Long serviceId = (Long) reference.getProperty(org.osgi.framework.Constants.SERVICE_ID);
        endpointDescriptionProperties.put(
            org.osgi.framework.Constants.SERVICE_ID, serviceId);

        // ENDPOINT_FRAMEWORK_ID
        String frameworkId = (String) PropertiesUtil.getPropertyValue(
            reference,
            overridingProperties,
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
        if (frameworkId == null)
            frameworkId = Activator.getDefault().getFrameworkUUID();
        endpointDescriptionProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
            frameworkId);

        // REMOTE_CONFIGS_SUPPORTED
        String[] remoteConfigsSupported = getSupportedConfigs(provider.getID());
        if (remoteConfigsSupported != null)
            endpointDescriptionProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
                remoteConfigsSupported);
        // SERVICE_IMPORTED_CONFIGS...set to constant value for all ECF
        // providers
        // supported (which is computed
        // for the exporting ECF container
        endpointDescriptionProperties.put(
            org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
            remoteConfigsSupported);

        // SERVICE_INTENTS
        Object intents = PropertiesUtil.getPropertyValue(null,
            overridingProperties,
            org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS);
        if (intents == null)
            intents = serviceIntents;
        if (intents != null)
            endpointDescriptionProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
                intents);

        // REMOTE_INTENTS_SUPPORTED
        String[] remoteIntentsSupported = getSupportedIntents(provider.getID());
        if (remoteIntentsSupported != null)
            endpointDescriptionProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
                remoteIntentsSupported);

        // ECF properties
        // ID namespace
        String idNamespace = provider.getID().getNamespace().getName();
        endpointDescriptionProperties.put(
            HolaRemoteConstants.ENDPOINT_NAMESPACE, idNamespace);

        // timestamp
        endpointDescriptionProperties.put(
            HolaRemoteConstants.ENDPOINT_TIMESTAMP, System.currentTimeMillis());

        // ENDPOINT_CONNECTTARGET_ID
        String connectTarget = (String) PropertiesUtil.getPropertyValue(
            reference, overridingProperties,
            HolaRemoteConstants.ENDPOINT_CONNECTTARGET_ID);
        if (connectTarget == null && isClient(provider)) {
            ID connectedID = provider.getTargetID();
            if (connectedID != null && !connectedID.equals(providerId))
                connectTarget = connectedID.getName();
        }
        if (connectTarget != null)
            endpointDescriptionProperties.put(
                HolaRemoteConstants.ENDPOINT_CONNECTTARGET_ID, connectTarget);

        // ENDPOINT_IDFILTER_IDS
        String[] idFilter = (String[]) PropertiesUtil.getPropertyValue(
            reference, overridingProperties,
            HolaRemoteConstants.ENDPOINT_IDFILTER_IDS);
        if (idFilter != null && idFilter.length > 0)
            endpointDescriptionProperties.put(
                HolaRemoteConstants.ENDPOINT_IDFILTER_IDS, idFilter);

        // ENDPOINT_REMOTESERVICE_FILTER
        String rsFilter = (String) PropertiesUtil.getPropertyValue(reference,
            overridingProperties,
            HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
        if (rsFilter != null)
            endpointDescriptionProperties.put(
                HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER, rsFilter);

        // Finally, copy all non-reserved properties
        return PropertiesUtil.copyNonReservedProperties(overridingProperties,
            endpointDescriptionProperties);
    }

    /**
     * @param provider
     * @return
     */
    private boolean isClient(RemoteServiceProvider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param id
     * @return
     */
    private String[] getSupportedIntents(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param id
     * @return
     */
    private String[] getSupportedConfigs(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    private String[] getImportedConfigs(ID containerID,
        String[] exporterSupportedConfigs) {
        return null;
    }

    /**
     * 发布 export event
     * 
     * @param exportRegistration
     */
    private void publishExportEvent(ExportRegistrationImpl exportRegistration) {
        Throwable exception = exportRegistration.getException();
        ExportReference exportReference = (exception == null) ? exportRegistration.getExportReference()
            : null;
        EndpointDescription endpointDescription = exportRegistration.getEndpointDescription();
        RemoteServiceAdminEvent rsaEvent = new HolaRemoteServiceAdminEvent(
            exportRegistration.getProviderID(),
            (exception == null) ? RemoteServiceAdminEvent.EXPORT_REGISTRATION
                : RemoteServiceAdminEvent.EXPORT_ERROR, getHostBundle(),
            exportReference, exception, endpointDescription);
        publishEvent(rsaEvent, endpointDescription);

    }

    /**
     * 发布 import event
     * 
     * @param importRegistration
     */
    private void publishImportEvent(ImportRegistrationImpl importRegistration) {
        Throwable exception = importRegistration.getException();
        org.osgi.service.remoteserviceadmin.ImportReference importReference = (exception == null) ? importRegistration.getImportReference()
            : null;
        EndpointDescription endpointDescription = importRegistration.getEndpointDescription();
        RemoteServiceAdminEvent rsaEvent = new HolaRemoteServiceAdminEvent(
            importRegistration.getProviderID(),
            (exception == null) ? RemoteServiceAdminEvent.IMPORT_REGISTRATION
                : RemoteServiceAdminEvent.IMPORT_ERROR, getHostBundle(),
            importReference, exception, endpointDescription);
        publishEvent(rsaEvent, endpointDescription);

    }

    /**
     * asynchronously publish remote service event to OSGI EventAdmin
     */
    public void publishEvent(RemoteServiceAdminEvent event,
        EndpointDescription endpointDescription) {
        EndpointPermission perm = new EndpointPermission(endpointDescription,
            Activator.getDefault().getFrameworkUUID(), EndpointPermission.READ);
        RemoteServiceAdminListener[] listeners = getListeners(perm);
        if (listeners != null)
            for (int i = 0; i < listeners.length; i++)
                listeners[i].remoteAdminEvent(event);
        // Now also post the event asynchronously to EventAdmin
        postEvent(event, endpointDescription);

    }

    /**
     * @param event
     * @param endpointDescription
     */
    private void postEvent(RemoteServiceAdminEvent event,
        EndpointDescription endpointDescription) {
        int eventType = event.getType();
        String eventTypeName = null;
        String registrationTypeName = null;
        switch (eventType) {
            case (RemoteServiceAdminEvent.EXPORT_REGISTRATION):
                eventTypeName = "EXPORT_REGISTRATION";
                registrationTypeName = "export.registration";
                break;
            case (RemoteServiceAdminEvent.EXPORT_ERROR):
                eventTypeName = "EXPORT_ERROR";
                registrationTypeName = "export.error";
                break;
            case (RemoteServiceAdminEvent.EXPORT_UNREGISTRATION):
                eventTypeName = "EXPORT_UNREGISTRATION";
                registrationTypeName = "export.registration";
                break;
            case (RemoteServiceAdminEvent.EXPORT_WARNING):
                eventTypeName = "EXPORT_WARNING";
                registrationTypeName = "export.registration";
                break;
            case (RemoteServiceAdminEvent.IMPORT_REGISTRATION):
                eventTypeName = "IMPORT_REGISTRATION";
                registrationTypeName = "import.registration";
                break;
            case (RemoteServiceAdminEvent.IMPORT_ERROR):
                eventTypeName = "IMPORT_ERROR";
                registrationTypeName = "import.registration";
                break;
            case (RemoteServiceAdminEvent.IMPORT_UNREGISTRATION):
                eventTypeName = "IMPORT_UNREGISTRATION";
                registrationTypeName = "import.registration";
                break;
            case (RemoteServiceAdminEvent.IMPORT_WARNING):
                eventTypeName = "IMPORT_WARNING";
                registrationTypeName = "import.registration";
                break;
        }
        if (eventTypeName == null) {
            LOG.error("Event Type :" + eventType + " not supported!");
            return;
        }
        final String topic = "org/osgi/service/remoteserviceadmin/"
            + eventTypeName;
        Bundle bundle = getHostBundle();
        if (bundle == null) {
            LOG.error("RemoteServiceAdmin Bundle is null");
            return;
        }
        final Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
        eventProperties.put("clientBundle", bundle);
        eventProperties.put("clientBundle.id", new Long(bundle.getBundleId()));
        eventProperties.put("clientBundle.symbolicname",
            bundle.getSymbolicName());
        eventProperties.put("clientBundle.version", bundle.getVersion());
        List<String> result = new ArrayList<String>();
        Map<X509Certificate, List<X509Certificate>> signers1 = this.clientBundle.getSignerCertificates(Bundle.SIGNERS_ALL);
        for (Iterator<X509Certificate> i = signers1.keySet().iterator(); i.hasNext();)
            result.add(i.next().toString());
        String[] signers = result.toArray(new String[result.size()]);
        if (signers != null && signers.length > 0)
            eventProperties.put("clientBundle.signer", signers);
        Throwable t = event.getException();
        if (t != null)
            eventProperties.put("cause", t);
        long serviceId = endpointDescription.getServiceId();
        if (serviceId != 0)
            eventProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
                new Long(serviceId));
        String frameworkUUID = endpointDescription.getFrameworkUUID();
        if (frameworkUUID != null)
            eventProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
                frameworkUUID);
        String endpointId = endpointDescription.getId();
        if (endpointId != null)
            eventProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
                endpointId);
        List<String> interfaces = endpointDescription.getInterfaces();
        if (interfaces != null && interfaces.size() > 0)
            eventProperties.put(org.osgi.framework.Constants.OBJECTCLASS,
                interfaces.toArray(new String[interfaces.size()]));
        List<String> importedConfigs = endpointDescription.getConfigurationTypes();
        if (importedConfigs != null && importedConfigs.size() > 0)
            eventProperties.put(
                org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
                importedConfigs.toArray(new String[importedConfigs.size()]));
        eventProperties.put("timestamp", new Long(new Date().getTime()));
        eventProperties.put("event", event);
        if (registrationTypeName != null)
            eventProperties.put(registrationTypeName, endpointDescription);
        final EventAdmin eventAdmin = AccessController.doPrivileged(new PrivilegedAction<EventAdmin>() {

            @Override
            public EventAdmin run() {
                synchronized (eventAdminTrackerLock) {
                    eventAdminTracker = new ServiceTracker<EventAdmin, EventAdmin>(
                        getHostBundleContext(), EventAdmin.class.getName(),
                        null);
                    eventAdminTracker.open();
                }
                return eventAdminTracker.getService();
            }
        });
        if (eventAdmin == null) {
            LOG.error("No EventAdmin service available to send eventTopic="
                + topic + " eventProperties=" + eventProperties);
            return;
        }
        // post via event admin
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                eventAdmin.postEvent(new Event(topic, eventProperties));
                return null;
            }
        });
    }

    private ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker;

    private final Object eventAdminTrackerLock = new Object();

    private final Object remoteServiceAdminListenerTrackerLock = new Object();

    private ServiceTracker<RemoteServiceAdminListener, RemoteServiceAdminListener> remoteServiceAdminListenerTracker;

    private RemoteServiceAdminListener[] getListeners(EndpointPermission perm) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                synchronized (remoteServiceAdminListenerTrackerLock) {
                    if (remoteServiceAdminListenerTracker == null) {
                        remoteServiceAdminListenerTracker = new ServiceTracker<RemoteServiceAdminListener, RemoteServiceAdminListener>(
                            getHostBundleContext(),
                            RemoteServiceAdminListener.class.getName(), null);
                        remoteServiceAdminListenerTracker.open();
                    }
                    return null;
                }
            }
        });
        ServiceReference<RemoteServiceAdminListener>[] unfilteredRefs = remoteServiceAdminListenerTracker.getServiceReferences();
        if (unfilteredRefs == null)
            return null;
        // Filter by Bundle.hasPermission
        List<ServiceReference<RemoteServiceAdminListener>> filteredRefs = new ArrayList<ServiceReference<RemoteServiceAdminListener>>();
        for (ServiceReference<RemoteServiceAdminListener> ref : unfilteredRefs)
            if (perm == null || ref.getBundle().hasPermission(perm))
                filteredRefs.add(ref);
        List<RemoteServiceAdminListener> results = new ArrayList<RemoteServiceAdminListener>();
        for (final ServiceReference<RemoteServiceAdminListener> ref : filteredRefs) {
            RemoteServiceAdminListener l = AccessController.doPrivileged(new PrivilegedAction<RemoteServiceAdminListener>() {

                @Override
                public RemoteServiceAdminListener run() {
                    return remoteServiceAdminListenerTracker.getService(ref);
                }
            });
            if (l != null)
                results.add(l);
        }
        return results.toArray(new RemoteServiceAdminListener[results.size()]);
    }

    class ProxyServiceFactory implements ServiceFactory<Object>
    {

        private final RemoteServiceProvider rsProvider;

        private final RemoteReference<?> rsReference;

        private final RemoteService remoteService;

        private final Map<String, Version> interfaceVersions;

        private long remoteProxyCount = 0L;

        public ProxyServiceFactory(Map<String, Version> interfaceVersions,
            RemoteServiceProvider rsProvider,
            RemoteReference<?> rsReference, RemoteService remoteService)
        {
            this.rsProvider = rsProvider;
            this.rsReference = rsReference;
            this.interfaceVersions = interfaceVersions;
            this.remoteService = remoteService;
        }

        @Override
        public Object getService(Bundle bundle,
            ServiceRegistration<Object> registration) {
            Object proxy = createProxy(bundle, registration.getReference(),
                remoteService, interfaceVersions);
            remoteProxyCount++;
            return proxy;
        }

        @Override
        public void ungetService(Bundle bundle,
            ServiceRegistration<Object> registration, Object service) {
            if (remoteProxyCount == 1L)
                rsProvider.ungetRemoteService(rsReference);
            ungetProxyClassLoader(bundle);
        }
    }

    private Object createProxy(Bundle requestBundle,
        ServiceReference<Object> reference, RemoteService remoteService,
        Map<String, Version> interfaceVersions) {
        String bundleSymbolicName = requestBundle.getSymbolicName();
        String[] serviceClassnames = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        Collection<Class<?>> serviceInterfaceClasses = loadServiceInterfacesViaBundle(
            requestBundle, serviceClassnames);
        if (serviceInterfaceClasses.size() < 1)
            throw new RuntimeException(
                "ProxyServiceFactory cannot load any serviceInterfaces="
                    + serviceInterfaceClasses + " for serviceReference="
                    + reference + " via clientBundle=" + bundleSymbolicName);

        // Now verify that the classes are of valid versions
        if (!verifyServiceInterfaceVersionsForProxy(requestBundle,
            serviceInterfaceClasses, interfaceVersions))
            return null;

        // Now create/get class loader for proxy. This will typically
        // be an instance of ProxyClassLoader
        ClassLoader cl = getProxyClassLoader(requestBundle);
        try {
            return remoteService.getProxy(
                cl,
                serviceInterfaceClasses.toArray(new Class[serviceInterfaceClasses.size()]));
        } catch (HolaException e) {
            throw new ServiceException(
                "ProxyServiceFactory cannot create proxy for clientBundle="
                    + bundleSymbolicName + " from serviceReference="
                    + reference, e);
        }
    }

    private final Map<Bundle, ProxyClassLoader> proxyClassLoaders = new HashMap<Bundle, ProxyClassLoader>();

    private ClassLoader getProxyClassLoader(Bundle bundle) {
        ProxyClassLoader proxyClassLoaderForBundle = null;
        synchronized (proxyClassLoaders) {
            proxyClassLoaderForBundle = proxyClassLoaders.get(bundle);
            if (proxyClassLoaderForBundle == null) {
                proxyClassLoaderForBundle = new ProxyClassLoader(bundle);
                proxyClassLoaders.put(bundle, proxyClassLoaderForBundle);
            } else
                proxyClassLoaderForBundle.addServiceUseCount();
        }
        return proxyClassLoaderForBundle;
    }

    private void ungetProxyClassLoader(Bundle bundle) {
        synchronized (proxyClassLoaders) {
            ProxyClassLoader proxyClassLoaderForBundle = proxyClassLoaders.get(bundle);
            if (proxyClassLoaderForBundle != null) {
                int useCount = proxyClassLoaderForBundle.getServiceUseCount();
                if (useCount == 0)
                    proxyClassLoaders.remove(bundle);
                else
                    proxyClassLoaderForBundle.removeServiceUseCount();
            }
        }

    }

    private boolean verifyServiceInterfaceVersionsForProxy(Bundle bundle,
        Collection<Class<?>> classes, Map<String, Version> interfaceVersions) {
        // For all service interface classes
        boolean result = true;
        for (Class<?> clazz : classes) {
            String className = clazz.getName();
            String packageName = getPackageName(className);
            // Now get remoteVersion, localVersion and do compare via package
            // version comparator service
            Version remoteVersion = interfaceVersions.get(className);
            Version localVersion = getPackageVersionViaRequestingBundle(
                packageName, bundle, remoteVersion);
            if (comparePackageVersions(packageName, remoteVersion, localVersion)) {
                LOG.error("Failed version check for proxy creation.  clientBundle="
                    + this.clientBundle
                    + " interfaceType="
                    + className
                    + " remoteVersion="
                    + remoteVersion
                    + " localVersion="
                    + localVersion);
                result = false;
            }
        }
        return result;
    }

    private boolean comparePackageVersions(String packageName,
        Version remoteVersion, Version localVersion) throws RuntimeException {

        LOG.trace("packageName=" + packageName + ",remoteVersion="
            + remoteVersion + ",localVersion=" + localVersion);

        // If no remote version info, then set it to empty
        if (remoteVersion == null)
            remoteVersion = Version.emptyVersion;
        if (localVersion == null)
            localVersion = Version.emptyVersion;

        // By default we do strict comparison of remote with local...they must
        // be exactly the same, or we thrown a runtime exception
        int compareResult = localVersion.compareTo(remoteVersion);
        // Now check compare result, and throw exception to fail compare
        return (compareResult != 0);
    }

    private Version getPackageVersionViaRequestingBundle(String packageName,
        final Bundle requestingBundle, Version remoteVersion) {
        Version result = null;
        // First check the requesting bundle for the desired export package
        // capability
        BundleRevision requestingBundleRevision = AccessController.doPrivileged(new PrivilegedAction<BundleRevision>() {

            @Override
            public BundleRevision run() {
                return requestingBundle.adapt(BundleRevision.class);
            }
        });
        if (requestingBundleRevision != null) {
            List<BundleCapability> requestingBundleCapabilities = requestingBundleRevision.getDeclaredCapabilities(BundleRevision.PACKAGE_NAMESPACE);
            for (BundleCapability requestingBundleCapability : requestingBundleCapabilities) {
                Version candidate = getVersionForMatchingCapability(
                    packageName, requestingBundleCapability);
                // If found, set our result
                if (candidate != null) {
                    if (remoteVersion != null
                        && candidate.equals(remoteVersion))
                        return candidate;
                    result = candidate;
                }
            }
        }
        // If not found in requestingBundle export package, then
        // look in exported package that are wired to the requesting bundle
        if (result == null) {
            // look for wired exported packages
            BundleWiring requestingBundleWiring = requestingBundle.adapt(BundleWiring.class);
            if (requestingBundleWiring != null) {
                result = getPackageVersionForMatchingWire(
                    packageName,
                    requestingBundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE),
                    BundleRevision.PACKAGE_NAMESPACE);
                // If not found in wired exported packages, then look
                // in wired require bundles
                if (result == null)
                    result = getPackageVersionForMatchingWire(
                        packageName,
                        requestingBundleWiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE),
                        BundleRevision.BUNDLE_NAMESPACE);
            }
        }
        return result;
    }

    private Version getPackageVersionForMatchingWire(String packageName,
        List<BundleWire> bundleWires, String namespace) {
        Version result = null;
        for (BundleWire wire : bundleWires) {
            if (namespace.equals(BundleRevision.PACKAGE_NAMESPACE))
                result = getVersionForMatchingCapability(packageName,
                    wire.getCapability());
            else if (namespace.equals(BundleRevision.BUNDLE_NAMESPACE))
                // If it's a bundle namespace (Require-Bundle), then we get the
                // version for package
                // of the providing bundle
                result = getVersionForPackage(
                    wire.getProviderWiring().getBundle(), packageName);

            if (result != null)
                return result;

        }
        return result;
    }

    private Collection<Class<?>> loadServiceInterfacesViaBundle(Bundle bundle,
        String[] interfaces) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (int i = 0; i < interfaces.length; i++) {
            try {
                result.add(bundle.loadClass(interfaces[i]));
            } catch (ClassNotFoundException e) {
                LOG.error("loadInterfacesViaBundle", "interface="
                    + interfaces[i] + " cannot be loaded by clientBundle="
                    + bundle.getSymbolicName(), e);
                continue;
            } catch (IllegalStateException e) {
                LOG.error(
                    "loadInterfacesViaBundle",
                    "interface="
                        + interfaces[i]
                        + " cannot be loaded since clientBundle is in illegal state",
                    e);
                continue;
            }
        }
        return result;
    }

    protected class ProxyClassLoader extends ClassLoader
    {

        private final Bundle loadingBundle;

        private int serviceUseCount = 0;

        public ProxyClassLoader(Bundle loadingBundle)
        {
            this.loadingBundle = loadingBundle;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return loadingBundle.loadClass(name);
        }

        public int getServiceUseCount() {
            return serviceUseCount;
        }

        public void addServiceUseCount() {
            serviceUseCount++;
        }

        public void removeServiceUseCount() {
            serviceUseCount--;
        }
    }

    /**
     * 监听注销事项,如果注销的服务为该RSA发布的,则关闭服务.
     * 
     */
    class EventListenerHooker implements EventListenerHook
    {

        @Override
        public void event(ServiceEvent event,
            Map<BundleContext, Collection<ListenerInfo>> listeners) {
            switch (event.getType()) {
                case ServiceEvent.UNREGISTERING:
                    handleServiceUnregistering(event.getServiceReference());
                    break;
                default:
                    break;
            }
        }
    }

    class ExportEndpoint
    {

        private ServiceReference<?> serviceReference;

        private HolaEndpointDescription endpointDescription;

        private RemoteRegistration<?> rsRegistration;

        private final Set<ExportRegistration> activeExportRegistrations = new HashSet<ExportRegistration>();

        ExportEndpoint(ServiceReference<?> serviceReference,
            HolaEndpointDescription endpointDescription,
            RemoteRegistration<?> reg)
        {
            Assert.isNotNull(serviceReference);
            this.serviceReference = serviceReference;
            Assert.isNotNull(endpointDescription);
            this.endpointDescription = endpointDescription;
            Assert.isNotNull(reg);
            this.rsRegistration = reg;
        }

        synchronized ID getProviderID() {
            return endpointDescription.getProviderID();
        }

        synchronized ServiceReference<?> getServiceReference() {
            return serviceReference;
        }

        synchronized EndpointDescription getEndpointDescription() {
            return endpointDescription;
        }

        synchronized RemoteRegistration<?> getRemoteServiceRegistration() {
            return rsRegistration;
        }

        synchronized boolean addExportRegistration(
            ExportRegistration exportRegistration) {
            return this.activeExportRegistrations.add(exportRegistration);
        }

        synchronized boolean close(ExportRegistration exportRegistration) {
            boolean removed = this.activeExportRegistrations.remove(exportRegistration);
            if (removed && activeExportRegistrations.size() == 0) {
                if (rsRegistration != null) {
                    rsRegistration.unregister();
                    rsRegistration = null;
                }
                serviceReference = null;
                endpointDescription = null;
            }
            return removed;
        }
    }

    class ExportReferenceImpl implements
        org.osgi.service.remoteserviceadmin.ExportReference
    {

        private ExportEndpoint exportEndpoint;

        private Throwable exception;

        private EndpointDescription errorEndpointDescription;

        ExportReferenceImpl(ExportEndpoint exportEndpoint)
        {
            Assert.isNotNull(exportEndpoint);
            this.exportEndpoint = exportEndpoint;
        }

        ExportReferenceImpl(Throwable exception,
            EndpointDescription errorEndpointDescription)
        {
            Assert.isNotNull(exception);
            this.exception = exception;
            Assert.isNotNull(exception);
            this.errorEndpointDescription = errorEndpointDescription;
        }

        synchronized Throwable getException() {
            return exception;
        }

        synchronized boolean close(ExportRegistration exportRegistration) {
            if (exportEndpoint == null)
                return false;
            boolean result = exportEndpoint.close(exportRegistration);
            exportEndpoint = null;
            return result;
        }

        synchronized ExportEndpoint getExportEndpoint() {
            return exportEndpoint;
        }

        synchronized RemoteRegistration<?> getRemoteServiceRegistration() {
            return (exportEndpoint == null) ? null
                : exportEndpoint.getRemoteServiceRegistration();
        }

        synchronized ID getProviderID() {
            return (exportEndpoint == null) ? null
                : exportEndpoint.getProviderID();
        }

        @Override
        public synchronized ServiceReference<?> getExportedService() {
            return (exportEndpoint == null) ? null
                : exportEndpoint.getServiceReference();
        }

        @Override
        public synchronized org.osgi.service.remoteserviceadmin.EndpointDescription getExportedEndpoint() {
            return (exportEndpoint == null) ? null
                : exportEndpoint.getEndpointDescription();
        }

        synchronized EndpointDescription getEndpointDescription() {
            return (exportEndpoint == null) ? errorEndpointDescription
                : exportEndpoint.getEndpointDescription();
        }

    }

    class ExportRegistrationImpl implements
        org.osgi.service.remoteserviceadmin.ExportRegistration
    {

        private final ExportReferenceImpl exportReference;

        private boolean closed = false;

        ExportRegistrationImpl(ExportEndpoint exportEndpoint)
        {
            Assert.isNotNull(exportEndpoint);
            exportEndpoint.addExportRegistration(this);
            this.exportReference = new ExportReferenceImpl(exportEndpoint);
        }

        ExportRegistrationImpl(Throwable exception,
            EndpointDescription errorEndpointDescription)
        {
            Assert.isNotNull(exception);
            this.exportReference = new ExportReferenceImpl(exception,
                errorEndpointDescription);
            this.closed = true;
        }

        ID getProviderID() {
            return exportReference.getProviderID();
        }

        ServiceReference<?> getServiceReference() {
            return exportReference.getExportedService();
        }

        @Override
        public org.osgi.service.remoteserviceadmin.ExportReference getExportReference() {
            Throwable t = getException();
            if (t != null)
                return null;
            return exportReference;
        }

        boolean match(ServiceReference<?> serviceReference) {
            return match(serviceReference, null);
        }

        boolean match(ServiceReference<?> serviceReference, ID containerID) {
            ServiceReference<?> ourServiceReference = getServiceReference();
            if (ourServiceReference == null)
                return false;
            boolean serviceReferenceCompare = ourServiceReference.equals(serviceReference);
            // If the second parameter is null, then we compare only on service
            // references
            if (containerID == null)
                return serviceReferenceCompare;
            ID ourContainerID = getProviderID();
            if (ourContainerID == null)
                return false;
            return serviceReferenceCompare
                && ourContainerID.equals(containerID);
        }

        synchronized ExportEndpoint getExportEndpoint(
            ServiceReference<?> serviceReference, ID containerID) {
            return match(serviceReference, containerID) ? exportReference.getExportEndpoint()
                : null;
        }

        RemoteRegistration<?> getRemoteServiceRegistration() {
            return exportReference.getRemoteServiceRegistration();
        }

        EndpointDescription getEndpointDescription() {
            return exportReference.getEndpointDescription();
        }

        @Override
        public void close() {
            boolean publish = false;
            ID providerId = null;
            Throwable exception = null;
            EndpointDescription endpointDescription = null;
            synchronized (this) {
                // Only do this once
                if (!closed) {
                    providerId = getProviderID();
                    exception = getException();
                    endpointDescription = getEndpointDescription();
                    publish = exportReference.close(this);
                    closed = true;
                }
            }
            removeExportRegistration(this);
            Bundle rsaBundle = getHostBundle();
            if (publish && rsaBundle != null)
                publishEvent(new HolaRemoteServiceAdminEvent(providerId,
                    RemoteServiceAdminEvent.EXPORT_UNREGISTRATION, rsaBundle,
                    exportReference, exception, endpointDescription),
                    endpointDescription);
        }

        @Override
        public Throwable getException() {
            return exportReference.getException();
        }
    }

    class ImportEndpoint
    {

        @SuppressWarnings("unused")
        // XXX this will be used when
        // ImportRegistration.update(EndpointDescription)
        // is added in RSA 1.1/RFC 203
        private final ID importContainerID;

        @SuppressWarnings("unused")
        private RemoteService rs;

        private RemoteServiceProvider provider;

        private HolaEndpointDescription endpointDescription;

        private RemoteListener rsListener;

        private RemoteReference<?> rsReference;

        private ServiceRegistration<?> proxyRegistration;

        private final Set<ImportRegistration> activeImportRegistrations = new HashSet<ImportRegistration>();

        ImportEndpoint(ID importContainerID, RemoteServiceProvider provider,
            RemoteReference rsReference, RemoteService rs,
            RemoteListener rsListener,
            ServiceRegistration proxyRegistration,
            HolaEndpointDescription endpointDescription)
        {
            this.importContainerID = importContainerID;
            this.provider = provider;
            this.endpointDescription = endpointDescription;
            this.rsReference = rsReference;
            this.rs = rs;
            this.rsListener = rsListener;
            this.proxyRegistration = proxyRegistration;
            // Add the remoteservice listener to the container adapter, so that
            // the rsListener notified asynchronously if our underlying remote
            // service
            // reference is unregistered locally due to disconnect or remote
            // ejection
            this.provider.addRemoteServiceListener(this.rsListener);
        }

        synchronized EndpointDescription getEndpointDescription() {
            return endpointDescription;
        }

        synchronized ServiceRegistration getProxyRegistration() {
            return proxyRegistration;
        }

        synchronized ID getProviderID() {
            return (rsReference == null) ? null : rsReference.getProviderID();
        }

        synchronized boolean addImportRegistration(
            ImportRegistrationImpl importRegistration) {
            return this.activeImportRegistrations.add(importRegistration);
        }

        synchronized boolean close(ImportRegistration importRegistration) {
            boolean removed = this.activeImportRegistrations.remove(importRegistration);
            if (removed && activeImportRegistrations.size() == 0) {
                if (proxyRegistration != null) {
                    proxyRegistration.unregister();
                    proxyRegistration = null;
                }
                if (provider != null) {
                    if (rsReference != null) {
                        provider.ungetRemoteService(rsReference);
                        rsReference = null;
                    }
                    // remove remote service listener
                    if (rsListener != null) {
                        provider.removeRemoteServiceListener(rsListener);
                        rsListener = null;
                    }
                    rs = null;
                    provider = null;
                }
                endpointDescription = null;
            }
            return removed;
        }

        synchronized boolean match(RemoteServiceID remoteServiceID) {
            if (remoteServiceID == null || rsReference == null)
                return false;
            return rsReference.getID().equals(remoteServiceID);
        }

        synchronized boolean match(EndpointDescription ed) {
            if (activeImportRegistrations.size() == 0)
                return false;
            return this.endpointDescription.isSameService(ed);
        }

    }

    class ImportReferenceImpl implements
        org.osgi.service.remoteserviceadmin.ImportReference
    {

        private ImportEndpoint importEndpoint;

        private Throwable exception;

        private EndpointDescription errorEndpointDescription;

        ImportReferenceImpl(ImportEndpoint importEndpoint)
        {
            Assert.isNotNull(importEndpoint);
            this.importEndpoint = importEndpoint;
        }

        ImportReferenceImpl(EndpointDescription endpointDescription,
            Throwable exception)
        {
            Assert.isNotNull(exception);
            this.exception = exception;
            Assert.isNotNull(endpointDescription);
            this.errorEndpointDescription = endpointDescription;
        }

        synchronized Throwable getException() {
            return exception;
        }

        synchronized boolean match(RemoteServiceID remoteServiceID) {
            return (importEndpoint == null) ? false
                : importEndpoint.match(remoteServiceID);
        }

        synchronized ImportEndpoint match(EndpointDescription ed) {
            if (importEndpoint != null && importEndpoint.match(ed))
                return importEndpoint;
            return null;
        }

        synchronized EndpointDescription getEndpointDescription() {
            return (importEndpoint == null) ? errorEndpointDescription
                : importEndpoint.getEndpointDescription();
        }

        synchronized ID getProviderID() {
            return (importEndpoint == null) ? null
                : importEndpoint.getProviderID();
        }

        @Override
        public synchronized ServiceReference<?> getImportedService() {
            return (importEndpoint == null) ? null
                : importEndpoint.getProxyRegistration().getReference();
        }

        @Override
        public synchronized org.osgi.service.remoteserviceadmin.EndpointDescription getImportedEndpoint() {
            return (importEndpoint == null) ? null
                : importEndpoint.getEndpointDescription();
        }

        synchronized boolean close(ImportRegistration importRegistration) {
            if (importEndpoint == null)
                return false;
            boolean result = importEndpoint.close(importRegistration);
            importEndpoint = null;
            return result;
        }

    }

    class ImportRegistrationImpl implements
        org.osgi.service.remoteserviceadmin.ImportRegistration
    {

        private final ImportReferenceImpl importReference;

        private boolean closed = false;

        ImportRegistrationImpl(ImportEndpoint importEndpoint)
        {
            Assert.isNotNull(importEndpoint);
            importEndpoint.addImportRegistration(this);
            this.importReference = new ImportReferenceImpl(importEndpoint);
        }

        ImportRegistrationImpl(EndpointDescription errorEndpointDescription,
            Throwable exception)
        {
            this.importReference = new ImportReferenceImpl(
                errorEndpointDescription, exception);
        }

        ID getProviderID() {
            return importReference.getProviderID();
        }

        EndpointDescription getEndpointDescription() {
            return importReference.getEndpointDescription();
        }

        boolean match(RemoteServiceID remoteServiceID) {
            return importReference.match(remoteServiceID);
        }

        boolean match(EndpointDescription ed) {
            return (getImportEndpoint(ed) != null);
        }

        ImportEndpoint getImportEndpoint(EndpointDescription ed) {
            return importReference.match(ed);
        }

        @Override
        public org.osgi.service.remoteserviceadmin.ImportReference getImportReference() {
            Throwable t = getException();
            if (t != null)
                return null;
            return importReference;
        }

        @Override
        public void close() {
            boolean publish = false;
            ID containerID = null;
            Throwable exception = null;
            EndpointDescription endpointDescription = null;
            synchronized (this) {
                // only do this once
                if (!closed) {
                    containerID = getProviderID();
                    exception = getException();
                    endpointDescription = getEndpointDescription();
                    publish = importReference.close(this);
                    closed = true;
                }
            }
            removeImportRegistration(this);
            Bundle rsaBundle = getHostBundle();

            if (publish && rsaBundle != null)
                publishEvent(new HolaRemoteServiceAdminEvent(containerID,
                    RemoteServiceAdminEvent.IMPORT_UNREGISTRATION, rsaBundle,
                    importReference, exception, endpointDescription),
                    endpointDescription);
        }

        @Override
        public Throwable getException() {
            return importReference.getException();
        }

    }
}
