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

import org.solmix.hola.rs.RSResponse;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月25日
 */

public class RSResponseImpl implements RSResponse, Serializable
{
    private static final long serialVersionUID = 9058448837652914452L;

    private Object                   value;

    private Throwable                exception;

    private final Map<String, String>      properties = new HashMap<String, String>();
    public RSResponseImpl(){
    }
   public RSResponseImpl(Object value){
        this.value=value;
    }
    
   public RSResponseImpl(Throwable exception){
        this.exception=exception;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RSResponse#getValue()
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RSResponse#getException()
     */
    @Override
    public Throwable getException() {
        return exception;
    }

    
    /**
     * @param exception the exception to set
     */
    public void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RSResponse#hasException()
     */
    @Override
    public boolean hasException() {
        return exception!=null;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }
  
    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String v= properties.get(key);
        if(v==null||v.trim().equals("")){
            return defaultValue;
        }
        return v;
    }

}
