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

package org.solmix.hola.rpc.hola.protocol;

import org.solmix.hola.rpc.hola.interceptor.ObjectOutputInterceptor;
import org.solmix.hola.rpc.protocol.RpcProtocolFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.exchange.Protocol;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.model.ServiceInfo;
import org.solmix.runtime.interceptor.support.AttachmentInInterceptor;
import org.solmix.runtime.interceptor.support.AttachmentOutInterceptor;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年12月10日
 */
@Extension(name = HolaProtocolFactory.NAME)
public class HolaProtocolFactory extends RpcProtocolFactory {

    public static final String NAME = "hola";

    /**
     * @param container
     */
    public HolaProtocolFactory(Container container) {
        super(container);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.ProtocolFactory#createProtocol(org.solmix.exchange.model.ProtocolInfo)
     */
    @Override
    public Protocol createProtocol(ProtocolInfo info) {
        HolaProtocol hp = null;
        HolaProtocolInfo hpi = null;
        if (info instanceof HolaProtocolInfo) {
            hpi=(HolaProtocolInfo) info;
            hp = new HolaProtocol(hpi);
        } else {
            throw new IllegalStateException(
                "Can't create HolaProtocol,protocolInfo is not a HolaProtocolInfo");
        }

        hp.getOutInterceptors().add(new AttachmentOutInterceptor());
        hp.getOutInterceptors().add(new ObjectOutputInterceptor(hpi.getSerializationInfo(), getContainer()));

        hp.getOutFaultInterceptors().add(new ObjectOutputInterceptor(hpi.getSerializationInfo(), getContainer()));
        
        
        hp.getInInterceptors().add(new AttachmentInInterceptor());
        

        return hp;
    }

    @Override
    public ProtocolInfo createProtocolInfo(ServiceInfo info, String protocol,
        Object configObject) {
        HolaProtocolInfo pi = new HolaProtocolInfo(info, protocol);
        setProperties(pi,configObject);
        return pi;
    }
}
