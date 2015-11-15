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

import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.runtime.identity.AbstractNamespace;
import org.solmix.runtime.identity.ID;
import org.solmix.runtime.identity.IDCreateException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月4日
 */

public class DiscoveryNamespace extends AbstractNamespace
{

    private static final long serialVersionUID = 6685461771166498986L;

    public static final String NAME = "namespace.discovery";

    public DiscoveryNamespace()
    {
        super();
    }

    public DiscoveryNamespace(String description)
    {
        super(NAME, description);
    }

    @Override
    public String getScheme() {
        return "discovery";
    }

    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
        if (parameters != null && parameters.length == 1 && parameters[0] instanceof DiscoveryID) {
            return (ID) parameters[0];
        } else if (parameters != null && parameters.length == 2 && parameters[0] instanceof DiscoveryType && parameters[1] instanceof String) {
            final DiscoveryType type = (DiscoveryType) parameters[0];
            final String uri = (String) parameters[1];
            return new DiscoveryServiceID(this, type, uri);
        } else if (parameters != null && parameters.length == 2 && parameters[0] instanceof DiscoveryType && parameters[1] instanceof DiscoveryInfo) {
            final DiscoveryType type = (DiscoveryType) parameters[0];
            final DiscoveryInfo uri = (DiscoveryInfo) parameters[1];
            return new DiscoveryServiceID(this, type, uri);
        }
        throw new IDCreateException("parameters must be [ServiceID] or [ServiceType,URI]");
    }

    private static class DiscoveryServiceID extends DiscoveryIDImpl
    {

        private static final long serialVersionUID = -9017925060137305026L;

        // Need public constructor
        public DiscoveryServiceID(AbstractNamespace namespace, DiscoveryType type, String uri)
        {
            super(namespace, type, uri);
        }

        public DiscoveryServiceID(AbstractNamespace namespace, DiscoveryType type, DiscoveryInfo uri)
        {
            super(namespace, type, uri);
        }
    }
}
