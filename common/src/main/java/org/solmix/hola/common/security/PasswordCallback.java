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

package org.solmix.hola.common.security;

import java.io.Serializable;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月4日
 */

public class PasswordCallback implements Callback, Serializable
{

    private static final long serialVersionUID = 6940002988125290335L;

    private final String prompt;

    private String defaultPassword;

    private String inputPassword;

    public PasswordCallback(String prompt)
    {
        if (prompt == null)
            throw new IllegalArgumentException("Prompt cannot be null"); //$NON-NLS-1$
        this.prompt = prompt;
    }

    public PasswordCallback(String prompt, String defaultPassword)
    {
        if (prompt == null)
            throw new IllegalArgumentException("Prompt cannot be null"); //$NON-NLS-1$
        this.prompt = prompt;
        this.defaultPassword = defaultPassword;
    }

    /**
     * Get the prompt.
     * 
     * <p>
     * 
     * @return the prompt.
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Get the default password.
     * 
     * <p>
     * 
     * @return the default password, or <code>null</code> if this
     *         <code>PasswordCallback</code> was not instantiated with a
     *         <code>defaultPassword</code>.
     */
    public String getDefaultPassword() {
        return defaultPassword;
    }

    /**
     * Set the retrieved password.
     * 
     * <p>
     * 
     * @param pw the password (which may be null).
     * 
     * @see #getPassword
     */
    public void setPassword(String pw) {
        this.inputPassword = pw;
    }

    /**
     * Get the retrieved password.
     * 
     * <p>
     * 
     * @return the retrieved password (which may be null)
     * 
     * @see #setPassword
     */
    public String getPassword() {
        return inputPassword;
    }
}
