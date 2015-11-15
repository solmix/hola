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
package org.solmix.hola.discovery.model;

import java.io.Serializable;
import java.util.Dictionary;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月16日
 */

public interface DiscoveryInfo extends  Serializable
{
	
    /**
     * Get ServiceID for service.
     * 
     * @return ServiceID the serviceID for the service. Will not be <code>null</code>.
     */
    public ServiceID getServiceID();

    /**
     * The priority for the service
     * 
     * Priority: The priority of this target host. A client MUST attempt to contact the target host with the lowest-numbered priority it can reach; 
     * target hosts with the same priority SHOULD be tried in an order defined by the weight field.
     * 
     * @return int the priority. 0 if no priority information for service.
     */
    public int getPriority();

    /**
     * The weight for the service. 0 if no weight information for service.
     * 
     * Weight: A server selection mechanism. The weight field specifies a relative weight for entries with the same priority. 
     * Larger weights SHOULD be given a proportionately higher probability of being selected. 
     *  Domain administrators SHOULD use Weight 0 when there isn't any server selection to do.
     *  In the presence of records containing weights greater than 0, records with weight 0 should have a very small chance of being selected.
     * 
     * @return int the weight
     */
    public int getWeight();

    /**
     * The time to live for the service. -1 if no TTL given for service.
     * 
     * TTL: A time to live (TTL) defining the live time of a service.
     * 
     * @return long the time to live in seconds
     * @since 4.0
     */
    public long getTTL();

    /**
     * Map with any/all properties associated with the service. Properties are
     * assumed to be name/value pairs, both of type String.
     * 
     * @return Map the properties associated with this service.  Will not be <code>null</code>.
     */
    public Dictionary<String, ?> getServiceProperties();
    
    /**
     * A user choose label used for pretty printing this service.
     * 
     * @return A human readable service name. Not used for uniqueness!
     * @since 3.0
     */
    public String getServiceName();
}
