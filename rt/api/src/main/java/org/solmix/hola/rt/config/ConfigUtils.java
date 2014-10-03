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
package org.solmix.hola.rt.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.Version;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.hola.core.HolaConstants;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月28日
 */

public class ConfigUtils
{
    private static final Logger LOG= LoggerFactory.getLogger(ConfigUtils.class);
    public static final String DEFAULT_VERSION="1.0.0";
    
    public static String getVersion(){
        return getVersion(AbstractConfig.class);
    }
    public static String getVersion(Class<?> clazz){
        try{
        String version =Version.readFromMaven("org.solmix.hola", "hola-rt-api");
        if(version==null||version.length()==0){
            CodeSource codeSource =  clazz.getProtectionDomain().getCodeSource();
            if(codeSource!=null){
                String file = codeSource.getLocation().getFile();
                if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                    file = file.substring(0, file.length() - 4);
                    int i = file.lastIndexOf('/');
                    if (i >= 0) {
                        file = file.substring(i + 1);
                    }
                    i = file.indexOf("-");
                    if (i >= 0) {
                        file = file.substring(i + 1);
                    }
                    while (file.length() > 0 && ! Character.isDigit(file.charAt(0))) {
                        i = file.indexOf("-");
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        } else {
                            break;
                        }
                    }
                    version = file;
                }
            }
        }
        return version == null || version.length() == 0 ? DEFAULT_VERSION : version;
        }catch(Exception e){
            LOG.error("Failed get version ,exception:"+e.getMessage());
            return DEFAULT_VERSION;
        }
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
    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() 
                || type == String.class 
                || type == Character.class
                || type == Boolean.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class 
                || type == Long.class
                || type == Float.class 
                || type == Double.class
                || type == Object.class;
    }
    public static String getProperty(String key) {
        return getProperty(key, null);
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && value.length() > 0) {
            return value;
        }
        Properties properties = getProperties();
        return replaceProperty(properties.getProperty(key, defaultValue), (Map)properties);
    }
 private static volatile Properties PROPERTIES;
    
    public static Properties getProperties() {
        if (PROPERTIES == null) {
            synchronized (ConfigUtils.class) {
                if (PROPERTIES == null) {
                    String path = System.getProperty(HolaConstants.HOLA_DEFAULT_CONFIG_FILE);
                    if (path == null || path.length() == 0) {
                        path = System.getenv(HolaConstants.HOLA_DEFAULT_CONFIG_FILE);
                        if (path == null || path.length() == 0) {
                            path = HolaConstants.DEFAULT_HOLA_CONFIG_FILE;
                        }
                    }
                    PROPERTIES = ConfigUtils.loadProperties(path, false, true);
                }
            }
        }
        return PROPERTIES;
    }
    private static Pattern VARIABLE_PATTERN = Pattern.compile(
        "\\$\\s*\\{?\\s*([\\._0-9a-zA-Z]+)\\s*\\}?");

  public static String replaceProperty(String expression, Map<String, String> params) {
    if (expression == null || expression.length() == 0 || expression.indexOf('$') < 0) {
        return expression;
    }
    Matcher matcher = VARIABLE_PATTERN.matcher(expression);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) { 
        String key = matcher.group(1);
        String value = System.getProperty(key);
        if (value == null && params != null) {
            value = params.get(key);
        }
        if (value == null) {
            value = "";
        }
        matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(sb);
    return sb.toString();
}
  public static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
      Properties properties = new Properties();
      if (fileName.startsWith("/")) {
          try {
              FileInputStream input = new FileInputStream(fileName);
              try {
                  properties.load(input);
              } finally {
                  input.close();
              }
          } catch (Throwable e) {
              LOG.warn("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage(), e);
          }
          return properties;
      }
      
      List<java.net.URL> list = new ArrayList<java.net.URL>();
      try {
          Enumeration<java.net.URL> urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
          list = new ArrayList<java.net.URL>();
          while (urls.hasMoreElements()) {
              list.add(urls.nextElement());
          }
      } catch (Throwable t) {
          LOG.warn("Fail to load " + fileName + " file: " + t.getMessage(), t);
      }
      
      if(list.size() == 0) {
          if (! optional) {
              LOG.warn("No " + fileName + " found on the class path.");
          }
          return properties;
      }
      
      if(! allowMultiFile) {
          if (list.size() > 1) {
              String errMsg = String.format("only 1 %s file is expected, but %d dubbo.properties files found on class path: %s",
                      fileName, list.size(), list.toString());
              LOG.warn(errMsg);
          }

          // fall back to use method getResourceAsStream
          try {
              properties.load(ClassLoaderUtils.getResource(fileName, ConfigUtils.class).openStream());
          } catch (Throwable e) {
              LOG.warn("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage(), e);
          }
          return properties;
      }
      
      LOG.info("load " + fileName + " properties file from " + list);
      
      for(java.net.URL url : list) {
          try {
              Properties p = new Properties();
              InputStream input = url.openStream();
              if (input != null) {
                  try {
                      p.load(input);
                      properties.putAll(p);
                  } finally {
                      try {
                          input.close();
                      } catch (Throwable t) {}
                  }
              }
          } catch (Throwable e) {
              LOG.warn("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage(), e);
          }
      }
      
      return properties;
  }
}
