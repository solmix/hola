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

package org.solmix.hola.common.event;

import java.util.EventObject;

import org.solmix.runtime.event.Event;
import org.solmix.runtime.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月4日
 */

public abstract class ConnectEvent extends EventObject implements Event
{

    private static final long serialVersionUID = 3193099827006507969L;

    private final int type;

    public final static int CONNECTING = 0x00000001;

    public final static int CONNECTED = 0x00000002;

    public final static int DISCONNECTING = 0x00000003;

    public final static int DISCONNECTED = 0x00000004;

    public final static int DESTROY = 0x00000005;

    public final static int EJECTED = 0x00000006;

    private final ID localID;

    /**
     * @param source
     */
    public ConnectEvent(Object source, int type, ID localID)
    {
        super(source);
        this.type = type;
        this.localID = localID;
    }

    public int getType() {
        return type;
    }

    ID getLocalID() {
        return localID;
    }

}
