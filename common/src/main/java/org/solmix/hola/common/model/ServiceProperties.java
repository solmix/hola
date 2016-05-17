/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.common.model;

import java.lang.reflect.Array;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.osgi.framework.Constants;
import org.solmix.commons.collections.UnChangeDictionary;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月20日
 */

public class ServiceProperties extends UnChangeDictionary<String, Object> {

    private ServiceProperties(int size, Dictionary<String, ?> props) {
        super(size);

        if (props == null) {
            return;
        }
        synchronized (props) {
            Enumeration<?> keysEnum = props.keys();

            while (keysEnum.hasMoreElements()) {
                Object key = keysEnum.nextElement();

                if (key instanceof String) {
                    String header = (String) key;

                    setProperty(header, props.get(header));
                }
            }
        }
    }

    public ServiceProperties(Dictionary<String, ?> props) {
        this((props == null) ? 2 : props.size() + 2, props);
    }

    public ServiceProperties()
    {
        super(2);
    }

    /**
     * Get a clone of the value of a service's property.
     * 
     * @param key header name.
     * @return Clone of the value of the property or <code>null</code> if there
     *         is no property by that name.
     */
   public Object getProperty(String key) {
        return cloneValue(get(key));
    }

    /**
     * Get the list of key names for the service's properties.
     * 
     * @return The list of property key names.
     */
   public synchronized String[] getPropertyKeys() {
        int size = size();

        String[] keynames = new String[size];

        Enumeration<String> keysEnum = keys();

        for (int i = 0; i < size; i++) {
            keynames[i] = keysEnum.nextElement();
        }

        return keynames;
    }

    /**
     * Put a clone of the property value into this property object.
     * 
     * @param key Name of property.
     * @param value Value of property.
     * @return previous property value.
     */
    synchronized Object setProperty(String key, Object value) {
        return set(key, cloneValue(value));
    }

    /**
     * Attempt to clone the value if necessary and possible.
     * 
     * For some strange reason, you can test to see of an Object is Cloneable
     * but you can't call the clone method since it is protected on Object!
     * 
     * @param value object to be cloned.
     * @return cloned object or original object if we didn't clone it.
     */
    private static Object cloneValue(Object value) {
        if (value == null)
            return null;
        if (value instanceof String) /* shortcut String */
            return value;
        if (value instanceof Number) /* shortcut Number */
            return value;
        if (value instanceof Character) /* shortcut Character */
            return value;
        if (value instanceof Boolean) /* shortcut Boolean */
            return value;

        Class<?> clazz = value.getClass();
        if (clazz.isArray()) {
            // Do an array copy
            Class<?> type = clazz.getComponentType();
            int len = Array.getLength(value);
            Object clonedArray = Array.newInstance(type, len);
            System.arraycopy(value, 0, clonedArray, 0, len);
            return clonedArray;
        }
        // must use reflection because Object clone method is protected!!
        try {
            return clazz.getMethod("clone", (Class<?>[]) null).invoke(value, (Object[]) null); 
        } catch (Exception e) {
            /* clone is not a public method on value's class */
        } catch (Error e) {
            /* JCL does not support reflection; try some well known types */
            if (value instanceof Vector<?>)
                return ((Vector<?>) value).clone();
            if (value instanceof Hashtable<?, ?>)
                return ((Hashtable<?, ?>) value).clone();
        }
        return value;
    }

    @Override
    public synchronized String toString() {
        String keys[] = getPropertyKeys();

        int size = keys.length;

        StringBuffer sb = new StringBuffer(20 * size);

        sb.append('{');

        int n = 0;
        for (int i = 0; i < size; i++) {
            String key = keys[i];
            if (!key.equals(Constants.OBJECTCLASS)) {
                if (n > 0)
                    sb.append(", "); 

                sb.append(key);
                sb.append('=');
                Object value = get(key);
                if (value.getClass().isArray()) {
                    sb.append('[');
                    int length = Array.getLength(value);
                    for (int j = 0; j < length; j++) {
                        if (j > 0)
                            sb.append(',');
                        sb.append(Array.get(value, j));
                    }
                    sb.append(']');
                } else {
                    sb.append(value);
                }
                n++;
            }
        }

        sb.append('}');

        return sb.toString();
    }
}
