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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.solmix.hola.rs.RemoteServiceReference;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月2日
 */

public class PropertiesUtil
{

    /**
     * 配置合并
     * @param reference
     * @param map
     * @return
     */
    public static Map<String, Object> mergeProperties(ServiceReference<?> reference,
        Map<String,Object> map) {
        return mergeProperties(copyProperties(reference, new HashMap<String,Object>()), map);
    }

    public static Map<String, Object> copyProperties(
        Map<String, Object> source, Map<String, Object> target) {
        for (String key : source.keySet())
            target.put(key, source.get(key));
        return target;
    }

    public static Map<String, Object> copyProperties(
        final ServiceReference<?> serviceReference,
        final Map<String, Object> target) {
        final String[] keys = serviceReference.getPropertyKeys();
        for (int i = 0; i < keys.length; i++) {
            target.put(keys[i], serviceReference.getProperty(keys[i]));
        }
        return target;
    }

    public static Map<String,Object> mergeProperties(final Map<String, Object> source,
        final Map<String, Object> overrides) {

        // copy to target from service reference
        final Map<String,Object> target = copyProperties(source, new TreeMap<String, Object>(
            String.CASE_INSENSITIVE_ORDER));

        // now do actual merge
        final Set<String> keySet = overrides.keySet();
        for (final String key : keySet) {
            // skip keys not allowed
            if (Constants.SERVICE_ID.equals(key)
                || Constants.OBJECTCLASS.equals(key)) {
                continue;
            }
            target.remove(key.toLowerCase());
            target.put(key.toLowerCase(), overrides.get(key));
        }

        return target;
    }

    private static String[] getExportedInterfaces(
        ServiceReference<?> serviceReference, Object propValue) {
        if (propValue == null)
            return null;
        String[] objectClass = (String[]) serviceReference.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
        return getMatchingInterfaces(objectClass, propValue);
    }

    public static String[] getExportedInterfaces(
        ServiceReference serviceReference) {
        return getExportedInterfaces(
            serviceReference,
            serviceReference.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES));
    }

    public static String[] getMatchingInterfaces(String[] origin,
        Object propValue) {
        if (propValue == null || origin == null)
            return null;
        boolean wildcard = propValue.equals("*");
        if (wildcard)
            return origin;
        else {
            final String[] stringArrayValue = getStringArrayFromPropertyValue(propValue);
            if (stringArrayValue == null)
                return null;
            else if (stringArrayValue.length == 1
                && stringArrayValue[0].equals("*")) { 
                // this will support the idiom: new String[] { "*" }
                return origin;
            } else
                return stringArrayValue;
        }
    }

    public static String[] getStringArrayFromPropertyValue(Object value) {
        if (value == null)
            return null;
        else if (value instanceof String)
            return new String[] { (String) value };
        else if (value instanceof String[])
            return (String[]) value;
        else if (value instanceof Collection)
            return ((Collection<?>) value).toArray(new String[] {});
        else
            return null;
    }

    public static String[] getExportedInterfaces(
        ServiceReference<?> serviceReference, Map<String, ?> overridingProperties) {
        Object overridingPropValue = overridingProperties.get(RemoteConstants.SERVICE_EXPORTED_INTERFACES);
        if (overridingPropValue != null)
            return getExportedInterfaces(serviceReference, overridingPropValue);
        return getExportedInterfaces(serviceReference);
    }

    /**
     * Get all service intents include
     * <p>
     * {@link org.osgi.service.remoteserviceadmin.RemoteConstants#SERVICE_INTENTS}
     * {@link org.osgi.service.remoteserviceadmin.RemoteConstants#SERVICE_EXPORTED_INTENTS}
     * {@link org.osgi.service.remoteserviceadmin.RemoteConstants#SERVICE_EXPORTED_INTENTS_EXTRA}
     * 
     * @param serviceReference
     * @param overridingProperties
     * @return
     */
    public static String[] getServiceIntents(
        ServiceReference<?> serviceReference,
        Map<String, ?> overridingProperties) {
        List<String> results = new ArrayList<String>();

        String[] intents = getStringArrayFromPropertyValue(overridingProperties.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
        if (intents == null) {
            intents = getStringArrayFromPropertyValue(serviceReference.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
        }
        if (intents != null)
            results.addAll(Arrays.asList(intents));

        String[] exportedIntents = getStringArrayFromPropertyValue(overridingProperties.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
        if (exportedIntents == null) {
            exportedIntents = getStringArrayFromPropertyValue(serviceReference.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
        }
        if (exportedIntents != null)
            results.addAll(Arrays.asList(exportedIntents));

        String[] extraIntents = getStringArrayFromPropertyValue(overridingProperties.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
        if (extraIntents == null) {
            extraIntents = getStringArrayFromPropertyValue(serviceReference.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
        }
        if (extraIntents != null)
            results.addAll(Arrays.asList(extraIntents));

        if (results.size() == 0)
            return null;
        return results.toArray(new String[results.size()]);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringPlusProperty(Map<String,Object> properties, String key) {
        Object value = properties.get(key);
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof String) {
            return Collections.singletonList((String) value);
        }

        if (value instanceof String[]) {
            String[] values = (String[]) value;
            List<String> result = new ArrayList<String>(values.length);
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    result.add(values[i]);
                }
            }
            return Collections.unmodifiableList(result);
        }

        if (value instanceof Collection<?>) {
            Collection<String> values =(Collection<String>) value;
            List<String> result = new ArrayList<String>(values.size());
            for (Iterator<String> iter = values.iterator(); iter.hasNext();) {
                String v = iter.next();
                if (v instanceof String) {
                    result.add(v);
                }
            }
            return Collections.unmodifiableList(result);
        }
        return Collections.EMPTY_LIST;
    }

    public static String[] getStringArrayWithDefault(
        Map<String, Object> properties, String key, String[] def) {
        Object o = properties.get(key);
        if (o instanceof String) {
            return new String[] { (String) o };
        } else if (o instanceof String[]) {
            return (String[]) o;
        } else if (o instanceof List<?>) {
            List<?> l = (List<?>) o;
            return l.toArray(new String[l.size()]);
        }
        return def;
    }

    public static String getStringWithDefault(Map props, String key, String def) {
        Object o = props.get(key);
        if (o == null || (!(o instanceof String)))
            return def;
        return (String) o;
    }

    /**
     * @param idFilterNames
     * @return
     */
    public static Object convertToStringPlusValue(List<String> values) {
        if (values == null)
            return null;
      int valuesSize = values.size();
      switch (valuesSize) {
      case 0:
            return null;
      case 1:
            return values.get(0);
      default:
            return values.toArray(new String[valuesSize]);
      }
    }
    public static boolean isOSGiProperty(String key) {
        return osgiProperties.contains(key)
                    || key.startsWith(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_);
  }

  public static boolean isECFProperty(String key) {
        return ecfProperties.contains(key);
  }

  // skip dotted (private) properties (R4.2 enterprise spec. table 122.1)
  public static boolean isPrivateProperty(String key) {
        return (key.startsWith(".")); //$NON-NLS-1$
  }

  public static boolean isReservedProperty(String key) {
        return isOSGiProperty(key) || isECFProperty(key)
                    || isPrivateProperty(key);
  }

    public static Object getPropertyValue(ServiceReference<?> serviceReference,
        Map<String, Object> overridingProperties, String key) {
        Object result = null;
        if (overridingProperties != null)
            result = overridingProperties.get(key);
        return (result != null) ? result : getPropertyValue(serviceReference,
            key);
    }

    public static Object getPropertyValue(ServiceReference<?> serviceReference,
        String key) {
        return (serviceReference == null) ? null
            : serviceReference.getProperty(key);
    }

    public static Map<String, Object> copyNonReservedProperties(
        Map<String, Object> source, Map<String, Object> target) {
        for (String key : source.keySet())
            if (!isReservedProperty(key))
                target.put(key, source.get(key));
        return target;
    }
    public static Map<String, Object> copyNonReservedProperties(
        RemoteServiceReference<?> rsReference, Map<String, Object> target) {
  String[] keys = rsReference.getPropertyKeys();
  for (int i = 0; i < keys.length; i++)
        if (!isReservedProperty(keys[i]))
              target.put(keys[i], rsReference.getProperty(keys[i]));
  return target;
}
    public static Map<String, Object> copyNonReservedProperties(
        ServiceReference<?> serviceReference, Map<String, Object> target) {
        String[] keys = serviceReference.getPropertyKeys();
        for (int i = 0; i < keys.length; i++)
            if (!isReservedProperty(keys[i]))
                target.put(keys[i], serviceReference.getProperty(keys[i]));
        return target;
    }
    public static Dictionary<String,Object> createDictionaryFromMap(Map<String,Object> propMap) {
        if (propMap == null)
              return null;
        Dictionary<String,Object> result = new Hashtable<String,Object>();
        for (Iterator<String> i = propMap.keySet().iterator(); i.hasNext();) {
              String key = i.next();
              Object val = propMap.get(key);
              if (key != null && val != null)
                    result.put(key, val);
        }
        return result;
  }
  protected static final List osgiProperties = Arrays
      .asList(new String[] {
                  // OSGi properties
                  org.osgi.framework.Constants.OBJECTCLASS,
                  org.osgi.framework.Constants.SERVICE_ID,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
                  org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS });

protected static final List ecfProperties = Arrays.asList(new String[] {
    org.solmix.hola.rs.RemoteConstants.OBJECTCLASS,
    org.solmix.hola.rs.RemoteConstants.SERVICE_ID,
      HolaRemoteConstants.DISCOVERY_SERVICE_NAME_PREFIX,
      HolaRemoteConstants.DISCOVERY_AUTH,
      HolaRemoteConstants.DISCOVERY_PROTOCOL,
      HolaRemoteConstants.DISCOVERY_SCOPE,
      HolaRemoteConstants.DISCOVERY_SERVICE_NAME,
      HolaRemoteConstants.ENDPOINT_CONNECTTARGET_ID,
      HolaRemoteConstants.ENDPOINT_ID,
      HolaRemoteConstants.ENDPOINT_NAMESPACE,
      HolaRemoteConstants.ENDPOINT_TIMESTAMP,
      HolaRemoteConstants.ENDPOINT_IDFILTER_IDS,
      HolaRemoteConstants.ENDPOINT_REMOTESERVICE_FILTER,
//      HolaRemoteConstants.SERVICE_EXPORTED_CONTAINER_CONNECT_CONTEXT,
//      HolaRemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS,
//      HolaRemoteConstants.SERVICE_EXPORTED_CONTAINER_ID,
//      HolaRemoteConstants.SERVICE_IMPORTED_VALUETYPE,
      HolaRemoteConstants.DISCOVERY_SERVICE_TYPE });


}
