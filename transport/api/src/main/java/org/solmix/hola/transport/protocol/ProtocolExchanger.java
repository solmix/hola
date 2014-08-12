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
package org.solmix.hola.transport.protocol;

import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.Transporters;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.Exchanger;
import org.solmix.hola.transport.handler.DecodeHandler;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月14日
 */

public class ProtocolExchanger implements Exchanger
{
    
    public static final String NAME="protocol";

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.exchange.Exchanger#newServer(org.solmix.hola.transport.exchange.ExchangeHandler, org.solmix.hola.core.model.EndpointInfo)
     */
    @Override
    public ExchangeServer newServer(ExchangeHandler handler,
        EndpointInfo parameter) throws TransportException {
      Server server =Transporters.newServer(parameter,new DecodeHandler(new ProtocolExchangeHandler(handler)));
        return new ProtocolExchangeServer(server);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.exchange.Exchanger#newClient(org.solmix.hola.transport.exchange.ExchangeHandler, org.solmix.hola.core.model.EndpointInfo)
     */
    @Override
    public ExchangeClient newClient(ExchangeHandler handler,
        EndpointInfo parameter) throws TransportException {
        Client client =Transporters.newClient(parameter,new DecodeHandler(new ProtocolExchangeHandler(handler)));
        return new ProtocolExchangeClient(client);
    }

}
