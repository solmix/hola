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
package org.solmix.hola.core.model;

import java.util.Map;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年10月24日
 */

public class InfoUtils
{

    public static Boolean getBoolean(Map<String,Object> map,String key){
        Object value = map.get(key);
        return value==null?null:Boolean.parseBoolean(value.toString());
    }
   
    public static boolean getBoolean(Map<String,Object> map,String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null
            || ((value instanceof String) && (value.toString().length() == 0))) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    
    }

    public static Byte getByte(Map<String,Object> map,String key) {
        return getByte(map,key,null);
    }

    public static byte getByte(Map<String,Object> map,String key, Byte defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Byte)
            return (Byte) value;
        else
            return new Byte(value.toString().trim());
    }
    
    public static Short getShort(Map<String,Object> map,String key) {
      return getShort(map,key,null);
    }
   
    public static short getShort(Map<String,Object> map,String key, Short defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Short)
            return (Short) value;
        else
            return new Short(value.toString().trim());
    }
    public static Integer getInt(Map<String,Object> map,String key) {
        return getInt(map,key,null);
    }
    
    public static Integer getInt(Map<String,Object> map,String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Integer)
            return ((Integer) value).intValue();
        else
            return new Integer(value.toString().trim());
    }

    public static Long getLong(Map<String,Object> map,String key) {
        return getLong(map,key,null);
    }

    public static long getLong(Map<String,Object> map,String key, Long defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Long)
            return ((Long) value).longValue();
        else
            return new Long(value.toString().trim());
    
    }

    public static String getString(Map<String,Object> map,String key) {
        Object value = map.get(key);
        return value==null?null:value.toString();
    }
    
    public static String getString(Map<String,Object> map,String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null)
            return defaultValue;
        else
            return value.toString();
    }
}
