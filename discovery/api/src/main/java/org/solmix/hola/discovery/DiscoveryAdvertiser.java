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

import org.solmix.hola.discovery.model.DiscoveryInfo;



/**
 * 服务公告
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年4月5日
 */

public interface DiscoveryAdvertiser
{

    /**
     * Register the given service. This publishes the service defined by the
     * serviceInfo to the underlying publishing mechanism
     * 
     * @param serviceInfo
     *            IDiscoveryInfo of the service to be published. Must not be
     *            <code>null</code>.
     */
    public void register(DiscoveryInfo serviceInfo);

    /**
     * Unregister a previously registered service defined by serviceInfo.
     * 
     * @param serviceInfo
     *            IDiscoveryInfo defining the service to unregister. Must not be
     *            <code>null</code>.
     */
    public void unregister(DiscoveryInfo serviceInfo);

    /**
     * Unregister all previously registered service.
     */
    public void unregisterAll();
}
