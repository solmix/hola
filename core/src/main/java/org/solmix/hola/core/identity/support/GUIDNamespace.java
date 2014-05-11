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

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDCreateException;
import org.solmix.hola.core.identity.Namespace;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月4日
 */

public class GUIDNamespace extends Namespace
{
    private static final long serialVersionUID = 1041703442049428943L;
    public GUIDNamespace() {
        super(GUID.class.getName(), "GUID Namespace"); //$NON-NLS-1$
  }

  @Override
public ID createID(Object[] args) throws IDCreateException {
        try {
              String init = getInitStringFromQueryString(args);
              if (init != null)
                    return new GUID(this, init);
              if (args == null || args.length <= 0)
                    return new GUID(this);
              else if (args.length == 1 && args[0] instanceof Integer)
                    return new GUID(this, ((Integer) args[0]).intValue());
              else if (args.length == 1 && args[0] instanceof String)
                    return new GUID(this, ((String) args[0]));
              else
                    return new GUID(this);
        } catch (Exception e) {
              throw new IDCreateException(getName() + " createInstance()", e); //$NON-NLS-1$
        }
  }

  @Override
public String getScheme() {
        return GUID.class.getName();
  }

  @Override
public Class<?>[][] getSupportedParameterTypes() {
        return new Class[][] { {}, { Integer.class } };
  }

}
