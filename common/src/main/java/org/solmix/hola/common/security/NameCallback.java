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

public class NameCallback implements Callback, Serializable
{

    private static final long serialVersionUID = 1128888745504063216L;

    private final String prompt;

    private String defaultName;

    private String inputName;

    public NameCallback(String prompt)
    {
        if (prompt == null)
            throw new IllegalArgumentException("Prompt cannot be null"); //$NON-NLS-1$
        this.prompt = prompt;
    }

    /**
     * Construct a <code>NameCallback</code> with a prompt and default name.
     * 
     * <p>
     * 
     * @param prompt the prompt used to request the information.
     *        <p>
     * 
     * @param defaultName the name to be used as the default name displayed with
     *        the prompt.
     * 
     * @exception IllegalArgumentException if <code>prompt</code> is null.
     */
    public NameCallback(String prompt, String defaultName)
    {
        if (prompt == null)
            throw new IllegalArgumentException("Prompt cannot be null"); //$NON-NLS-1$
        this.prompt = prompt;
        this.defaultName = defaultName;
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
     * Get the default name.
     * 
     * <p>
     * 
     * @return the default name, or null if this <code>NameCallback</code> was
     *         not instantiated with a <code>defaultName</code>.
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Set the retrieved name.
     * 
     * <p>
     * 
     * @param name the retrieved name (which may be null).
     * 
     * @see #getName
     */
    public void setName(String name) {
        this.inputName = name;
    }

    /**
     * Get the retrieved name.
     * 
     * <p>
     * 
     * @return the retrieved name (which may be null)
     * 
     * @see #setName
     */
    public String getName() {
        return inputName;
    }
}
