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
package org.solmix.hola.rs;

import org.solmix.commons.util.StringUtils;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Message;
import org.solmix.exchange.PipelineSelector;
import org.solmix.exchange.model.OperationInfo;
import org.solmix.exchange.support.DefaultClient;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.transport.RemoteAddress;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年10月4日
 */

public class RemoteClient extends DefaultClient
{
    private static final long serialVersionUID = 7971996445962377766L;

    public RemoteClient(Container container, Endpoint endpoint, PipelineSelector pipelineSelector)
    {
        super(container, endpoint, pipelineSelector);
    }
    
    @Override
    protected void setupOutMessage(Message msg, Endpoint endpoint, OperationInfo oi) {
        RemoteAddress ra=  endpoint.getEndpointInfo().getExtension(RemoteAddress.class);
        msg.put(Message.PATH_INFO, ra.getPath());
        String version=ra.getAttributes().get(HOLA.VERSION_KEY);
        if(!StringUtils.isEmpty(version)){
            msg.put(HOLA.VERSION_KEY, version);
        }
    }

}
