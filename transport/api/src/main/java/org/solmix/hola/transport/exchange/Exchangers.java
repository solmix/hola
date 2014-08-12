/*
 * Copyright 2013 The Solmix Project
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
package org.solmix.hola.transport.exchange;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.protocol.ProtocolExchanger;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月14日
 */

public class Exchangers
{

    public static ExchangeServer newServer(ExchangeHandler handler,EndpointInfo parameter) throws TransportException{
        Assert.isNotNull(handler);
        Assert.isNotNull(parameter);
        //暂时完全不管扩展,扩展考虑使用SytemContext.getExtensionForClass(Exchanger.class).get
        return new ProtocolExchanger().newServer(handler, parameter);
    }
    
    public static ExchangeClient newClient(ExchangeHandler handler,EndpointInfo parameter) throws TransportException{
        Assert.isNotNull(handler);
        Assert.isNotNull(parameter);
        return new ProtocolExchanger().newClient(handler, parameter);
    }
}
