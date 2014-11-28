/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.rm.generic;

import javax.annotation.Resource;

import org.solmix.hola.rm.RemoteManager;
import org.solmix.hola.rm.RemoteManagerFactory;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月19日
 */

public class GenericRemoteManagerFactory implements RemoteManagerFactory {

    @Resource
    private Container container;
    
    @Override
    public RemoteManager createManager() {
        return createManager(container);
    }


    @Override
    public RemoteManager createManager(Container container) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    @Override
    public int getDefaultPort() {
        return 1314;
    }


   

}