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

import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.ChannelHandler;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年8月17日
 */

public abstract class AbstractExchanger implements ExchangerProvider
{

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.exchange.ExchangerProvider#bind(org.solmix.hola.core.model.ChannelInfo,
     *      org.solmix.hola.transport.channel.ChannelHandler,
     *      org.solmix.hola.transport.exchange.Replier)
     */
    @Override
    public ExchangeServer bind(ChannelInfo info, ChannelHandler handler,
        Replier<?> replier) throws TransportException {
        return bind(info, new ExchangeHandlerDispatcher(replier, handler));
    }

    @Override
    public ExchangeClient connect(ChannelInfo info, ChannelHandler handler,
        Replier<?> replier) throws TransportException {
        return connect(info, new ExchangeHandlerDispatcher(replier, handler));
    }

}
