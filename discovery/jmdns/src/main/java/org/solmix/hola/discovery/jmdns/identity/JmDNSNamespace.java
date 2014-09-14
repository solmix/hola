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

package org.solmix.hola.discovery.jmdns.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.solmix.hola.core.identity.AbstractNamespace;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.discovery.identity.ServiceType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月7日
 */

public class JmDNSNamespace extends AbstractNamespace
{

    private static final long serialVersionUID = 4446156336083869995L;

    public static final String NAME = "namespace.jmdns";

    public static final String SCHEME = "jmdns";

    /**
     * @param string
     */
    public JmDNSNamespace(String discription)
    {
        super(NAME, discription);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Namespace#getScheme()
     */
    @Override
    public String getScheme() {
        return SCHEME;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Namespace#createID(java.lang.Object[])
     */
    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
        if (parameters == null || parameters.length < 1
            || parameters.length > 2) {
            throw new IDCreateException(
                "EndpointInfo cannot be null and must be of length 1 or 2");
        }
        String type = null;
        if (parameters[0] instanceof String) {
            final String arg = (String) parameters[0];
            if (arg.startsWith(getScheme() + Namespace.SCHEME_SEPARATOR)) {
                final int index = arg.indexOf(Namespace.SCHEME_SEPARATOR);
                if (index >= arg.length())
                    type = null;
                type = arg.substring(index + 1);
            }
        }
        if (type == null) {
            if (parameters[0] instanceof ServiceType) {
                type = ((ServiceType) parameters[0]).getName();
            } else if (parameters[0] instanceof String) {
                type = (String) parameters[0];
            } else
                throw new IDCreateException(
                    "Service type id parameter has to be of type String or IServiceTypeID");

        }
        JmDNSServiceType jtype = new JmDNSServiceType(this, type);
        if (parameters.length == 1)
            return jtype;
        else if (parameters[1] instanceof String) {
            try {
                final URI uri = new URI((String) parameters[1]);
                return new JmDNSServiceID(this, jtype, uri);
            } catch (URISyntaxException e) {
                throw new IDCreateException(
                    "Second parameter as String must follow URI syntax"); 
            }
        } else if (parameters[1] instanceof URI) {
            return new JmDNSServiceID(this, jtype, (URI) parameters[1]);
        } else {
            throw new IDCreateException(
                "Second parameter must be of either String or URI type"); 
        }
    }

    @Override
    public Class<?>[][] getSupportedParameterTypes() {
        return new Class[][] { { String.class }, { String.class, String.class } };
    }
}
