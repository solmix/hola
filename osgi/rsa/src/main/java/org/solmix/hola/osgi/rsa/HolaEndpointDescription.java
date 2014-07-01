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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.rs.AsyncRemoteServiceProxy;

/**
 * 扩展OSGI RSA中的{@link EndpointDescription} ,添加元数据描述信息
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月2日
 */

public class HolaEndpointDescription extends EndpointDescription
{

    private static final Logger LOG = LoggerFactory.getLogger(HolaEndpointDescription.class.getName());

    private String id;

    private Long timestamp;

    private String idNamespace;

    private Long rsId;

    private ID connectTargetID;

    private String rsFilter;

    private ID[] idFilter;

    private List<String> asyncInterfaces;

    private Map<String, Object> overrides;

    private ID providerID;

    public HolaEndpointDescription(final ServiceReference<?> reference,
        final Map<String, Object> properties)
    {
        super(reference, properties);
        verifyProperties();
    }

    /**
     * @param properties
     */
    public HolaEndpointDescription(Map<String, ?> properties)
    {
        super(properties);
        verifyProperties();
    }

    public Map<String, Version> getInterfaceVersions() {
        Map<String, Version> result = new HashMap<String, Version>();
        addInterfaceVersions(getInterfaces(), result);
        addInterfaceVersions(getAsyncInterfaces(), result);
        return result;
    }

    public String getEndpointId() {
        return id;
    }

    /**
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    private void addInterfaceVersions(List<String> interfaces,
        Map<String, Version> result) {
        if (interfaces == null)
            return;
        for (String intf : interfaces) {
            int index = intf.lastIndexOf('.');
            if (index == -1)
                continue;
            String packageName = intf.substring(0, index);
            result.put(intf, getPackageVersion(packageName));
        }
    }

    public ID getProviderID() {
        return providerID;
    }

    /**
     * @return the connectTargetID
     */
    public ID getConnectTargetID() {
        return connectTargetID;
    }

    /**
     * @return the asyncInterfaces
     */
    public List<String> getAsyncInterfaces() {
        return asyncInterfaces;
    }

    /**
     * 
     */
    private void verifyProperties() {
        this.id = verifyStringProperty(HolaRemoteConstants.ENDPOINT_ID);
        if (this.id == null) {
            LOG.warn("HolaEndpointDescription perperty "
                + HolaRemoteConstants.ENDPOINT_ID
                + " not set. Using OSGI endpoint.id value");
            this.id = getId();
        }
        this.timestamp = verifyLongProperty(HolaRemoteConstants.ENDPOINT_TIMESTAMP);
        if (this.timestamp == null) {
            LOG.warn("HolaEndpointDescription perperty "
                + HolaRemoteConstants.ENDPOINT_TIMESTAMP
                + " not set. Using OSGI endpoint.service.id value");
            this.timestamp = getServiceId();
        }
        this.idNamespace = verifyStringProperty(HolaRemoteConstants.ENDPOINT_NAMESPACE);
        this.providerID = verifyIDProperty(idNamespace, this.id);
        this.rsId = verifyLongProperty(Constants.SERVICE_ID);
        if (this.rsId == null)
            this.rsId = getServiceId();
        this.connectTargetID = verifyIDProperty(HolaRemoteConstants.ENDPOINT_CONNECTTARGET_ID);
        this.rsFilter = verifyStringProperty(HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
        this.idFilter = verifyIDFilter();
        this.asyncInterfaces = verifyAsyncInterfaces();
    }

    private List<String> verifyAsyncInterfaces() {
        // Check to see that async proxy has not been disabled
        List<String> resultInterfaces = new ArrayList<String>();
        Object noAsyncProxy = getProperties().get(
            org.solmix.hola.rs.RemoteConstants.SERVICE_PREVENT_ASYNCPROXY);
        if (noAsyncProxy == null) {
            // Get service.exported.async.objectClass property value
            Object asyncObjectClass = getProperties().get(
                HolaRemoteConstants.SERVICE_EXPORTED_ASYNC_INTERFACES);
            // If present
            if (asyncObjectClass != null) {
                List<String> originalInterfaces = getInterfaces();
                String[] matchingInterfaces = PropertiesUtil.getMatchingInterfaces(
                    originalInterfaces.toArray(new String[originalInterfaces.size()]),
                    asyncObjectClass);
                if (matchingInterfaces != null)
                    for (int i = 0; i < matchingInterfaces.length; i++) {
                        String asyncInterface = convertInterfaceToAsync(matchingInterfaces[i]);
                        if (asyncInterface != null
                            && !resultInterfaces.contains(asyncInterface))
                            resultInterfaces.add(asyncInterface);
                    }
            }
        }
        return Collections.unmodifiableList(resultInterfaces);
    }

    private String convertInterfaceToAsync(String interfaceName) {
        if (interfaceName == null)
            return null;
        String asyncProxyName = (String) getProperties().get(
            org.solmix.hola.rs.RemoteConstants.SERVICE_ASYNC_RSPROXY_CLASS_
                + interfaceName);
        if (asyncProxyName != null)
            return asyncProxyName;
        if (interfaceName.endsWith(AsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX))
            return interfaceName;
        return interfaceName + AsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX;
    }

    private ID[] verifyIDFilter() {
        List<String> idNames = PropertiesUtil.getStringPlusProperty(
            getProperties(), HolaRemoteConstants.ENDPOINT_IDFILTER_IDS);
        if (idNames.size() == 0)
            return null;
        List<ID> results = new ArrayList<ID>();
        String idNamespace = getIDNamespace();
        for (String idName : idNames) {
            results.add(DefaultIDFactory.getDefault().createID(idNamespace,
                idName));
        }
        return results.toArray(new ID[results.size()]);
    }

    /**
     * @return
     */
    private String getIDNamespace() {
        return this.idNamespace;
    }

    private ID verifyIDProperty(String namePropName) {
        return verifyIDProperty(idNamespace, verifyStringProperty(namePropName));
    }

    private String verifyStringProperty(String proertyName) {
        Object v = getProperties().get(proertyName);
        try {
            return (String) v;
        } catch (ClassCastException e) {
            IllegalArgumentException iae = new IllegalArgumentException(
                "property value is not a String: " + proertyName);
            iae.initCause(e);
            throw iae;
        }
    }

    private Long verifyLongProperty(String proertyName) {
        Object v = getProperties().get(proertyName);
        try {
            return (Long) v;
        } catch (ClassCastException e) {
            IllegalArgumentException iae = new IllegalArgumentException(
                "property value is not a Long: " + proertyName);
            iae.initCause(e);
            throw iae;
        }
    }

    private ID verifyIDProperty(String idNamespace, String idName) {
        if (idName == null)
            return null;
        return DefaultIDFactory.getDefault().createID(idNamespace, idName);
    }

    /**
     * 
     */
    public ID[] getIDFilter() {
        return idFilter;
    }

    /**
     * @return
     */
    public String getRemoteServiceFilter() {
        return rsFilter;
    }

    /**
     * @return
     */
    public String getIdNamespace() {
        return idNamespace;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (overrides != null)
            return overrides;
        return super.getProperties();
    }

    @Override
    public boolean isSameService(EndpointDescription other) {
        // If same ed instance then they are for same service
        if (this == other)
            return true;
        // Like superclass, check to see that the framework id is not null
        String frameworkId = getFrameworkUUID();
        if (frameworkId == null)
            return false;
        // The id, the service id and the frameworkid have to be identical
        // to be considered the same service
        return (getId().equals(other.getId())
            && getServiceId() == other.getServiceId() && frameworkId.equals(other.getFrameworkUUID()));
    }

    /**
     * @return
     */
    public Long getRemoteServiceId() {
        return this.rsId;
    }

    void setPropertiesOverrides(Map<String, Object> propertiesOverrides) {
        this.overrides = PropertiesUtil.mergeProperties(super.getProperties(),
            propertiesOverrides);
    }
}
