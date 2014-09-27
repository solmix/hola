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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.hola.core.HolaConstants;
import org.solmix.runtime.adapter.Adaptable;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月22日
 */
@ThreadSafe
public class EndpointInfo implements Adaptable
{
    /**
     * 主机
     */
    public static final String PROTOCOL = "protocol";
    /**
     * 主机
     */
    public static final String HOST = "host";

    /**
     * 端口
     */
    public static final String PORT = "port";   
/**
     * 服务路径(contextPath)
     */
    public static final String PATH = "path";
    
    /**
     * 用户名
     */
    public static final String USERNAME="username";
    /**
     * 密码
     */
    public static final String PASSWORD = "password";

    public static final String SCOPE_KEY = "scope";
    
    public static final String SCOPE_NONE = "none";
    
    public static final String SCOPE_REMOTE = "remote";
    
    public static final String SCOPE_LOCAL = "local";
    
    /**
     * 是否公告服务
     */
    public static final String ADVERTISE_KEY = "advertise";
    public static final String INTERFACE_KEY = "interface";
    
    
    
    private final Map<String, Object> properties;

    public EndpointInfo(Map<String, Object> properties){
        if (properties == null) {
            properties = new HashMap<String, Object>();
        } else {
            properties = new HashMap<String, Object>(properties);
        }
        this.properties = Collections.unmodifiableMap(properties);
    }
    
    public EndpointInfo addProperty(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return this;
        if (value.equals(getProperty(key)))
            return this;
        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.put(key, value);
        return new EndpointInfo(map);
    }

   
    public EndpointInfo addProperties(Map<String, Object> properties) {
        if(properties==null||properties.size()==0){
            return this;
        }
        boolean hasAndEqual = true;
        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = getProperty(entry.getKey());
            if(value == null && entry.getValue() != null || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if(hasAndEqual) 
            return this;

        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.putAll(properties);
        return  new EndpointInfo(map);
    }

    
    public EndpointInfo addProperties(Properties properties) {
        if(properties==null||properties.size()==0){
            return this;
        }
        boolean hasAndEqual = true;
        for(Map.Entry<Object, Object> entry : properties.entrySet()) {
            Object value = getProperty(entry.getKey().toString());
            if(value == null && entry.getValue() != null || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if(hasAndEqual) 
            return this;

        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        for(Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return  new EndpointInfo(map);
    }

   
    public EndpointInfo addPropertyIfAbsent(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return this;
        if (hasProperty(key))
            return this;
        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.put(key, value);
        return new EndpointInfo(map);
    }

   
    public boolean hasProperty(String key) {
        Object value = getProperty(key);
        return value != null;
    }

    
    public Map<String, Object> getProperties() {
        return properties;
    }

   
    public Object getProperty(String key) {
        Object value = properties.get(key);
        if (value != null)
            return value;
        return properties.get(HolaConstants.DEFAULT_KEY_PREFIX + key);
    }
    public Boolean getBoolean(String key){
        Object value = getProperty(key);
        return value==null?null:Boolean.parseBoolean(value.toString());
    }
   
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = getProperty(key);
        if (value == null
            || ((value instanceof String) && (value.toString().length() == 0))) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    
    }

    public Byte getByte(String key) {
        return getByte(key,null);
    }

    public byte getByte(String key, Byte defaultValue) {
        Object value = getProperty(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Byte)
            return (Byte) value;
        else
            return new Byte(value.toString().trim());
    }
    
    public Short getShort(String key) {
      return getShort(key,null);
    }
   
    public short getShort(String key, Short defaultValue) {
        Object value = getProperty(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Short)
            return (Short) value;
        else
            return new Short(value.toString().trim());
    }
    public Integer getInt(String key) {
        return getInt(key,null);
    }
    
    public Integer getInt(String key, Integer defaultValue) {
        Object value = getProperty(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Integer)
            return ((Integer) value).intValue();
        else
            return new Integer(value.toString().trim());
    }

    public Long getLong(String key) {
        return getLong(key,null);
    }

    public long getLong(String key, Long defaultValue) {
        Object value = getProperty(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Long)
            return ((Long) value).longValue();
        else
            return new Long(value.toString().trim());
    
    }

    public String getString(String key) {
        Object value = getProperty(key);
        return value==null?null:value.toString();
    }
    
    public String getString(String key, String defaultValue) {
        Object value = getProperty(key);
        if (value == null)
            return defaultValue;
        else
            return value.toString();
    }

    public static EndpointInfo parse(String url) {
        if (url == null || url.trim().length() == 0) {
            throw new IllegalArgumentException("endpointinfo url is null");
        }
        final Map<String, Object> properties = new HashMap<String, Object>();
        int p = url.indexOf("?");
        if (p >= 0) {
            String[] parts = url.substring(p + 1).split("\\&");
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        properties.put(part.substring(0, j),
                            part.substring(j + 1));
                    } else {
                        properties.put(part, part);
                    }
                }
            }
            url = url.substring(0, p);
        }
        p = url.indexOf("://");
        if (p >= 0) {
            if (p == 0)
                throw new IllegalStateException("url missing protocol: \""
                    + url + "\"");
            properties.put(PROTOCOL, url.substring(0, p));
            url = url.substring(p + 3);
        }
        p = url.indexOf("/");
        if (p >= 0) {
            properties.put(PATH, url.substring(p + 1));
            url = url.substring(0, p);
        }
        p = url.indexOf("@");
        if (p >= 0) {
            String username = url.substring(0, p);
            String password = null;
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            properties.put(USERNAME, username);
            if (password == null) {
                properties.put(PASSWORD, password);
            }
            url = url.substring(p + 1);
        }
        p = url.indexOf(":");
        if (p >= 0 && p < url.length() - 1) {
            properties.put(PORT, Integer.parseInt(url.substring(p + 1)));
            url = url.substring(0, p);
        }
        if (url.length() > 0) {
            properties.put(HOST, url);
        }
        return new EndpointInfo(properties);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.adapter.Adaptable#adaptTo(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(type==null)
            return null;
        if(this.getClass()==type){
            return (AdapterType) this;
        }
        if(EndpointInfo.class.isAssignableFrom(type)){
            AdapterType instance=null;
            try {
                Constructor<AdapterType>  c=type.getConstructor(Map.class);
                if(c!=null){
                    instance=  c.newInstance(this.getProperties());
                }else{
                    instance=type.newInstance();
                    EndpointInfo p=  ((EndpointInfo )instance);
                    p.addProperties(this.getProperties());
                }
            } catch (Exception e) {
               throw new IllegalArgumentException("Failed instance class:"+type.getName());
            } 
            return instance;
        }else{
            throw new IllegalArgumentException("super class:"+type.getName()+",must be subclass of "+EndpointInfo.class.getName());
        }
    }
}
