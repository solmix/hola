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

import org.solmix.hola.core.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月7日
 */

public class ConnectingEvent extends ConnectEvent
{

    private static final long serialVersionUID = -6155331833096382235L;

    private final ID targetID;

    private final Object data;

    /**
     * @param source
     * @param type
     * @param localID
     */
    public ConnectingEvent(Object source, ID localID, ID targetID, Object data)
    {
        super(source, CONNECTING, localID);
        this.targetID = targetID;
        this.data = data;
    }

    /**
     * @return the targetID
     */
    public ID getTargetID() {
        return targetID;
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

}
