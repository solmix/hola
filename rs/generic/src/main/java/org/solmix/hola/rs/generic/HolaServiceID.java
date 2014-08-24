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
package org.solmix.hola.rs.generic;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.BaseID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.rs.identity.RemoteServiceID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月20日
 */

public class HolaServiceID extends BaseID implements RemoteServiceID
{

    /**
     * 
     */
    private static final long serialVersionUID = 2138033174688783422L;
    private final String url;

    public HolaServiceID(Namespace ns,String url){
        super(ns);
        Assert.isNotNull(url);
        this.url=url;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceCompareTo(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected int namespaceCompareTo(BaseID o) {
        if (o instanceof HolaServiceID) {
            final HolaServiceID other = (HolaServiceID) o;
            final String typename = other.getUrl();
            return getUrl().compareTo(typename);
        }
        return 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceEquals(org.solmix.hola.core.identity.BaseID)
     */
    @Override
    protected boolean namespaceEquals(BaseID o) {
        if(o instanceof HolaServiceID){
            final HolaServiceID other = (HolaServiceID) o;
            if (other.getUrl().equals(getUrl())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceGetName()
     */
    @Override
    protected String namespaceGetName() {
        return url;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.BaseID#namespaceHashCode()
     */
    @Override
    protected int namespaceHashCode() {
        return url.hashCode();
    }
    
    @Override
    public String getUrl(){
        return url;
    }

}
