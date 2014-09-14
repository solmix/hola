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

package org.solmix.hola.rs.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.solmix.commons.util.Assert;
import org.solmix.hola.rs.RSRequest;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月17日
 */

public class RSRequestImpl implements RSRequest, Serializable
{

    private static final long serialVersionUID = -5293962379025983974L;

    protected String method;

    protected Object[] parameters;

    protected int timeout;

    private Map<String, String> properties;

    private Class<?>[] parameterTypes;

    public RSRequestImpl()
    {
    }

    public RSRequestImpl(String method, Object[] parameters, int timeout,
        Map<String, String> properties,Class<?>[] parameterTypes)
    {
        this.method = method;
        this.parameters = parameters;
        this.timeout = timeout;
        this.parameterTypes=parameterTypes==null?new Class<?>[0]:parameterTypes;
        this.properties = properties == null ? new HashMap<String, String>()
            : properties;

        Assert.isNotNull(this.method);
    }

    public RSRequestImpl(String method, Object[] parameters,Class<?>[] parameterTypes)
    {
        this(method, parameters, DEFAULT_TIMEOUT, null,parameterTypes);
    }

    public RSRequestImpl(String method, Object[] parameters,Class<?>[] parameterTypes, int timeout)
    {
        this(method, parameters, timeout, null,parameterTypes);
    }

    public RSRequestImpl(String method)
    {
        this(method, null,null);
    }

    @Override
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    protected void setMethod(String method) {
        this.method = method;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * @param args
     */
    public void setParameters(Object[] args) {
        this.parameters = args == null ? new Object[0] : args;

    }

    /**
     * @return
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes == null ? new Class<?>[0]
            : parameterTypes;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @param map
     */
    public void setProperties(Map<String, String> map) {
        this.properties = properties == null ? new HashMap<String, String>()
            : properties;

    }

    @Override
    public String getProperty(String key) {
        if (properties == null)
            return null;
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        if (properties == null)
            properties = new HashMap<String, String>();
        properties.put(key, value);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        if (properties == null)
            return null;
        String v = properties.get(key);
        if (v == null || v.trim().equals("")) {
            return defaultValue;
        }
        return v;
    }

}
