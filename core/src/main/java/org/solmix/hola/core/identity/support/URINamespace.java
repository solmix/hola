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
package org.solmix.hola.core.identity.support;

import java.net.URI;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.core.identity.Namespace;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月4日
 */

public class URINamespace extends Namespace
{
    private static final long serialVersionUID = -8242924346471457245L;

    public URINamespace(String name, String desc) {
          super(name, desc);
    }

    public URINamespace() {
          super(URIID.class.getName(), "URIID Namespace"); //$NON-NLS-1$
    }

    @Override
    public ID createID(Object[] parameters) throws IDCreateException {
          try {
                String init = getInitStringFromQueryString(parameters);
                if (init != null)
                      return new URIID(this, new URI(init));
                if (parameters[0] instanceof URI)
                      return new URIID(this, (URI) parameters[0]);
                if (parameters[0] instanceof String)
                      return new URIID(this, new URI((String) parameters[0]));
                throw new IDCreateException("Cannot create URIID");
          } catch (Exception e) {
                throw new IDCreateException(getName()  + " createInstance()", e); 
          }
    }

    @Override
    public String getScheme() {
          return "uri";
    }

    @Override
    public Class<?>[][] getSupportedParameterTypes() {
          return new Class[][] { { String.class }, { URI.class } };
    }

}
