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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Protocol;
import org.solmix.exchange.Service;
import org.solmix.exchange.data.SerializationManager;
import org.solmix.exchange.model.NamedID;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.model.SerializationInfo;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.ConfigSupportedReference;
import org.solmix.hola.rs.ProtocolFactorySupport;
import org.solmix.hola.rs.generic.HolaRemoteServiceFactory;
import org.solmix.hola.rs.generic.interceptor.HolaOutInterceptor;
import org.solmix.hola.rs.interceptor.InBindingInterceptor;
import org.solmix.hola.rs.interceptor.SerializationOutInterceptor;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月18日
 */
@Extension(name=ProtocolFactoryImpl.PROTOCOL_ID)
public class ProtocolFactoryImpl extends ProtocolFactorySupport implements ConfigSupportedReference
{
    public static final String PROTOCOL_ID =HolaRemoteServiceFactory.PROVIDER_ID;
    public static final Logger LOG = LoggerFactory.getLogger(ProtocolFactoryImpl.class);
    @Override
    public Protocol createProtocol(ProtocolInfo info) {
        HolaProtocol protocol= new HolaProtocol(info,container);
//        protocol.getOutInterceptors().add(new HeaderEncodeInterceptor());
       
        protocol.getOutInterceptors().add(new HolaOutInterceptor());
        
        SerializationOutInterceptor soi= new SerializationOutInterceptor(info.getExtension(SerializationInfo.class));
        SerializationManager sm= container.getExtension(SerializationManager.class);
        soi.setSerializationManager(sm);
        protocol.setSerializationManager(sm);
        protocol.getOutInterceptors().add(soi);
        protocol.getInInterceptors().add(new InBindingInterceptor());
        return protocol;
    }
    
    @Override
    public ProtocolInfo createProtocolInfo(Service service, String protocol, Dictionary<String, ?> configObject) {
        ProtocolInfo ptlInfo = new ProtocolInfo(service.getServiceInfo(), PROTOCOL_ID);
        ptlInfo.setName(new NamedID(ProtocolInfo.PROTOCOL_NS, PROTOCOL_ID));
        makeConfigAsEndpointInfoExtension(this, ptlInfo, configObject);
        SerializationInfo si = ptlInfo.getExtension(SerializationInfo.class);
        if (si != null && si.getSerialization() == null) {
            si.setSerialization(HOLA.DEFAULT_SERIALIZATION);
        }
        
        return ptlInfo;
    }

    @Override
    public Logger getLogger() {
        return LOG;
    }

    @Override
    public String[] getSupportedIntents(Dictionary<String, ?> info) {
        return null;
    }

    @Override
    public String[] getSupportedConfigs(Dictionary<String, ?> info) {
        return new String[]{HOLA.SERIALIZATION_KEY,HOLA.PALYLOAD_KEY};
    }

    @Override
    public Class<?> getSupportedConfigClass() {
        return SerializationInfo.class;
    }
}
