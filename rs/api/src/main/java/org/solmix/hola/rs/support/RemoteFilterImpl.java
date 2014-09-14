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
package org.solmix.hola.rs.support;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.ServiceReference;
import org.solmix.hola.rs.RemoteFilter;
import org.solmix.hola.rs.RemoteServiceReference;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月1日
 */

public class RemoteFilterImpl implements RemoteFilter
{

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.Filter#match(org.osgi.framework.ServiceReference)
     */
    @Override
    public boolean match(ServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.Filter#match(java.util.Dictionary)
     */
    @Override
    public boolean match(Dictionary<String, ?> dictionary) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.Filter#matchCase(java.util.Dictionary)
     */
    @Override
    public boolean matchCase(Dictionary<String, ?> dictionary) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.Filter#matches(java.util.Map)
     */
    @Override
    public boolean matches(Map<String, ?> map) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteFilter#match(org.solmix.hola.rs.RemoteServiceReference)
     */
    @Override
    public boolean match(RemoteServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return false;
    }

}
