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
package org.solmix.hola.rs.event;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月1日
 * @param <T>
 */

public class RemoteRequestCompleteEvent<T> extends RemoteRequestEvent
{

    private final T response;
    private final Throwable exception;
    /**
     * @param source
     * @param type
     * @param requestId
     */
    public RemoteRequestCompleteEvent(Object source,  long requestId,T response,Throwable exception)
    {
        super(source,RemoteRequestEvent.COMPLETE, requestId);
        this.response=response;
        this.exception=exception;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3729061673293976413L;
    
    /**
     * @return the response
     */
    public T getResponse() {
        return response;
    }

    
    /**
     * @return the exception
     */
    public Throwable getException() {
        return exception;
    }
    public boolean hadException(){
        return exception!=null;
    }

}
