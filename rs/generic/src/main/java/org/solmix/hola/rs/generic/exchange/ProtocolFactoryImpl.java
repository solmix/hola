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

import java.util.Dictionary;

import org.solmix.exchange.Protocol;
import org.solmix.exchange.Service;
import org.solmix.exchange.model.NamedID;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.support.AbstractProtocolFactory;
import org.solmix.hola.rs.generic.HolaRemoteServiceFactory;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月18日
 */
@Extension(name=ProtocolFactoryImpl.PROTOCOL_ID)
public class ProtocolFactoryImpl extends AbstractProtocolFactory
{
    public static final String PROTOCOL_ID =HolaRemoteServiceFactory.PROVIDER_ID;

    @Override
    public Protocol createProtocol(ProtocolInfo info) {
        return null;
    }
    
    @Override
    public ProtocolInfo createProtocolInfo(Service service, String protocol, Dictionary<String, ?> configObject) {
        ProtocolInfo ptlInfo = new ProtocolInfo(service.getServiceInfo(), PROTOCOL_ID);
        ptlInfo.setName(new NamedID(ProtocolInfo.PROTOCOL_NS, PROTOCOL_ID));
        return ptlInfo;
    }
}
