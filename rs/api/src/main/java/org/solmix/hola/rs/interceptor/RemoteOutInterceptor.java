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
package org.solmix.hola.rs.interceptor;

import java.io.OutputStream;
import java.util.List;

import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月24日
 */

public class RemoteOutInterceptor extends PhaseInterceptorSupport<Message>
{

    /**
     * @param phase
     */
    public RemoteOutInterceptor()
    {
        super(Phase.WRITE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        OutputStream out=  message.getContent(OutputStream.class);
       Object o= message.getContent(List.class);
        if(out!=null){
            System.out.println(o);
        }
        
    }

}
