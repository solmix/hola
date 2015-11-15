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

import org.solmix.hola.discovery.identity.DiscoveryTypeImpl;
import org.solmix.runtime.identity.AbstractNamespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月7日
 */

public class JmDNSServiceType extends DiscoveryTypeImpl
{

    /**
     * 
     */
    private static final long serialVersionUID = -8977443589652118890L;

    /**
     * @param ns
     * @param type
     */
    public JmDNSServiceType(AbstractNamespace ns, String type)
    {
        super(ns, type);
    }

    @Override
    public String getInternal() {
        final StringBuffer buf = new StringBuffer();
        buf.append("_");
        for (int i = 0; i < getScopes().length; i++) {
            buf.append(getScopes()[i]);
            buf.append(DELIM);
        }

        buf.append(getProtocols()[0]);
        buf.append(".");

        buf.append(getScopes()[0]);
        buf.append(".");

        return buf.toString();
    }
}
