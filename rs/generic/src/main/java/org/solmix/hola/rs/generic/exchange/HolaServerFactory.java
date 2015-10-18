/*
 * Copyright 2015 The Solmix Project
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
package org.solmix.hola.rs.generic.exchange;

import org.solmix.exchange.ProtocolFactoryManager;
import org.solmix.hola.rs.ServerFactory;
import org.solmix.hola.rs.interceptor.InBindingInterceptor;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月17日
 */

public class HolaServerFactory extends ServerFactory
{

    private static final long serialVersionUID = -8186449363629929908L;

    /**
     * @param factory
     */
    public HolaServerFactory()
    {
        super(new HolaServiceFactory());
        getInInterceptors().add(new InBindingInterceptor());
    }
    
    @Override
    public void setContainer(Container container){
        super.setContainer(container);
        ProtocolFactoryManager pfm= container.getExtension(ProtocolFactoryManager.class);
        if(pfm!=null){
            ProtocolFactoryImpl pf = new ProtocolFactoryImpl();
            pf.setContainer(container);
            pfm.register(ProtocolFactoryImpl.PROTOCOL_ID, pf);
        }
    }
    
}
