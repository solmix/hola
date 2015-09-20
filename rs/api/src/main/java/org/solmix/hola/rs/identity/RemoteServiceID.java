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
package org.solmix.hola.rs.identity;

import org.solmix.runtime.identity.BaseID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月17日
 */

public class RemoteServiceID extends BaseID
{

    private static final long serialVersionUID = -5170752509147784456L;

    private final String name;

    private final String serviceNamespace;

    private final String version;
    
    public RemoteServiceID(String serviceNamespace,String name,String version){
        this.name=name;
        this.serviceNamespace=serviceNamespace;
        this.version=version;
    }
    
    @Override
    protected int namespaceCompareTo(BaseID o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected boolean namespaceEquals(BaseID o) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    protected String namespaceGetName() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    protected int namespaceHashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
