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
package org.solmix.hola.core.security;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月4日
 */

public class UnsupportedCallbackException extends Exception
{
    private static final long serialVersionUID = -1821878324884011143L;

    private final Callback callback;

    /**
     * Constructs a <code>UnsupportedCallbackException</code> with no detail
     * message.
     * 
     * <p>
     * 
     * @param callback
     *            the unrecognized <code>Callback</code>.
     */
    public UnsupportedCallbackException(Callback callback) {
          super();
          this.callback = callback;
    }

    /**
     * Constructs a UnsupportedCallbackException with the specified detail
     * message. A detail message is a String that describes this particular
     * exception.
     * 
     * <p>
     * 
     * @param callback
     *            the unrecognized <code>Callback</code>.
     *            <p>
     * 
     * @param msg
     *            the detail message.
     */
    public UnsupportedCallbackException(Callback callback, String msg) {
          super(msg);
          this.callback = callback;
    }

    /**
     * Get the unrecognized <code>Callback</code>.
     * 
     * <p>
     * 
     * @return the unrecognized <code>Callback</code>.
     */
    public Callback getCallback() {
          return callback;
    }
}
