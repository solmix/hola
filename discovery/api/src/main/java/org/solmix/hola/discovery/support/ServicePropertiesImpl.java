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

package org.solmix.hola.discovery.support;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.solmix.hola.discovery.ServiceProperties;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public class ServicePropertiesImpl implements ServiceProperties
{

    private static final long serialVersionUID = -7614114318631158045L;

    private final Hashtable<String,Object> props;

    public ServicePropertiesImpl(Hashtable<String,Object> props)
    {
        this.props = (props == null) ? new Hashtable<String,Object>() : props;
    }

    public ServicePropertiesImpl()
    {
        props = new Hashtable<String,Object>();
    }

    public ServicePropertiesImpl(ServiceProperties sp)
    {
        props = new Hashtable<String,Object>();
        Enumeration<String> names = sp.getPropertyNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            Object value = sp.getProperty(key.toString());
            props.put(key, value);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#getPropertyNames()
     */
    @Override
    public Enumeration<String> getPropertyNames() {
        return props.keys();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#getPropertyString(java.lang.String)
     */
    @Override
    public String getPropertyString(String name) {
        final Object val = props.get(name);
        if (val instanceof String) {
            return (String) val;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#getPropertyBytes(java.lang.String)
     */
    @Override
    public byte[] getPropertyBytes(String name) {
        final Object val = props.get(name);
        if (val instanceof ByteArrayWrapper) {
            ByteArrayWrapper baw = (ByteArrayWrapper) val;
            return baw.getByte();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) {
        return props.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#setPropertyString(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Object setPropertyString(String name, String value) {
        return props.put(name, value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#setPropertyBytes(java.lang.String,
     *      byte[])
     */
    @Override
    public Object setPropertyBytes(String name, byte[] value) {
        return props.put(name, new ByteArrayWrapper(value));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public Object setProperty(String name, Object value) {
        return props.put(name, value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.ServiceProperties#size()
     */
    @Override
    public int size() {
        return props.size();
    }

    private static class ByteArrayWrapper implements Serializable
    {

        private static final long serialVersionUID = -8528836675536956297L;

        private final byte[] value;

        public ByteArrayWrapper(byte[] value)
        {
            this.value = value;
        }

        public byte[] getByte() {
            return value;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ByteArrayWrapper) {
                ByteArrayWrapper baw = (ByteArrayWrapper) obj;
                return Arrays.equals(value, baw.value);
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return value.hashCode();
        }

    }
}
