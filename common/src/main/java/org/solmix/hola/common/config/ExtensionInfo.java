/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.hola.common.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.hola.common.Constants;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年10月2日
 */
@ThreadSafe
public abstract class ExtensionInfo<T> implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 7466337213411363731L;
    private final Map<String, Object> properties;
    
    public ExtensionInfo(Map<String, Object> properties){
        if (properties == null) {
            properties = new HashMap<String, Object>();
        } else {
            properties = new HashMap<String, Object>(properties);
        }
        this.properties = Collections.unmodifiableMap(properties);
    }
    
    public T addProperty(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return getSelf();
        if (value.equals(getProperty(key)))
            return getSelf();
        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.put(key, value);
        return makeSelf(map);
    }

    
    public T addProperties(Map<String, Object> properties) {
        if(properties==null||properties.size()==0){
            return getSelf();
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
            return getSelf();

        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.putAll(properties);
        return makeSelf(map);
    }

    
    public T addProperties(Properties properties) {
        if(properties==null||properties.size()==0){
            return getSelf();
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
            return getSelf();

        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        for(Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return  makeSelf(map);
    }

   
    public T addPropertyIfAbsent(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return getSelf();
        if (hasProperty(key))
            return getSelf();
        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.put(key, value);
        return makeSelf(map);
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
        return properties.get(Constants.DEFAULT_KEY_PREFIX + key);
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
    /*@SuppressWarnings("unchecked")
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(type==null)
            return null;
        if(this.getClass()==type){
            return (AdapterType) this;
        }
        if(ExtensionInfo.class.isAssignableFrom(type)){
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
    }*/
    protected abstract T getSelf();
    protected abstract T makeSelf(Map<String, Object> map);
}
