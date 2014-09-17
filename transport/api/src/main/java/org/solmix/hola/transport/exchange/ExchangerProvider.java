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

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.protocol.ProtocolExchanger;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年7月14日
 */
@Extension(name=ProtocolExchanger.NAME)
public interface ExchangerProvider
{
    ExchangeServer bind(RemoteInfo info,ExchangeHandler handler)throws TransportException;
    ExchangeServer bind(RemoteInfo info,ChannelHandler handler,Replier<?> replier)throws TransportException;
    
    ExchangeClient connect(RemoteInfo info,ExchangeHandler handler)throws TransportException;
    ExchangeClient connect(RemoteInfo info,ChannelHandler handler,Replier<?> replier)throws TransportException;
}