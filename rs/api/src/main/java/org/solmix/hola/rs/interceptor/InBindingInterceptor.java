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

import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.exchange.model.NamedID;
import org.solmix.exchange.model.OperationInfo;
import org.solmix.hola.common.serial.Serialization;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年10月5日
 */

public class InBindingInterceptor extends PhaseInterceptorSupport<Message>
{

    /**
     * @param phase
     */
    public InBindingInterceptor()
    {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Exchange exchange = message.getExchange();
        Endpoint ed = exchange.getEndpoint();
        NamedID opName = (NamedID) message.get(Message.OPERATION);
        OperationInfo oi = ed.getEndpointInfo().getService().getInterface().getOperation(opName);
        exchange.put(OperationInfo.class, oi);
        exchange.put(Serialization.class, message.get(Serialization.class));
    }

}
