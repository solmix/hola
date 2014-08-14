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

package org.solmix.hola.core.model;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.solmix.hola.core.HolaConstants;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月14日
 */

public class EndpointInfo
{

    private final Map<String, Object> parameters;

    protected EndpointInfo()
    {
        parameters = null;
    }

    public EndpointInfo(Map<String, ?> parameters)
    {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        } else {
            parameters = new HashMap<String, Object>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public Object getParameter(String key) {
        Object value = parameters.get(key);
        if (value != null)
            return value;
        return parameters.get(HolaConstants.DEFAULT_KEY_PREFIX + key);
    }


    /**
     * @param key
     * @param defaultValue
     * @return return key's value if value is null return defaultValue.
     */
    public String getString(String key, String defaultValue) {
        Object value = getParameter(key);
        if (value == null)
            return defaultValue;
        else
            return value.toString();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = getParameter(key);
        if (value == null
            || ((value instanceof String) && (value.toString().length() == 0))) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public byte getByte(String key, Byte defaultValue) {
        Object value = getParameter(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Byte)
            return (Byte) value;
        else
            return new Byte(value.toString().trim());
    }

    public short getShort(String key, Short defaultValue) {
        Object value = getParameter(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Short)
            return (Short) value;
        else
            return new Short(value.toString().trim());
    }
    public int getInt(String key, int defaultValue,boolean positive) {
        int value = getInt(key,defaultValue);
       if(positive&&value<=0)
           return defaultValue;
       else
           return value;
    }
    public int getInt(String key, int defaultValue) {
        Object value = getParameter(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Integer)
            return ((Integer) value).intValue();
        else
            return new Integer(value.toString().trim());
    }
    
    public long getLong(String key, long defaultValue) {
        Object value = getParameter(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Long)
            return ((Long) value).longValue();
        else
            return new Long(value.toString().trim());
    }
    
    public String getHost(){
      return getString(HolaConstants.KEY_HOST,null);
    }

    /**
     * 
     */
    public int getPort() {
        return getInt(HolaConstants.KEY_PORT, 0);
    }

    /**
     * @param keyHeartbeat
     * @param defaultHeartbeat
     * @return
     */
    public EndpointInfo addParameterIfNotSet(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return this;
        if (hasParameter(key))
            return this;
        Map<String, Object> map = new HashMap<String, Object>(getParameters());
        map.put(key, value);
        return new EndpointInfo(map);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    /**
     * @param key
     * @return
     */
    public boolean hasParameter(String key) {
            Object value = getParameter(key);
            return value != null;
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public EndpointInfo addParameter(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return this;
        if (value.equals(getParameter(key)))
            return this;
        Map<String, Object> map = new HashMap<String, Object>(getParameters());
        map.put(key, value);
        return new EndpointInfo(map);
    }

    /**
     * @return
     */
    public String getIp() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param parameters
     * @return
     */
    public EndpointInfo addParameters(Map<String, Object> parameters2) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for(Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = getParameters().get(entry.getKey());
            if(value == null && entry.getValue() != null || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if(hasAndEqual) return this;

        Map<String, Object> map = new HashMap<String, Object>(getParameters());
        map.putAll(parameters);
        return  new EndpointInfo(map);
    }

    /**
     * @return
     */
    public InetSocketAddress toInetSocketAddress() {
        return  new InetSocketAddress(getHost(), getPort());
    }

    /**
     * @return
     */
    public String getAddress() {
        return getPort()<=1?getHost():getHost()+":"+getPort();
    }
    
    public ChannelInfo getChannel(){
        return new ChannelInfo();
    }
}
