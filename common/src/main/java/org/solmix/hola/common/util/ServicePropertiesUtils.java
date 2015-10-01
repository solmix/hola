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
package org.solmix.hola.common.util;

import java.net.URL;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;


/**
 * 地址和参数之间的转换
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月22日
 */

public class ServicePropertiesUtils
{
    private ServicePropertiesUtils(){
        
    }
    public static String toAddress(Dictionary<String, ?> properties){
        return toAddress(properties,false,true,false);
    }
    /**
     * 根据service配置参数生成Address
     * <li>如果配置address，返回
     * <li>根据protocol://host(IP):port/path?key1=value1&key2=value2
     * @param properties
     * @return
     */
    public static String toAddress(Dictionary<String, ?> properties,boolean appendUser,boolean appendParameter, boolean useIP){
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
        if(StringUtils.isEmpty(host)){
            host=HOLA.LOCALHOST_VALUE;
        }
        if (useIP) {
              host = NetUtils.getIpByHost(host);
        }
        if(!StringUtils.isEmpty(host)){
            buf.append(host);
            String port=String.valueOf(properties.get(HOLA.PORT_KEY));
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
            buildQueryString(buf, true, properties, new String[] { HOLA.ADDRESS_KEY, HOLA.PROTOCOL_KEY, HOLA.HOST_KEY,
                HOLA.PORT_KEY, HOLA.PATH_KEY, HOLA.USER_KEY, HOLA.PASSWORD_KEY });
        }
        return buf.toString();
    }
    
    private static void buildQueryString(StringBuilder buf, boolean concat, Dictionary<String, ?> properties,String[] excluds) {
        if (properties !=null && properties.size() > 0) {
          List<String> excludes = (excluds == null || excluds.length == 0 ? null : Arrays.asList(excluds));
          boolean first = true;
          Enumeration<String> enums= properties.keys();
          while(enums.hasMoreElements()){
              String key = enums.nextElement();
              if(excludes.contains(key)){
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

    public static URL toAddressURL(Dictionary<String, ?> properties){
        return null;
        
    }
    
    public static Dictionary<String, ?> toProperties(String address){
        if (address == null || (address = address.trim()).length() == 0) {
            throw new IllegalArgumentException("address == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Dictionary<String, Object> parameters = null;
        int i = address.indexOf("?");
        if (i >= 0) {
            String[] parts = address.substring(i + 1).split("\\&");
            parameters = new Hashtable<String, Object>();
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
        parameters.put(HOLA.PROTOCOL_KEY, protocol);
        parameters.put(HOLA.USER_KEY, username);
        parameters.put(HOLA.PASSWORD_KEY, password);
        parameters.put(HOLA.HOST_KEY, host);
        parameters.put(HOLA.PORT_KEY, port);
        parameters.put(HOLA.PATH_KEY, path);
        return parameters;
    }

}
