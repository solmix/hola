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

import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.Protocol;
import org.solmix.runtime.exchange.model.ProtocolInfo;
import org.solmix.runtime.exchange.support.DefaultMessage;
import org.solmix.runtime.interceptor.support.InterceptorProviderSupport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年12月10日
 */

public class HolaProtocol extends InterceptorProviderSupport implements
    Protocol {

    private static final long serialVersionUID = 4846308399740969939L;

    private final ProtocolInfo protocolInfo;

    public HolaProtocol(ProtocolInfo info) {
        this.protocolInfo = info;
    }

    @Override
    public Message createMessage() {
        return createMessage(new DefaultMessage());
    }

    @Override
    public Message createMessage(Message m) {
        //wrapped message.
        return m;
    }

    @Override
    public ProtocolInfo getBindingInfo() {
        return protocolInfo;
    }

}
