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

import org.solmix.hola.discovery.event.ServiceEvent;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月5日
 */

public interface ServiceListener
{

    /**
     * 是否需要re-deiscovery
     */
    public boolean triggerDiscovery();

    /**
     * Notification that a service has been discovered (the service is fully
     * resolved).
     * 
     * @param anEvent Will not be <code>null</code>
     */
    public void discovered(ServiceEvent anEvent);

    /**
     * Notification that a previously discovered service has been undiscovered.
     * 
     * @param anEvent Will not be <code>null</code>
     */
    public void undiscovered(ServiceEvent anEvent);
}