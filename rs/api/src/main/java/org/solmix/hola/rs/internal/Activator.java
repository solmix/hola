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
package org.solmix.hola.rs.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.solmix.hola.rs.RSProviderManager;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月28日
 */

public class Activator implements BundleActivator
{
    private BundleContext bundleContext;
    private static Activator instance;
    
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.bundleContext=context;
        Activator.instance=this;
        
    }
    public static synchronized Activator getDefault(){
        if(instance==null)
            return new Activator();
        return instance;
    }

    
    /**
     * @return the bundleContext
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        RSProviderManager.close();

    }

}
