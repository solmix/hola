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

package org.solmix.hola.osgi.rsa;

import java.util.Arrays;
import java.util.Collection;

import org.solmix.hola.common.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月3日
 */

public class RemoteReferenceNotFoundException extends Exception
{

    private static final long serialVersionUID = -4174685192086828376L;

    private final ID targetID;

    private final ID[] idFilter;

    private final Collection<String> interfaces;

    private final String rsFilter;

    public RemoteReferenceNotFoundException(ID targetID, ID[] idFilter,
        Collection<String> interfaces, String rsFilter)
    {
        this.targetID = targetID;
        this.idFilter = idFilter;
        this.interfaces = interfaces;
        this.rsFilter = rsFilter;
    }

    public ID getTargetID() {
        return targetID;
    }

    public ID[] getIdFilter() {
        return idFilter;
    }

    public Collection<String> getInterfaces() {
        return interfaces;
    }

    public String getRsFilter() {
        return rsFilter;
    }

    @Override
    public String toString() {
        return "RemoteReferenceNotFoundException[targetID=" + targetID //$NON-NLS-1$
            + ", idFilter=" + Arrays.toString(idFilter) + ", interfaces=" //$NON-NLS-1$ //$NON-NLS-2$
            + interfaces + ", rsFilter=" + rsFilter + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
