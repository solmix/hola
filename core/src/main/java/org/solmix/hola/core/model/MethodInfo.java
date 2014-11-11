/*
 * Copyright 2014 The Solmix Project
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

package org.solmix.hola.core.model;

import java.util.List;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月28日
 */

public class MethodInfo extends AbstractMethodInfo
{

    /**    */
    private static final long serialVersionUID = 1308437448372357031L;

    private String name;

    private Boolean retry;

    private Integer executes;

    private List<ArgumentInfo> arguments;

    
    /**   */
    public String getName() {
        return name;
    }

    
    /**   */
    public void setName(String name) {
        checkName("name", name);
        this.name = name;
        if (id == null || id.length() == 0) {
            id = name;
        }
    }

    
    /**   */
    public Boolean getRetry() {
        return retry;
    }

    
    /**   */
    public void setRetry(Boolean retry) {
        this.retry = retry;
    }

    
    /**   */
    public Integer getExecutes() {
        return executes;
    }

    
    /**   */
    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    
    /**   */
    public List<ArgumentInfo> getArguments() {
        return arguments;
    }

    
    /**   */
    public void setArguments(List<ArgumentInfo> arguments) {
        this.arguments = arguments;
    }
    
}
