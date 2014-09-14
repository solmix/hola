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

package org.solmix.hola.discovery;

import java.util.Arrays;
import java.util.Comparator;

import org.solmix.commons.util.Assert;
import org.solmix.hola.discovery.identity.ServiceType;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月5日
 */

public class ServiceTypeComparator implements Comparator<ServiceType>
{

    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(ServiceType o1, ServiceType o2) {
        Assert.isNotNull(o1);
        Assert.isNotNull(o2);
        if (o1 == o2) {
            return 0;
        }
        if (o1.equals(o2)) {
            return 0;
        }
        if (o1 instanceof ServiceType && o2 instanceof ServiceType) {
            final ServiceType type1 = o1;
            final ServiceType type2 = o2;

            final String name1 = type1.getNamingAuthority();
            final String name2 = type2.getNamingAuthority();
            if (!name1.equals("*") && !name2.equals("*") 
                && !name1.equals(name2)) {
                return -1;
            }

            final String[] services1 = type1.getServices();
            final String[] services2 = type2.getServices();
            if (!services1[0].equals("*") && !services2[0].equals("*") 
                && !Arrays.equals(services1, services2)) {
                return -1;
            }

            final String[] protocols1 = type1.getProtocols();
            final String[] protocols2 = type2.getProtocols();
            if (!protocols1[0].equals("*") && !protocols2[0].equals("*") 
                && !Arrays.equals(protocols1, protocols2)) {
                return -1;
            }

            final String[] scopes1 = type1.getScopes();
            final String[] scopes2 = type2.getScopes();
            if (!scopes1[0].equals("*") && !scopes2[0].equals("*") //$NON-NLS-1$ //$NON-NLS-2$
                && !Arrays.equals(scopes1, scopes2)) {
                return -1;
            }
            return 0;
        }
        return -1;
    }

}
