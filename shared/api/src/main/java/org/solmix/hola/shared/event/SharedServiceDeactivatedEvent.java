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
package org.solmix.hola.shared.event;

import org.solmix.hola.core.event.ConnectEvent;
import org.solmix.hola.core.identity.ID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年6月23日
 */

public class SharedServiceDeactivatedEvent extends ConnectEvent
{

    /**
     * @param source
     * @param type
     * @param localID
     */
    public SharedServiceDeactivatedEvent(Object source, int type, ID localID)
    {
        super(source, type, localID);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param providerID
     * @param serviceID
     */
    public SharedServiceDeactivatedEvent(ID providerID, ID serviceID)
    {
        super(null, 1, providerID);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1403308332523573845L;

}
