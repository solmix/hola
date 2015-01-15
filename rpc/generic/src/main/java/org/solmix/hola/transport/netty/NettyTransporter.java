/**
 * Copyright (c) 2015 The Solmix Project
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
package org.solmix.hola.transport.netty;

import java.io.IOException;

import org.slf4j.Logger;
import org.solmix.runtime.Container;
import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.Pipeline;
import org.solmix.runtime.exchange.model.EndpointInfo;
import org.solmix.runtime.exchange.support.AbstractTransporter;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月15日
 */

public class NettyTransporter extends AbstractTransporter {

    /**
     * @param address
     * @param endpointInfo
     * @param container 
     */
    public NettyTransporter(String address, EndpointInfo endpointInfo,
        Container container) {
        super(address, endpointInfo, container);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.Transporter#shutdown()
     */
    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.Transporter#getBackPipeline(org.solmix.runtime.exchange.Message)
     */
    @Override
    public Pipeline getBackPipeline(Message msg) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.support.AbstractTransporter#getLogger()
     */
    @Override
    protected Logger getLogger() {
        // TODO Auto-generated method stub
        return null;
    }

}
