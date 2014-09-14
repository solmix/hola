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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.osgi.framework.Version;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.discovery.ServiceProperties;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public abstract class AbstractMetadataFactory
{

    private static final String LIST_SEPARATOR = ",";

    protected void encodeServiceProperties(
        org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
        ServiceProperties result) {
        encodeOSGI(endpointDescription, result);
        // 处理扩展
        if (endpointDescription instanceof HolaEndpointDescription) {
            HolaEndpointDescription hde = (HolaEndpointDescription) endpointDescription;
            encodeHolaExtra(hde, result);
        }
        encodeOther(endpointDescription, result);
    }

    /**
     * @param serviceProperties
     */
    protected EndpointDescription transform(ServiceProperties serviceProperties) {
        Map<String, Object> edp = new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER);
        decodeOSGI(serviceProperties, edp);
        decodeHolaExtra(serviceProperties, edp);
        decodeOther(serviceProperties, edp);
        return new HolaEndpointDescription(edp);
    }

    /**
     * @param endpointDescription
     * @param result
     */
    private void encodeOther(EndpointDescription endpointDescription,
        ServiceProperties result) {
        Map<String, Object> properties = endpointDescription.getProperties();
        for (String key : properties.keySet()) {
            if (!PropertiesUtil.isReservedProperty(key)) {
                Object val = properties.get(key);
                if (val instanceof byte[]) {
                    result.setPropertyBytes(key, (byte[]) val);
                } else if (val instanceof String) {
                    result.setPropertyString(key, (String) val);
                } else {
                    result.setProperty(key, val);
                }
            }
        }

    }

    private void decodeOther(ServiceProperties props, Map<String, Object> result) {
        for (Enumeration keys = props.getPropertyNames(); keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            if (!PropertiesUtil.isReservedProperty(key)) {
                byte[] bytes = props.getPropertyBytes(key);
                if (bytes != null) {
                    result.put(key, bytes);
                    continue;
                }
                String str = props.getPropertyString(key);
                if (str != null) {
                    result.put(key, str);
                    continue;
                }
                Object obj = props.getProperty(key);
                if (obj != null) {
                    result.put(key, obj);
                    continue;
                }
            }
        }

    }

    /**
     * @param hde
     * @param result
     */
    private void encodeHolaExtra(HolaEndpointDescription hde,
        ServiceProperties result) {
        Map<String, Object> hdeProps = hde.getProperties();
        Long rsId = (Long) hdeProps.get(org.solmix.hola.rs.RemoteConstants.SERVICE_ID);
        if (rsId != null) {
            encodeLong(result, org.solmix.hola.rs.RemoteConstants.SERVICE_ID, rsId);
        }
        String hedId = (String) hdeProps.get(HolaRemoteConstants.ENDPOINT_ID);
        if (hedId != null) {
            encodeString(result, HolaRemoteConstants.ENDPOINT_ID, hedId);
        }
        String containerIDNamespace = hde.getIdNamespace();
        if (containerIDNamespace != null)
            encodeString(result, HolaRemoteConstants.ENDPOINT_NAMESPACE,
                containerIDNamespace);
        Long hts = (Long) hdeProps.get(HolaRemoteConstants.ENDPOINT_TIMESTAMP);
        if (hts != null) {
            encodeLong(result, HolaRemoteConstants.ENDPOINT_TIMESTAMP, hts);
        }
        ID[] idFilter = hde.getIDFilter();
        if (idFilter != null && idFilter.length > 0) {
            List<String> idNames = new ArrayList<String>();
            for (int i = 0; i < idFilter.length; i++) {
                idNames.add(idFilter[i].getName());
            }
            encodeList(result, HolaRemoteConstants.ENDPOINT_IDFILTER_IDS,
                idNames);
        }
        String remoteFilter = hde.getRemoteServiceFilter();
        if (remoteFilter != null)
            encodeString(result,
                HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER, remoteFilter);
        List<String> asyncTypes = hde.getAsyncInterfaces();
        if (asyncTypes != null && asyncTypes.size() > 0)
            encodeList(result,
                HolaRemoteConstants.SERVICE_EXPORTED_ASYNC_INTERFACES,
                asyncTypes);
    }

    private void decodeHolaExtra(ServiceProperties serviceProperties,
        Map<String, Object> edp) {
        String containerIDNamespace = decodeString(serviceProperties,
            HolaRemoteConstants.ENDPOINT_NAMESPACE);
        if (containerIDNamespace != null) {
            // remote service id
            Long remoteServiceId = decodeLong(serviceProperties,
                org.solmix.hola.rs.RemoteConstants.SERVICE_ID);
            if (remoteServiceId != null)
                edp.put(org.solmix.hola.rs.RemoteConstants.SERVICE_ID, remoteServiceId);

            // container id namespace
            edp.put(HolaRemoteConstants.ENDPOINT_NAMESPACE,
                containerIDNamespace);

            // ecf endpoint id
            String ecfEndpointId = decodeString(serviceProperties,
                RemoteConstants.ENDPOINT_ID);
            if (ecfEndpointId != null)
                edp.put(RemoteConstants.ENDPOINT_ID, ecfEndpointId);

            // timestamp
            Long timestamp = decodeLong(serviceProperties,
                HolaRemoteConstants.ENDPOINT_TIMESTAMP);
            if (timestamp != null)
                edp.put(HolaRemoteConstants.ENDPOINT_TIMESTAMP, timestamp);

            /*
             * // connect target ID String connectTargetIDName = decodeString(
             * serviceProperties, RemoteConstants.ENDPOINT_CONNECTTARGET_ID); if
             * (connectTargetIDName != null) edp.put(
             * RemoteConstants.ENDPOINT_CONNECTTARGET_ID, connectTargetIDName);
             */

            // ID filter
            List<String> idFilterNames = decodeList(serviceProperties,
                HolaRemoteConstants.ENDPOINT_IDFILTER_IDS);
            Object idFilterNamesval = PropertiesUtil.convertToStringPlusValue(idFilterNames);
            if (idFilterNamesval != null)
                edp.put(HolaRemoteConstants.ENDPOINT_IDFILTER_IDS,
                    idFilterNamesval);

            // remote service filter
            String remoteServiceFilter = decodeString(serviceProperties,
                HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
            if (remoteServiceFilter != null)
                edp.put(HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
                    remoteServiceFilter);

            List<String> asyncInterfaces = decodeList(serviceProperties,
                HolaRemoteConstants.SERVICE_EXPORTED_ASYNC_INTERFACES);
            if (asyncInterfaces != null && asyncInterfaces.size() > 0)
                edp.put(HolaRemoteConstants.SERVICE_EXPORTED_ASYNC_INTERFACES,
                    asyncInterfaces.toArray(new String[asyncInterfaces.size()]));

        }

    }

    /**
     * @param result
     * @param endpointIdfilterIds
     * @param idNames
     */
    private void encodeList(ServiceProperties props, String key,
        List<String> list) {
        if (list == null)
            return;
        if (list.size() == 1) {
            props.setPropertyString(key, list.get(0));
        } else {
            final StringBuffer result = new StringBuffer();
            for (Iterator<String> i = list.iterator(); i.hasNext();) {
                result.append(i.next());
                if (i.hasNext())
                    result.append(LIST_SEPARATOR);
            }
            props.setPropertyString(key, result.toString());
        }

    }

    /**
     * @param result
     * @param endpointId
     * @param hedId
     */
    private void encodeString(ServiceProperties result, String name,
        String value) {
        result.setPropertyString(name, value);
    }

    /**
     * @param result
     * @param serviceId
     * @param rsId
     */
    private void encodeLong(ServiceProperties result, String serviceId,
        Long rsId) {
        result.setPropertyString(serviceId, rsId.toString());

    }

    /**
     * @param endpointDescription
     * @param result
     */
    private void encodeOSGI(EndpointDescription endpointDescription,
        ServiceProperties result) {
        List<String> interfaces = endpointDescription.getInterfaces();
        encodeList(result, org.osgi.framework.Constants.OBJECTCLASS, interfaces);

        for (String intf : interfaces) {
            String intfPackageName = getPackageName(intf);
            Version intfVersion = endpointDescription.getPackageVersion(intfPackageName);
            if (intfVersion != null
                && !Version.emptyVersion.equals(intfVersion))
                encodeString(result, RemoteConstants.ENDPOINT_PACKAGE_VERSION_
                    + intfPackageName, intfVersion.toString());
        }

        String endpointId = endpointDescription.getId();
        encodeString(result, RemoteConstants.ENDPOINT_ID, endpointId);

        long endpointServiceId = endpointDescription.getServiceId();
        encodeLong(result, RemoteConstants.ENDPOINT_SERVICE_ID, new Long(
            endpointServiceId));

        String frameworkUUID = endpointDescription.getFrameworkUUID();
        if (frameworkUUID != null)
            encodeString(result, RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
                frameworkUUID);

        List<String> configurationTypes = endpointDescription.getConfigurationTypes();
        if (configurationTypes.size() > 0)
            encodeList(result, RemoteConstants.SERVICE_IMPORTED_CONFIGS,
                configurationTypes);

        List<String> serviceIntents = endpointDescription.getIntents();
        if (serviceIntents.size() > 0)
            encodeList(result, RemoteConstants.SERVICE_INTENTS, serviceIntents);

        Map<String, Object> endpointDescriptionProperties = endpointDescription.getProperties();
        List<String> remoteConfigsSupported = PropertiesUtil.getStringPlusProperty(
            endpointDescriptionProperties,
            RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
        if (remoteConfigsSupported.size() > 0)
            encodeList(result, RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
                remoteConfigsSupported);

        List<String> remoteIntentsSupported = PropertiesUtil.getStringPlusProperty(
            endpointDescriptionProperties,
            RemoteConstants.REMOTE_INTENTS_SUPPORTED);
        if (remoteIntentsSupported.size() > 0)
            encodeList(result, RemoteConstants.REMOTE_INTENTS_SUPPORTED,
                remoteIntentsSupported);

    }

    /**
     * @param serviceProperties
     * @param edp
     */
    private void decodeOSGI(ServiceProperties serviceProperties,
        Map<String, Object> osgiProperties) {
        List<String> interfaces = decodeList(serviceProperties,
            org.osgi.framework.Constants.OBJECTCLASS);
        osgiProperties.put(org.osgi.framework.Constants.OBJECTCLASS,
            interfaces.toArray(new String[interfaces.size()]));
        // RemoteConstants.ENDPOINT_PACKAGE_VERSION_
        for (String intf : interfaces) {
            String packageKey = RemoteConstants.ENDPOINT_PACKAGE_VERSION_
                + getPackageName(intf);
            String intfVersion = decodeString(serviceProperties, packageKey);
            if (intfVersion != null)
                osgiProperties.put(packageKey, intfVersion);
        }
        // RemoteConstants.ENDPOINT_ID
        String endpointId = decodeString(serviceProperties,
            RemoteConstants.ENDPOINT_ID);
        osgiProperties.put(RemoteConstants.ENDPOINT_ID, endpointId);
        // RemoteConstants.ENDPOINT_SERVICE_ID
        Long endpointServiceId = decodeLong(serviceProperties,
            RemoteConstants.ENDPOINT_SERVICE_ID);
        osgiProperties.put(RemoteConstants.ENDPOINT_SERVICE_ID,
            endpointServiceId);
        // RemoteConstants.ENDPOINT_FRAMEWORK_UUID
        String fwkuuid = decodeString(serviceProperties,
            RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
        osgiProperties.put(RemoteConstants.ENDPOINT_FRAMEWORK_UUID, fwkuuid);
        // RemoteConstants.SERVICE_IMPORTED_CONFIGS
        List<String> configTypes = decodeList(serviceProperties,
            RemoteConstants.SERVICE_IMPORTED_CONFIGS);
        if (configTypes != null && configTypes.size() > 0)
            osgiProperties.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS,
                configTypes.toArray(new String[configTypes.size()]));
        // RemoteConstants.SERVICE_INTENTS
        List<String> intents = decodeList(serviceProperties,
            RemoteConstants.SERVICE_INTENTS);
        if (intents != null && intents.size() > 0)
            osgiProperties.put(RemoteConstants.SERVICE_INTENTS,
                intents.toArray(new String[intents.size()]));
        // RemoteConstants.REMOTE_CONFIGS_SUPPORTED
        List<String> remoteConfigsSupported = decodeList(serviceProperties,
            RemoteConstants.REMOTE_CONFIGS_SUPPORTED);
        if (remoteConfigsSupported != null && remoteConfigsSupported.size() > 0)
            osgiProperties.put(
                RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
                remoteConfigsSupported.toArray(new String[remoteConfigsSupported.size()]));
        // RemoteConstants.REMOTE_INTENTS_SUPPORTED
        List<String> remoteIntentsSupported = decodeList(serviceProperties,
            RemoteConstants.REMOTE_INTENTS_SUPPORTED);
        if (remoteIntentsSupported != null && remoteIntentsSupported.size() > 0)
            osgiProperties.put(
                RemoteConstants.REMOTE_INTENTS_SUPPORTED,
                remoteIntentsSupported.toArray(new String[remoteIntentsSupported.size()]));

    }

    /**
     * @param serviceProperties
     * @param endpointServiceId
     * @return
     */
    private Long decodeLong(ServiceProperties props, String name) {
        Object o = props.getProperty(name);
        if (o == null)
            return new Long(0);
        if (o instanceof Long)
            return (Long) o;
        if (o instanceof Integer)
            return new Long(((Integer) o).longValue());
        if (o instanceof String)
            return new Long(o.toString().trim());
        return new Long(0);
    }

    /**
     * @param serviceProperties
     * @param endpointFrameworkUuid
     * @return
     */
    private String decodeString(ServiceProperties props, String name) {
        return props.getPropertyString(name);
    }

    /**
     * @param serviceProperties
     * @param serviceImportedConfigs
     * @return
     */
    private List<String> decodeList(ServiceProperties serviceProperties,
        String name) {
        String value = serviceProperties.getPropertyString(name);
        if (value == null)
            return Collections.emptyList();
        List<String> result = new ArrayList<String>();
        final StringTokenizer t = new StringTokenizer(value, LIST_SEPARATOR);
        while (t.hasMoreTokens())
            result.add(t.nextToken());
        return result;
    }

    private String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf("."); //$NON-NLS-1$
        if (lastDotIndex == -1)
            return ""; //$NON-NLS-1$
        return className.substring(0, lastDotIndex);
    }

    /**
     * 
     */
    public void close() {
    }
}
