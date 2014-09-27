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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月6日
 */

public class AbstractConfig implements Serializable
{
    private static final long serialVersionUID = -3105914257348322382L;
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

    private static final int MAX_LENGTH = 100;

    private static final int MAX_PATH_LENGTH = 200;

    private static final Pattern PATTERN_NAME = Pattern.compile("[\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_MULTI_NAME = Pattern.compile("[,\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_METHOD_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*");
    
    private static final Pattern PATTERN_PATH = Pattern.compile("[/\\-$._0-9a-zA-Z]+");

    private static final Pattern PATTERN_NAME_HAS_SYMBOL = Pattern.compile("[:*,/\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_KEY = Pattern.compile("[*,\\-._0-9a-zA-Z]+");

    protected Container container;
    
    protected String id;
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    protected static void checkProperty(String property, String value, int maxlength, Pattern pattern) {
        if (value == null || value.length() == 0) {
            return;
        }
        if(value.length() > maxlength){
            throw new IllegalStateException("Invalid " + property + "=\"" + value + "\" is longer than " + maxlength);
        }
        if (pattern != null) {
            Matcher matcher = pattern.matcher(value);
            if(! matcher.matches()) {
                throw new IllegalStateException("Invalid " + property + "=\"" + value + "\" contain illegal charactor, only digit, letter, '-', '_' and '.' is legal.");
            }
        }
    }
    
    protected static void checkMethodName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_METHOD_NAME);
    }
    
    protected static void checkName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_NAME);
    }
    
    protected static void checkLength(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, null);
    }
    
    protected static void checkMultiName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_MULTI_NAME);
    }
    
    protected static void checkPathName(String property, String value) {
        checkProperty(property, value, MAX_PATH_LENGTH, PATTERN_PATH);
    }
    
    protected  void checkExtension(Class<?> type,String property, String value) {
    	if(container==null){
    		throw new IllegalStateException("Container is null");
    	}
    	checkName(property, value);
    	if(value != null&&value.length() > 0 
    			&& ! container.getExtensionLoader(type).hasExtension(value)){
    		throw new IllegalStateException("No such extension " + value + " for " + property + "/" + type.getName());
    	}
    }
    
    protected static boolean booleanValue(Boolean b){
        if(b!=null&&b.booleanValue())
            return true;
        return false;
    }
    
    protected static void appendProperties(Map<String, Object> props, Object config) {
        appendProperties(props, config, null);
    }
    @SuppressWarnings("unchecked")
    protected static void appendProperties(Map<String, Object> props, Object config, String prefix) {
        if(config==null)
            return;
        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();
                if ((name.startsWith("get") || name.startsWith("is")) 
                        && ! "getClass".equals(name)
                        && Modifier.isPublic(method.getModifiers()) 
                        && method.getParameterTypes().length == 0
                        && isPrimitive(method.getReturnType())) {
                    Property parameter = method.getAnnotation(Property.class);
                    if (method.getReturnType() == Object.class || parameter != null && parameter.excluded()) {
                        continue;
                    }
                    int i = name.startsWith("get") ? 3 : 2;
                    String prop = StringUtils.camelToSplitName(name.substring(i, i + 1).toLowerCase() + name.substring(i + 1), ".");
                    String key;
                    if (parameter != null && parameter.key() != null && parameter.key().length() > 0) {
                        key = parameter.key();
                    } else {
                        key = prop;
                    }
                    Object value = method.invoke(config, new Object[0]);
                    String str = String.valueOf(value).trim();
                    if (value != null && str.length() > 0) {
                        if (parameter != null && parameter.escaped()) {
                            str = encode(str);
                        }
                        if (prefix != null && prefix.length() > 0) {
                            key = prefix + "." + key;
                        }
                        props.put(key, str);
                    } else if (parameter != null && parameter.required()) {
                        throw new IllegalStateException(config.getClass().getSimpleName() + "." + key + " == null");
                    }
                    //FIXME
                }else if ("getParameters".equals(name)
                    && Modifier.isPublic(method.getModifiers()) 
                    && method.getParameterTypes().length == 0
                    && method.getReturnType() == Map.class) {
                Map<String, String> map = (Map<String, String>) method.invoke(config, new Object[0]);
                if (map != null && map.size() > 0) {
                    String pre = (prefix != null && prefix.length() > 0 ? prefix + "." : "");
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        props.put(pre + entry.getKey().replace('-', '.'), entry.getValue());
                    }
                }
            }
            }catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
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
    private static boolean isPrimitive(Class<?> type) {
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
	/**
	 * @return the container
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(Container container) {
		this.container = container;
	}
    
}
