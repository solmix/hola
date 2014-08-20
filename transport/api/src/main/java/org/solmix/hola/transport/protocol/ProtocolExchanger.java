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

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.TransporterProvider;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;
import org.solmix.hola.transport.exchange.AbstractExchanger;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.ExchangerProvider;
import org.solmix.hola.transport.handler.DecodeHandler;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月14日
 */
@Extension(name=ProtocolExchanger.NAME)
public class ProtocolExchanger extends AbstractExchanger implements ExchangerProvider
{
    
    public static final String NAME="protocol";

    private final Container container;
    
    public  ProtocolExchanger(Container container){
        this.container=container;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.exchange.Exchanger#bind(org.solmix.hola.transport.exchange.ExchangeHandler, org.solmix.hola.core.model.EndpointInfo)
     */
    @Override
    public ExchangeServer bind(RemoteInfo info,ExchangeHandler handler) throws TransportException {
        Server server=  getTransporterProvider(info).bind(info, new DecodeHandler(new ProtocolExchangeHandler(handler)));
        return new ProtocolExchangeServer(server);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.exchange.Exchanger#connect(org.solmix.hola.transport.exchange.ExchangeHandler, org.solmix.hola.core.model.EndpointInfo)
     */
    @Override
    public ExchangeClient connect(RemoteInfo info,ExchangeHandler handler) throws TransportException {
        Client client=  getTransporterProvider(info).connect( info,new DecodeHandler(new ProtocolExchangeHandler(handler)));
        return new ProtocolExchangeClient(client);
    }
    
    private TransporterProvider getTransporterProvider(RemoteInfo info){
        if(container==null)
            throw new IllegalArgumentException("container is null");
        String t=info.getTransport();
        TransporterProvider provider;
        if(t==null){
            provider= container.getExtensionLoader(TransporterProvider.class).getDefault();
        }else{
            provider= container.getExtensionLoader(TransporterProvider.class).getExtension(t);
        }
       return provider;
        
    }

}
