/**
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
 * @version $Id$ 2014年10月29日
 */

public class AbstractReferenceDefinition extends AbstractInterfaceDefinition {

    /**    */
    private static final long serialVersionUID = -8683910932237735582L;

    protected String version;

    protected String group;

    protected Boolean check;

    protected Boolean generic;

    protected String reconnect;

    protected Boolean lazy;

    /**   */
    public String getVersion() {
        return version;
    }

    /**   */
    public void setVersion(String version) {
        checkKey("version", version);
        this.version = version;
    }

    /**   */
    public String getGroup() {
        return group;
    }

    /**   */
    public void setGroup(String group) {
        checkKey("group", group);
        this.group = group;
    }

    /**   */
    public Boolean isCheck() {
        return check;
    }

    /**   */
    public void setCheck(Boolean check) {
        this.check = check;
    }

    /**   */
    public Boolean isGeneric() {
        return generic;
    }

    /**   */
    public void setGeneric(Boolean generic) {
        this.generic = generic;
    }

    /**   */
    public String getReconnect() {
        return reconnect;
    }

    /**   */
    public void setReconnect(String reconnect) {
        this.reconnect = reconnect;
    }

    /**   */
    public Boolean isLazy() {
        return lazy;
    }

    /**   */
    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

}
