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

package org.solmix.hola.rs.generic;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.rs.RemoteServiceReference;
import org.solmix.hola.rs.RemoteServiceRegistration;
import org.solmix.hola.rs.identity.RemoteServiceID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月1日
 */

public class HolaRemoteServiceRegistration<S> implements
    RemoteServiceRegistration<S>, java.io.Serializable
{

    private static final long serialVersionUID = 319786149652327809L;

    protected RemoteServiceID id;

    protected Properties properties;

    transient protected Object registrationLock = new Object();

    /** The registration state */
    protected int state = REGISTERED;

    public static final int REGISTERED = 0x00;

    public static final int UNREGISTERING = 0x01;

    public static final int UNREGISTERED = 0x02;

    protected transient HolaRemoteServiceReference<S> reference;

    private int serviceRanking;

    private String[] clazzes;

    @Override
    public RemoteServiceID getRemoteServiceID() {
        return id;
    }

    @Override
    public ID getProviderID() {
        return (id == null) ? null : id.getProviderID();
    }

    @Override
    public Object getProperty(String key) {
        return properties.getProperty(key);
    }
    
 

    @Override
    public void setProperties(Map<String, Object> properties) {
        synchronized (registrationLock) {
            /* in the process of unregistering */
            if (state != REGISTERED) {
                throw new IllegalStateException("Service already registered"); //$NON-NLS-1$
            }
            this.properties = createProperties(properties);
        }
        // XXX Need to notify that registration modified
    }

    /**
     * 添加额外的参数
     */
    private Properties createProperties(Map<String, ?> props) {
        final Properties resultProps = new Properties(props);

        resultProps.setProperty(HolaConstants.REMOTE_OBJECTCLASS, clazzes);

        resultProps.setProperty(HolaConstants.REMOTE_SERVICE_ID, new Long(
            getRemoteServiceID().getRelativeID()));

        final Object ranking = (props == null) ? null
            : props.get(HolaConstants.REMOTE_RANKING);

        serviceRanking = (ranking instanceof Integer) ? ((Integer) ranking).intValue()
            : 0;

        return resultProps;
    }

    @Override
    public String[] getPropertyKeys() {
        return properties.getPropertyKeys();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceRegistration#getReference()
     */
    @Override
    public RemoteServiceReference<S> getReference() {
        if (reference == null) {
            synchronized (this) {
                reference = new HolaRemoteServiceReference<S>(this);
            }
        }
        return reference;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceRegistration#unregister()
     */
    @Override
    public void unregister() {
       if(provider!=null)
           provider.unregisterRemoteService(this);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // TODO
        return sb.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (!(o.getClass().equals(this.getClass())))
            return false;
        return getRemoteServiceID().equals(((HolaRemoteServiceRegistration<?>) o).getRemoteServiceID());
    }

    @Override
    public int hashCode() {
        return getRemoteServiceID().hashCode();
    }

    /**
     * 存放远程服务的配置信息
     */
    static class Properties extends Hashtable<String, Object>
    {

        private static final long serialVersionUID = -3684607010228779249L;

        private Properties(int size, Map<String, ?> props)
        {
            super((size << 1) + 1);
            if (props != null) {
                synchronized (props) {
                    final Iterator<String> keysEnum = props.keySet().iterator();
                    while (keysEnum.hasNext()) {
                        final Object key = keysEnum.next();
                        if (key instanceof String) {
                            final String header = (String) key;
                            setProperty(header, props.get(header));
                        }
                    }
                }
            }
        }

        protected Properties(Map<String, ?> props)
        {
            this((props == null) ? 2 : Math.max(2, props.size()), props);
        }

        /**
         * Get a clone of the value of a service's property.
         * 
         * @param key header name.
         * @return Clone of the value of the property or <code>null</code> if
         *         there is no property by that name.
         */
        protected Object getProperty(String key) {
            return (cloneValue(get(key)));
        }

        /**
         * Get the list of key names for the service's properties.
         * 
         * @return The list of property key names.
         */
        protected synchronized String[] getPropertyKeys() {
            final int size = size();
            final String[] keynames = new String[size];
            final Enumeration<String> keysEnum = keys();
            for (int i = 0; i < size; i++) {
                keynames[i] = keysEnum.nextElement();
            }
            return (keynames);
        }

        /**
         * Put a clone of the property value into this property object.
         * 
         * @param key Name of property.
         * @param value Value of property.
         * @return previous property value.
         */
        protected synchronized Object setProperty(String key, Object value) {
            return (put(key, cloneValue(value)));
        }

        /**
         * Attempt to clone the value if necessary and possible.
         * 
         * For some strange reason, you can test to see of an Object is
         * Cloneable but you can't call the clone method since it is protected
         * on Object!
         * 
         * @param value object to be cloned.
         * @return cloned object or original object if we didn't clone it.
         */
        protected static Object cloneValue(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                return (value);
            }

            final Class<?> clazz = value.getClass();
            if (clazz.isArray()) {
                // Do an array copy
                final Class<?> type = clazz.getComponentType();
                final int len = Array.getLength(value);
                final Object clonedArray = Array.newInstance(type, len);
                System.arraycopy(value, 0, clonedArray, 0, len);
                return clonedArray;
            }
            // must use reflection because Object clone method is protected!!
            try {
                return (clazz.getMethod("clone", (Class[]) null).invoke(value,
                    (Object[]) null));
            } catch (final Exception e) {
                /* clone is not a public method on value's class */
            } catch (final Error e) {
                /* JCL does not support reflection; try some well known types */
                if (value instanceof Vector<?>) {
                    return (((Vector<?>) value).clone());
                }
                if (value instanceof Hashtable<?, ?>) {
                    return (((Hashtable<?, ?>) value).clone());
                }
            }
            return (value);
        }

        /**
         * 剔除{@link HolaConstants#REMOTE_OBJECTCLASS}
         */
        @Override
        public synchronized String toString() {
            final String keys[] = getPropertyKeys();
            final int size = keys.length;
            final StringBuffer sb = new StringBuffer(20 * size);
            sb.append('{');
            int n = 0;
            for (int i = 0; i < size; i++) {
                final String key = keys[i];
                if (!key.equals(HolaConstants.REMOTE_OBJECTCLASS)) {
                    if (n > 0) {
                        sb.append(", ");
                    }
                    sb.append(key);
                    sb.append('=');
                    final Object value = get(key);
                    if (value.getClass().isArray()) {
                        sb.append('[');
                        final int length = Array.getLength(value);
                        for (int j = 0; j < length; j++) {
                            if (j > 0) {
                                sb.append(',');
                            }
                            sb.append(Array.get(value, j));
                        }
                        sb.append(']');
                    } else {
                        sb.append(value);
                    }
                    n++;
                }
            }
            sb.append('}');
            return (sb.toString());
        }
    }

    protected String[] getClasses() {
        return clazzes;
    }

    private Object service;

    private RemoteServiceID remoteServiceID;
    
    private HolaRemoteServiceProvider provider;

    public void publish(HolaRemoteServiceProvider provider,
        RemoteServiceRegistry registry, String[] clazzes, Object service,
        Map<String, ?> properties) {
        this.provider=provider;
        this.service = service;
        this.reference = new HolaRemoteServiceReference<S>(this);
        this.clazzes = clazzes;
        synchronized (registry) {
            ID id = registry.getProviderID();
            if (id == null)
                throw new NullPointerException(
                    "Local ProviderID must be non-null to register remote services");
            this.remoteServiceID = registry.createRemoteServiceID(registry.getNextServiceId());
            this.properties = createProperties(properties);
            registry.publishService(this);
        }

    }

}
