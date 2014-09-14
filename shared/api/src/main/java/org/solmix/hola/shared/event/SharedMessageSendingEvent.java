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
 * @version 0.0.1 2014年5月20日
 */

public class SharedMessageSendingEvent extends ConnectEvent
{

    private static final long serialVersionUID = -3829952100186953163L;

    public static final int TYPE = 0x00000011;

    private final ID targetID;

    private final ID serviceID;

    private final Object message;

    /**
     * @param source
     * @param type
     * @param localID
     */
    public SharedMessageSendingEvent(Object source, ID localID, ID targetID,
        ID serviceID, Object message)
    {
        super(source, TYPE, localID);
        this.targetID = targetID;
        this.serviceID = serviceID;
        this.message = message;
    }

    /**
     * @return the targetID
     */
    public ID getTargetID() {
        return targetID;
    }

    /**
     * @return the serviceID
     */
    public ID getServiceID() {
        return serviceID;
    }

    public Object getMessage() {
        return message;
    }

}
