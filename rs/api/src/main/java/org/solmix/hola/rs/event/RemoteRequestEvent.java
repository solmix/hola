/**
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

package org.solmix.hola.rs.event;

import java.util.EventObject;

/**
 * 
 */

public abstract class RemoteRequestEvent extends EventObject {

    private static final long serialVersionUID = -2415013195949875344L;

    private final int type;

    public final static int START = 0x00000001;

    public final static int COMPLETE = 0x00000002;

    private final long requestId;

    /**
     * @param source
     */
    public RemoteRequestEvent(Object source, int type, long requestId) {
        super(source);
        this.type = type;
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

}
