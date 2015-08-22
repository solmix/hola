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
package org.solmix.hola.rpc.hola;

import org.solmix.hola.rpc.RpcPhasePolicy;
import org.solmix.runtime.Container;
import org.solmix.exchange.EndpointException;
import org.solmix.exchange.Service;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.DefaultEndpoint;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年11月19日
 */

public class HolaEndpoint extends DefaultEndpoint{

    private static final long serialVersionUID = 5255996701919292066L;
    public HolaEndpoint(Container container, Service s, EndpointInfo ed) throws EndpointException {
        super(container, s, ed, new RpcPhasePolicy());
        
    }

}
