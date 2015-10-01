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

import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月25日
 */

public class HeaderEncodeInterceptor extends PhaseInterceptorSupport<Message>
{

    public HeaderEncodeInterceptor()
    {
        super(Phase.PRE_PROTOCOL_FRONTEND);
    }

   
    @Override
    public void handleMessage(Message message) throws Fault {
       if(isRequest(message)){
           
       }else{
           
       }
        
        message.getInterceptorChain().add(new HeaderEncodeEndingInterceptor());
        
    }
    
    
    public static class HeaderEncodeEndingInterceptor extends
    PhaseInterceptorSupport<Message> {

        public HeaderEncodeEndingInterceptor()
        {
            super(Phase.PRE_PROTOCOL_ENDING);
        }

      
        @Override
        public void handleMessage(Message message) throws Fault {
        }
        
    }

}
