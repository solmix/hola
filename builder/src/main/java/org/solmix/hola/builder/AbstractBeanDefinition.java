/**
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

package org.solmix.hola.builder;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.commons.util.SystemPropertyAction;
import org.solmix.commons.util.TransformUtils;
import org.solmix.exchange.model.InfoPropertiesSupport;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月24日
 */

public class AbstractBeanDefinition extends InfoPropertiesSupport implements
    Serializable {

    private static final long serialVersionUID = -2463252429465944248L;

    protected static final Logger logger = LoggerFactory.getLogger(AbstractBeanDefinition.class);

    private static final int MAX_LENGTH = 100;

    private static final int MAX_PATH_LENGTH = 200;

    private static final Pattern PATTERN_NAME = Pattern.compile("[\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_MULTI_NAME = Pattern.compile("[,\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_METHOD_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*");

    private static final Pattern PATTERN_PATH = Pattern.compile("[/\\-$._0-9a-zA-Z]+");

    private static final Pattern PATTERN_NAME_HAS_SYMBOL = Pattern.compile("[:*,/\\-._0-9a-zA-Z]+");

    private static final Pattern PATTERN_KEY = Pattern.compile("[*,\\-._0-9a-zA-Z]+");

    private static final String HOLA_PREFIX = "hola";
    private static final String[] SUFFIXS = new String[] {"Definition", "Bean"};
    protected String id;

    @Property(excluded = true)
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    protected static void checkProperty(String property, String value,
        int maxlength, Pattern pattern) {
        if (value == null || value.length() == 0) {
            return;
        }
        if (value.length() > maxlength) {
            throw new IllegalStateException("Invalid " + property + "=\""
                + value + "\" is longer than " + maxlength);
        }
        if (pattern != null) {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalStateException(
                    "Invalid "
                        + property
                        + "=\""
                        + value
                        + "\" contain illegal charactor, only digit, letter, '-', '_' and '.' is legal.");
            }
        }
    }

    protected static void checkMethodName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_METHOD_NAME);
    }

    protected static void checkName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_NAME);
    }

    protected static void checkPathLength(String property, String value) {
        checkProperty(property, value, MAX_PATH_LENGTH, null);
    }

    protected static void checkLength(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, null);
    }

    protected static void checkMultiName(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_MULTI_NAME);
    }

    protected static void checkKey(String property, String value) {
        checkProperty(property, value, MAX_LENGTH, PATTERN_KEY);
    }

    protected static void checkPathName(String property, String value) {
        checkProperty(property, value, MAX_PATH_LENGTH, PATTERN_PATH);
    }
    
    protected static void appendSystemProperties(AbstractBeanDefinition definition) {
        if (definition == null) {
            return;
        }
        String prefix = HOLA_PREFIX+"." + getTagName(definition.getClass()) + ".";
        Method[] methods = definition.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();
                if (name.length() > 3 && name.startsWith("set") && Modifier.isPublic(method.getModifiers()) 
                        && method.getParameterTypes().length == 1 && TransformUtils.isPrimitive(method.getParameterTypes()[0])) {
                    String property = StringUtils.camelToSplitName(name.substring(3, 4).toLowerCase() + name.substring(4), "-");

                    String value = null;
                    if (definition.getId() != null && definition.getId().length() > 0) {
                        String pn = prefix + definition.getId() + "." + property;
                        value = System.getProperty(pn);
                        if(! StringUtils.isBlank(value)) {
                            logger.info("Use System Property " + pn + " to config hola");
                        }
                    }
                    if (value == null || value.length() == 0) {
                        String pn = prefix + property;
                        value = System.getProperty(pn);
                        if(! StringUtils.isBlank(value)) {
                            logger.info("Use System Property " + pn + " to config hola");
                        }
                    }
                    if (value == null || value.length() == 0) {
                        Method getter;
                        try {
                            getter = definition.getClass().getMethod("get" + name.substring(3), new Class<?>[0]);
                        } catch (NoSuchMethodException e) {
                            try {
                                getter = definition.getClass().getMethod("is" + name.substring(3), new Class<?>[0]);
                            } catch (NoSuchMethodException e2) {
                                getter = null;
                            }
                        }
                        if (getter != null) {
                            if (getter.invoke(definition, new Object[0]) == null) {
                                if (definition.getId() != null && definition.getId().length() > 0) {
                                    value = SystemPropertyAction.getProperty(prefix + definition.getId() + "." + property);
                                }
                                if (value == null || value.length() == 0) {
                                    value = SystemPropertyAction.getProperty(prefix + property);
                                }
                            }
                        }
                    }
                    if (value != null && value.length() > 0) {
                        method.invoke(definition, new Object[] {TransformUtils.convertPrimitive(method.getParameterTypes()[0], value)});
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    private static String getTagName(Class<?> cls) {
        String tag = cls.getSimpleName();
        for (String suffix : SUFFIXS) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        tag = tag.toLowerCase();
        return tag;
    }
    
    protected static void appendDictionaries(Dictionary<String, Object> dic, Object config) {
        appendDictionaries(dic, config, null);
    }

    protected static void appendDictionaries(Dictionary<String, Object> dic, Object config, String prefix) {
        if (config == null) {
            return;
        }
        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();
                if ((name.startsWith("get") || name.startsWith("is")) 
                        && ! "getClass".equals(name)
                        && Modifier.isPublic(method.getModifiers()) 
                        && method.getParameterTypes().length == 0
                        && TransformUtils.isPrimitive(method.getReturnType())) {
                    Property parameter = method.getAnnotation(Property.class);
                    if (method.getReturnType() == Object.class || parameter != null && parameter.excluded()) {
                        continue;
                    }
                    int i = name.startsWith("get") ? 3 : 2;
                    String prop = StringUtils.camelToSplitName(name.substring(i, i + 1).toLowerCase() + name.substring(i + 1), HOLA.CAMEL_SPLIT_KEY);
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
                            str = PropertiesUtils.encode(str);
                        }
                        if (parameter != null && parameter.append()) {
                            String pre = (String)dic.get(HOLA.DEFAULT_KEY + "." + key);
                            if (pre != null && pre.length() > 0) {
                                str = pre + "," + str;
                            }
                            pre = (String)dic.get(key);
                            if (pre != null && pre.length() > 0) {
                                str = pre + "," + str;
                            }
                        }
                        if (prefix != null && prefix.length() > 0) {
                            key = prefix + "." + key;
                        }
                        dic.put(key, str);
                    } else if (parameter != null && parameter.required()) {
                        throw new IllegalStateException(config.getClass().getSimpleName() + "." + key + " == null");
                    }
                } else if ("getProperties".equals(name)
                        && Modifier.isPublic(method.getModifiers()) 
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() == Map.class) {
                    Map<String, Object> map = (Map<String, Object>) method.invoke(config, new Object[0]);
                    if (map != null && map.size() > 0) {
                        String pre = (prefix != null && prefix.length() > 0 ? prefix + "." : "");
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            dic.put(pre + entry.getKey().replace('-', '.'), entry.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
