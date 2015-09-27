/*
 * Copyright 2015 The Solmix Project
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
package org.solmix.hola.transport.identity;

import org.solmix.runtime.identity.AbstractNamespace;
import org.solmix.runtime.identity.ID;
import org.solmix.runtime.identity.IDCreateException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月28日
 */

public class ServerKeyNamespace extends AbstractNamespace
{
    private static final long serialVersionUID = 4277332295215582783L;

    @Override
    public String getScheme() {
        return ServerKeyID.class.getName();
    }
    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
        if (parameters == null || parameters.length != 3) {
            throw new IDCreateException(
                "ID cannot be null and must be of length 2");
        }
        return new ServerKeyID(this, (String) parameters[0], (String) parameters[1],(Integer)parameters[2]);
    }

    @Override
    public Class<?>[][] getSupportedParameterTypes() {
        return new Class[][] { {String.class }, {String.class },{Integer.class } };
    }

}
