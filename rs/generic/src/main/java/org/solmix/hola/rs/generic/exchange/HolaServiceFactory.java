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

import org.solmix.exchange.model.NamedIDPolicy;
import org.solmix.exchange.model.ServiceInfo;
import org.solmix.exchange.support.DefaultService;
import org.solmix.exchange.support.ReflectServiceFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月17日
 */

public class HolaServiceFactory extends ReflectServiceFactory
{


    public HolaServiceFactory()
    {
        NamedIDPolicy policy = new NamedIDPolicy(this, ProtocolFactoryImpl.PROTOCOL_ID);
        setNamedIDPolicy(policy);
    }

    @Override
    protected void buildServiceModel() {
        super.buildServiceModel();

        ServiceInfo serviceInfo = new ServiceInfo();
        DefaultService service = new DefaultService(serviceInfo);
        setService(service);
    }
}
