/*
 * Copyright 2015 The Solmix Project
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

import static org.solmix.commons.util.DataUtils.commaSeparatedStringToList;
import static org.solmix.commons.util.DataUtils.listToArray;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;


/**
 * 地址和参数之间的转换
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月22日
 */

public class PropertiesUtils
{
    private PropertiesUtils(){
        
    }
    public static String[] ESCAPLES=new String[] { HOLA.ADDRESS_KEY, HOLA.PROTOCOL_KEY, HOLA.HOST_KEY,
        HOLA.PORT_KEY, HOLA.PATH_KEY, HOLA.USER_KEY, HOLA.PASSWORD_KEY,HOLA.MONITOR_KEY,HOLA.DISCOVERY_KEY };
    public static String toAddress(Dictionary<String, ?> properties){
        return toAddress(properties,false,true,false,true);
    }
    
    public static String toIndentityAddress(Dictionary<String, ?> properties){
        return toAddress(properties,false,true,false,true);
    }
    /**
     * 根据service配置参数生成Address
     * <li>如果配置address，返回
     * <li>根据protocol://host(IP):port/path?key1=value1&key2=value2
     * @param properties
     * @return
     */
    public static String toAddress(Dictionary<String, ?> properties,boolean appendUser,boolean appendParameter, boolean useIP,boolean identity){
        if(properties==null){
            return null;
        }
        String address =(String)properties.get(HOLA.ADDRESS_KEY);
        if(!StringUtils.isEmpty(address)){
            return address;
        }
        StringBuilder buf = new StringBuilder();
        String protocol =(String) properties.get(HOLA.PROTOCOL_KEY);
        if(!StringUtils.isEmpty(protocol)){
            buf.append(protocol);
            buf.append("://");
        }
        if(appendUser){
            String user =(String) properties.get(HOLA.USER_KEY);
            if(!StringUtils.isEmpty(user)){
                buf.append(user);
                String password =(String) properties.get(HOLA.PASSWORD_KEY);
                if(!StringUtils.isEmpty(user)){
                    buf.append(":");
                    buf.append(password);
                }
                buf.append("@");
            }
        }
        String host=(String) properties.get(HOLA.HOST_KEY);
        if (host!=null&&useIP) {
              host = NetUtils.getIpByHost(host);
        }
        if(!StringUtils.isEmpty(host)){
            buf.append(host);
            String port=properties.get(HOLA.PORT_KEY)!=null?properties.get(HOLA.PORT_KEY).toString():null;
            if (port!=null&&Integer.valueOf(port.toString()) > 0) {
                  buf.append(":");
                  buf.append(port);
            }
        }
        String path=(String) properties.get(HOLA.PATH_KEY);
        if(!StringUtils.isEmpty(path)){
            if(!path.startsWith("/")){
                buf.append("/");
            }
            buf.append(path);
        }
        if (appendParameter) {
            buildQueryString(buf, true,identity, properties,ESCAPLES );
        }
        return buf.toString();
    }
    
    private static void buildQueryString(StringBuilder buf, boolean concat, boolean identity, Dictionary<String, ?> properties, String[] excluds) {
        if (properties != null && properties.size() > 0) {
            List<String> excludes = (excluds == null || excluds.length == 0 ? null : Arrays.asList(excluds));
            boolean first = true;
            if (identity) {
                List<String> sorted = new ArrayList<String>();
                Enumeration<String> enums = properties.keys();
                while (enums.hasMoreElements()) {
                    String key = enums.nextElement();
                    sorted.add(key);
                }
                Collections.sort(sorted);
                for(String key:sorted){
                    if (excludes.contains(key)) {
                        continue;
                    }
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(key);
                    buf.append("=");
                    buf.append(properties.get(key).toString().trim());
                }
            } else {
                Enumeration<String> enums = properties.keys();
                while (enums.hasMoreElements()) {
                    String key = enums.nextElement();
                    if (excludes.contains(key)) {
                        continue;
                    }
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(key);
                    buf.append("=");
                    buf.append(properties.get(key).toString().trim());
                }
            }
        }
    }
    public static URL toAddressURL(Dictionary<String, ?> properties){
        try {
            return new java.net.URL(toAddress(properties));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        
    }
    
    public static Dictionary<String, Object> toDictionary(ServiceProperties sp){
        if(sp==null){
            return null;
        }
        Dictionary<String, Object> dic = new Hashtable<String, Object>();
        String[] keys = sp.getPropertyKeys();
        for(String key:keys){
            dic.put(key, sp.getProperty(key));
        }
        return dic;
    }
    
    
    public static Map<String,Object> toMap(Dictionary<String, ?> dic){
        if(dic==null){
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        Enumeration<String> e=  dic.keys();
        while(e.hasMoreElements()){
            String key = e.nextElement();
            map.put(key, dic.get(key));
        }
        return  map;
    }
    
    
    
    
    public static Dictionary<String, Object> toProperties(String address){
        return toProperties(address,new Hashtable<String, Object>());
    }
    
    public static Dictionary<String, Object> toProperties(String address,Dictionary<String, Object> parameters){
        if (address == null || (address = address.trim()).length() == 0) {
            throw new IllegalArgumentException("address == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        int i = address.indexOf("?");
        if (i >= 0) {
            String[] parts = address.substring(i + 1).split("\\&");
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            address = address.substring(0, i);
        }
        i = address.indexOf("://");
        if (i >= 0) {
            if (i == 0)
                throw new IllegalStateException("address missing protocol: \"" + address + "\"");
            protocol = address.substring(0, i);
            address = address.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = address.indexOf(":/");
            if (i >= 0) {
                if (i == 0)
                    throw new IllegalStateException("address missing protocol: \"" + address + "\"");
                protocol = address.substring(0, i);
                address = address.substring(i + 1);
            }
        }

        i = address.indexOf("/");
        if (i >= 0) {
            path = address.substring(i + 1);
            address = address.substring(0, i);
        }
        i = address.indexOf("@");
        if (i >= 0) {
            username = address.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            address = address.substring(i + 1);
        }
        i = address.indexOf(":");
        if (i >= 0 && i < address.length() - 1) {
            port = Integer.parseInt(address.substring(i + 1));
            address = address.substring(0, i);
        }
        if (address.length() > 0)
            host = address;
        if(protocol!=null){
            parameters.put(HOLA.PROTOCOL_KEY, protocol);
        }
       if(username!=null){
           parameters.put(HOLA.USER_KEY, username);
       }
       if(password!=null){
           parameters.put(HOLA.PASSWORD_KEY, password);
       }
       if(host!=null){
           parameters.put(HOLA.HOST_KEY, host);
       }
       if(port!=0){
           parameters.put(HOLA.PORT_KEY, port);
       }
       if(path!=null){
           parameters.put(HOLA.PATH_KEY, path);
       }
        return parameters;
    }

    public static String getString(Dictionary<String, ?> properties,String key){
        Object value = properties.get(key);
        if (value == null || value.toString().length() == 0) {
            value = properties.get(HOLA.DEFAULT_KEY_PREFIX + key);
        }
        return value==null?null:value.toString();
    }
    public static String getStringAndDecoded(Dictionary<String, ?> properties,String key) {
        return getStringAndDecoded(properties,key, null);
    }
    
    public  static String getStringAndDecoded(Dictionary<String, ?> properties,String key, String defaultValue) {
        return decode(getString(properties,key, defaultValue));
    }

    /**
     * @param key
     * @param defaultValue
     * @return return key's value if value is null return defaultValue.
     */
    public static String getString(Dictionary<String, ?> properties,Object key, String defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value.toString();
        }
    }

    public static Dictionary<String, ?> getSubtree(Dictionary<String, ?> properties,String key) {
        return getSubtreePrefixed(key, properties);
    }

    public static Dictionary<String, ?> getSubtreePrefixed(String prefix, Dictionary<String, ?> data) {
        if (prefix == null || data == null)
            return null;
        if (prefix.length() == 0)
            return data;
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        Enumeration<String> en = data.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (key.startsWith(prefix + "."))
                result.put(key.substring(prefix.length() + 1), data.get(key));
        }
        return result;
    }
    /**
     * @param key
     * @param defaultValue
     * @return return key's value if value is null return defaultValue.
     */
    public static String[] getStringArray(Dictionary<String, ?> properties,String key, String defaultValue[]) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        else
            return (String[]) listToArray(getList(properties,key));
    }

    /**
     * Gets the map being decorated.
     * 
     * @return the decorated map
     */
    public static DataTypeMap getMap(Dictionary<String, ?> properties,String key) {
        return getMap(properties,key, null);
    }

    /**
     * Gets the map being decorated.
     * 
     * @param key
     * @param defaultValue
     * @return if value is null return default value.
     */
    @SuppressWarnings("unchecked")
    public static DataTypeMap getMap(Dictionary<String, ?> properties,String key, Map<String, Object> defaultValue) {
        Object value = properties.get(key);
        if (value instanceof DataTypeMap) {
            return (DataTypeMap) value;
        }
        if (value instanceof Map<?, ?>) {
            return new DataTypeMap((Map<String, Object>) value);
        }
        if (value == null) {
            if (defaultValue != null) {
                if (defaultValue instanceof DataTypeMap) {
                    return (DataTypeMap) defaultValue;
                } else {
                    return new DataTypeMap(defaultValue);
                }
            } else {
                return null;
            }
        } else {
            return new DataTypeMap((Map<String, Object>) value);
        }
    }

    /**
     * @param key
     * @return
     */
    public static List<?> getList(Dictionary<String, ?> properties,String key) {
        return getList(properties,key, null);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static List<?> getList(Dictionary<String, ?> properties,String key, List<?> defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof List<?>)
            return (List<?>) value;
        List<String> result = new ArrayList<String>();
        for (StringTokenizer st = new StringTokenizer(value.toString().trim(),
            " \r\t\n,"); st.hasMoreTokens(); result.add(st.nextToken().toString().trim()))
            ;
        return result;
    }

    /**
     * put comma separated objects into a List
     * 
     * @param key
     * @return
     */
    public static List<?> getCommaSeparatedList(Dictionary<String, ?> properties,String key) {
        return getCommaSeparatedList(properties,key, null);
    }

    /**
     * put comma separated objects into a List
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static List<?> getCommaSeparatedList(Dictionary<String, ?> properties,String key, List<?> defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof List<?>)
            return (List<?>) value;
        else
            return commaSeparatedStringToList(value.toString().trim());
    }

    
    /**
     * Provided a flexible way to get boolean value
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(Dictionary<String, ?> properties,String key, boolean defaultValue) {
        return getBoolean(properties,key, new Boolean(defaultValue)).booleanValue();
    }

    /**
     * Provided a flexible way to get boolean value
     * 
     * @param key
     * @return
     */
    public static Boolean getBoolean(Dictionary<String, ?> properties,String key) {
        return getBoolean(properties,key, ((Boolean) (null)));
    }

    /**
     * Provided a flexible way to get boolean value
     * <p>
     * NOTE:String value "true","yes" return true; "false","no" return false.
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static Boolean getBoolean(Dictionary<String, ?> properties,String key, Boolean defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Boolean)
            return (Boolean) value;
        String s = value.toString().toLowerCase().trim();
        if (s.equals("true") || s.equals("yes"))
            return new Boolean(true);
        if (s.equals("false") || s.equals("no"))
            return new Boolean(false);
        else
            return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public static Byte getByte(Dictionary<String, ?> properties,String key) {
        return getByte(properties,key, ((Byte) (null)));
    }
    public static char getChar(Dictionary<String, ?> properties,String key){
        return getChar(properties,key,(Character) null);
    }
    public static char getChar(Dictionary<String, ?> properties,String key,Character defaultValue){
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Character)
            return (Character) value;
        else if(value.toString().trim().length()>1){
            return  value.toString().trim().charAt(0);
        }else{
            return (char)0;
        }
           
    }
    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static byte getByte(Dictionary<String, ?> properties,String key, byte defaultValue) {
        return getByte(properties,key, new Byte(defaultValue)).byteValue();
    }
    /**
     * 取正数
     * @param properties
     * @param key
     * @param defaultValue
     * @return
     */
    public static byte getPositiveByte(Dictionary<String, ?> properties,String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value= getByte(properties,key, new Byte(defaultValue)).byteValue();
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }
    
    /**
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static Byte getByte(Dictionary<String, ?> properties,String key, Byte defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Byte)
            return (Byte) value;
        else
            return new Byte(value.toString().trim());
    }

    public static short getShort(Dictionary<String, ?> properties,String key, short defaultValue) {
        return getShort(properties,key, new Short(defaultValue)).shortValue();
    }

    /**
     * @param key
     * @return
     */
    public static Short getShort(Dictionary<String, ?> properties,String key) {
        return getShort(properties,key, ((Short) (null)));
    }

    /**
     * get short type object if the key equal null return defaultValue
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static Short getShort(Dictionary<String, ?> properties,String key, Short defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Short)
            return (Short) value;
        else
            return new Short(value.toString().trim());
    }

    /**
     * @param key
     * @return
     */
    public static Integer getInt(Dictionary<String, ?> properties,String key) {
        return getInteger(properties,key, ((Integer) (null)));
    }

    /**
     * @param key
     * @return
     */
    public static Integer getInteger(Dictionary<String, ?> properties,String key) {
        return getInteger(properties,key, ((Integer) (null)));
    }

    public static int getInt(Dictionary<String, ?> properties,String key, int defaultValue) {
        return getInteger(properties,key, new Integer(defaultValue)).intValue();
    }
    
    public static int getPositiveInt(Dictionary<String, ?> properties,String key, int defaultValue) {
       
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value= getInteger(properties,key, new Integer(defaultValue)).intValue();
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }
    public static int getInteger(Dictionary<String, ?> properties,String key, int defaultValue) {
        return getInteger(properties,key, new Integer(defaultValue)).intValue();
    }

    public static Integer getInteger(Dictionary<String, ?> properties,String key, Integer defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Integer)
            return (Integer) value;
        else
            return new Integer(value.toString().trim());
    }

    /**
     * @param key
     * @return
     */
    public static Long getLong(Dictionary<String, ?> properties,String key) {
        return getLong(properties,key, ((Long) (null)));
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLong(Dictionary<String, ?> properties,String key, long defaultValue) {
        return getLong(properties,key, new Long(defaultValue)).longValue();
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static Long getLong(Dictionary<String, ?> properties,String key, Long defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Long)
            return (Long) value;
        else
            return new Long(value.toString().trim());
    }

    public static Float getFloat(Dictionary<String, ?> properties,String key) {
        return getFloat(properties,key, ((Float) (null)));
    }

    public static float getFloat(Dictionary<String, ?> properties,String key, float defaultValue) {
        return getFloat(properties,key, new Float(defaultValue)).floatValue();
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static Float getFloat(Dictionary<String, ?> properties,String key, Float defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Float)
            return (Float) value;
        else
            return new Float(value.toString().trim());
    }

    public static Double getDouble(Dictionary<String, ?> properties,String key) {
        return getDouble(properties,key, ((Double) (null)));
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static double getDouble(Dictionary<String, ?> properties,String key, double defaultValue) {
        return getDouble(properties,key, new Double(defaultValue)).doubleValue();
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static Double getDouble(Dictionary<String, ?> properties,String key, Double defaultValue) {
        Object value = properties.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Double)
            return (Double) value;
        else
            return new Double(value.toString().trim());
    }
    public static String encode(String value) {
        if (value == null || value.length() == 0) { 
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static String decode(String value) {
        if (value == null || value.length() == 0) { 
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public static String getServiceInterface(Dictionary<String, ?> properties) {
        String si = getString(properties, HOLA.INTERFACE_KEY);
        if(si==null){
            si= getString(properties, HOLA.PATH_KEY);
        }
        return si;
    }
    /**用于服务链接的字符串，包含backup*/
    public static String getConnectString(Dictionary<String, ?> properties) {
        String host=getString(properties, HOLA.HOST_KEY);
        int port =getInt(properties, HOLA.PORT_KEY,0);
        StringBuilder sb  = new StringBuilder().append(host).append(":").append(port);
        String backs = PropertiesUtils.getString(properties, HOLA.BACKUP_KEY);
        String[] backups = backs==null?null:HOLA.SPLIT_COMMA_PATTERN.split(backs);
        if(!StringUtils.isEmpty(backs)){
            for(String back:backups){
                sb.append(",");
                sb.append(appendDefaultPort(back,port));
            }
        }
        return sb.toString();
    }
    
    private static String appendDefaultPort(String address, int defaultPort) {
        if (address != null && address.length() > 0
                  && defaultPort > 0) {
            int i = address.indexOf(':');
            if (i < 0) {
                return address + ":" + defaultPort;
            } else if (Integer.parseInt(address.substring(i + 1)) == 0) {
                return address.substring(0, i + 1) + defaultPort;
            }
        }
        return address;
    }
    /**在目标数据集中加入目标数据集中不存在，但源数据集中存在的*/
    public static Dictionary<String, ?> copyNotExist(Dictionary<String, Object> source, Dictionary<String, Object> target) {
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }
        Enumeration<String> keys = source.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object texist = target.get(key);
            if (texist == null) {
                target.put(key, source.get(key));
            }
        }
        return target;
    }
    
    public static void filterCopy(Dictionary<String, Object> source, Dictionary<String, Object> target, String... escapes) {
        if (escapes == null || escapes.length == 0) {
            return;
        }
        List<String> escapeList = Arrays.asList(escapes);
        Enumeration<String> keys = source.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if(key.startsWith(HOLA.DIC_HIDDEN_PREFIX)){
                continue;
            }
            if (escapeList.contains(key)) {
                continue;
            }
            target.put(key, source.get(key));
        }
    }
    
    public static String getServiceKey(ServiceProperties serviceProperties) {
        String inf = getServiceInterface(serviceProperties);
        if (inf == null) return null;
        StringBuilder buf = new StringBuilder();
        String group = getString(serviceProperties,HOLA.GROUP_KEY);
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = getString(serviceProperties,HOLA.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }
    
    /**
     * @param address 地址
     * @param defaultDic 地址上附加的默认参数
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Dictionary<String, ?> parseURL(String address,Dictionary<String, Object> defaults){
        if (address == null || address.length() == 0) {
            return null;
        }
        String url;
        if (address.indexOf("://") >= 0) {
            url = address;
        } else {
            String[] addresses = HOLA.SPLIT_COMMA_PATTERN.split(address);
            url = addresses[0];
            if (addresses.length > 1) {
                StringBuilder backup = new StringBuilder();
                for (int i = 1; i < addresses.length; i++) {
                    if (i > 1) {
                        backup.append(",");
                    }
                    backup.append(addresses[i]);
                }
                url += "?" + HOLA.BACKUP_KEY + "=" + backup.toString();
            }
        }
        Object defaultProtocol = defaults == null ? null : defaults.get(HOLA.PROTOCOL_KEY);
        if (defaultProtocol == null) {
            defaultProtocol = "hola";
        }
        defaults.put(HOLA.PROTOCOL_KEY, defaultProtocol);
        Dictionary<String, ?> prop=  toProperties(url);
        copyNotExist( defaults,(Dictionary<String, Object>)prop);
        return prop;
    }
    
    public static List<Dictionary<String, ?>> parseURLs(String address, Dictionary<String, ?> defaults) {
        if (address == null || address.length() == 0) {
            return null;
        }
        String[] addresses = HOLA.DISCOVERY_SPLIT_PATTERN.split(address);
        if (addresses == null || addresses.length == 0) {
            return null; //here won't be empty
        }
        List<Dictionary<String, ?>> registries = new ArrayList<Dictionary<String, ?>>();
        for (String addr : addresses) {
            registries.add(parseURL(addr, (Dictionary<String, Object>)defaults));
        }
        return registries;
    }

    /**
     * 如果不存在就添加
     * 
     * @param dic
     * @param key
     * @param value
     */
    public static void putIfAbsent(Dictionary<String, Object> dic, String key, Object value) {
        if (key == null || key.length() == 0 || value == null) {
            return;
        }
        if(dic.get(key)!=null){
            return ;
        }
        dic.put(key, value);
    }
    
    /**
     * 放入Dictionary中,如果不存在则添加,如果存在对象类型为List则add(value),否则按照逗号分隔的字符串处理.
     * 
     * @param dic
     * @param key
     * @param value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void putIfExitAsArray(Dictionary<String, Object> dic, String key, Object value){
        if (key == null || key.length() == 0 || value == null) {
            return;
        }
        Object o = dic.get(key);
        if(o==null){
            dic.put(key, value);
        }else if( o instanceof List){
            ((List)o).add(value);
        }else{
            dic.put(key, new StringBuilder().append( o.toString()).append(",").append(value.toString()).toString());
        }
    }

}
