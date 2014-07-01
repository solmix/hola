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

package org.solmix.hola.shared.transport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年6月25日
 */

public class DisconnectEvent extends SharedConnectionEvent
{

    private final Throwable exception;

    /**
     * @param conn
     * @param data
     */
    public DisconnectEvent(Channel conn, Object data,
        Throwable exception)
    {
        super(conn, data);
        this.exception = exception;
    }

    /**
     * @return the exception
     */
    public Throwable getException() {
        return exception;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Disconnect Event[ connection=")
        .append(getSharedConnection())
        .append(",data=").append(getData())
        .append(",exception=").append(exception)
        .append("]");
        return sb.toString();
    }
}
