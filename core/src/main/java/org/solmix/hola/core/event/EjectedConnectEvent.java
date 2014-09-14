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

package org.solmix.hola.core.event;

import java.io.Serializable;

import org.solmix.hola.core.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月7日
 */

public class EjectedConnectEvent extends ConnectEvent
{

    private final ID targetID;

    private final Serializable reason;

    /**
     * @param source
     * @param type
     * @param localID
     */
    public EjectedConnectEvent(Object source, ID localID, ID targetID,
        java.io.Serializable reason)
    {
        super(source, EJECTED, localID);
        this.targetID = targetID;
        this.reason = reason;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7689341648825369438L;

    /**
     * @return the targetID
     */
    public ID getTargetID() {
        return targetID;
    }

    /**
     * @return the reason
     */
    public Serializable getReason() {
        return reason;
    }

}
