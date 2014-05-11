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
package org.solmix.hola.discovery.identity;

import org.solmix.hola.core.identity.ID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月5日
 */

public interface ServiceType extends ID
{
    /**
     * Default ECF protocols (will be translated into provider specific representation)
     * @since 3.0
     */
    public static final String[] DEFAULT_PROTO = new String[] {"tcp"}; //$NON-NLS-1$
    /**
     * Default ECF scopes (will be translated into provider specific representation)
     * @since 3.0
     */
    public static final String[] DEFAULT_SCOPE = new String[] {"default"}; //$NON-NLS-1$
    /**
     * Default ECF naming authority (will be translated into provider specific representation)
     * @since 3.0
     */
    public static final String DEFAULT_NA = "iana"; //$NON-NLS-1$

    /*
     * jSLP => getServices()[0]:getServices()[1][.getNamingAuthoriy():getService()[n]
     * jmDNS => _getServices()[0]._getServices()[n]._getProtocol()[0]._getScopes()[0]
     */
    /*
     * jSLP => naming authority (IANA or custom)
     * jmDNS => IANA
     */
    /**
     * @return String Naming Authority for this ServiceType.  Will not be <code>null</code>.
     * If this instance has been created with the provider specific default, this will return
     * {@link IServiceTypeID#DEFAULT_NA} instead.
     */
    public String getNamingAuthority();

    /*
     * jSLP => unknown (0) only known at the service consumer level
     * jmDNS => protocols (udp/ip or tcp/ip or both) (1)
     */
    /**
     * @return String[] of protocols supported.  Will not be <code>null</code>, but may
     * be empty array.
     * If this instance has been created with the provider specific default, this will return
     * {@link IServiceTypeID#DEFAULT_PROTO} instead.
     */
    public String[] getProtocols();

    /*
     * jSLP => Scopes (n)
     * jmDNS => domain (1)
     */
    /**
     * @return The scopes in which this Service is registered.  Will not be <code>null</code>, but may
     * be empty array.
     * If this instance has been created with the provider specific default, this will return
     * {@link IServiceTypeID#DEFAULT_SCOPE} instead!
     */
    public String[] getScopes();

    /*
     * jSLP => abstract and concrete types (n)
     * jmDNS => everything before port (n)
     */

    /**
     * @return The name of the service type.  If the underlying discovery mechanism
     *         supports naming hierarchies, the hierarchy will be returned
     *         flattened as an array.  Will not be <code>null</code>, but may
     *         be empty array.
     */
    public String[] getServices();

    /**
     * Get the internal name of the service type.  Provider implementations may choose
     * to have this return the same value as {@link ID#getName()}, or they may return
     * a different, internal value appropriate to the provider.
     * 
     * @return String internal name for this service type.  Will not return <code>null</code>.
     */
    public String getInternal();
}
