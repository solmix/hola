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

package org.solmix.hola.builder;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月30日
 */

public class ArgumentDefinition {

    private Integer index;

    private String type;

    private String callback;

    /**   */
    public Integer getIndex() {
        return index;
    }

    /**   */
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**   */
    public String getType() {
        return type;
    }

    /**   */
    public void setType(String type) {
        this.type = type;
    }

    /**   */
    public String getCallback() {
        return callback;
    }

    /**   */
    public void setCallback(String callback) {
        this.callback = callback;
    }

}
