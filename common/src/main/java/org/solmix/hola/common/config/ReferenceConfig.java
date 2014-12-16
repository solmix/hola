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

package org.solmix.hola.common.config;

import org.solmix.hola.common.service.GenericService;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月30日
 */

public class ReferenceConfig extends AbstractReferenceConfig {

    /**    */
    private static final long serialVersionUID = 2328539426745902984L;

    private String interfaceName;

    private Class<?> interfaceClass;

    private String url;

    private ClientConfig client;

    /**   */
    public ClientConfig getClient() {
        return client;
    }

    /**   */
    public void setClient(ClientConfig client) {
        this.client = client;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (id == null || id.length() == 0) {
            id = interfaceName;
        }
    }

    public String getInterface() {
        return interfaceName;
    }

    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class "
                + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? (String) null
            : interfaceClass.getName());
    }

    public Class<?> getInterfaceClass() {
        if (interfaceClass != null) {
            return interfaceClass;
        }
        if (isGeneric() || (getClient() != null && getClient().isGeneric())) {
            return GenericService.class;
        }
        try {
            if (interfaceName != null && interfaceName.length() > 0) {
                this.interfaceClass = Class.forName(interfaceName, true,
                    Thread.currentThread().getContextClassLoader());
            }
        } catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return interfaceClass;
    }

}
