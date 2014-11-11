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
package org.solmix.hola.core.security;

import java.security.BasicPermission;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年10月31日
 */

public class HolaServicePermission extends BasicPermission
{

    /**
     * @param name 
     */
    public HolaServicePermission(String name)
    {
        super(name);
    }
    
    public HolaServicePermission(String name, String actions) {
        super(name, actions);
    }
    
    /**    */
    private static final long serialVersionUID = -257225152976956518L;

}
