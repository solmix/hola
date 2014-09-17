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
package org.solmix.hola.rt.spring;

import org.solmix.hola.rt.spring.parser.ApplicationDefinitionParser;
import org.solmix.hola.rt.spring.parser.ClientDefinitionParser;
import org.solmix.hola.rt.spring.parser.DiscoveryDefinitionParser;
import org.solmix.hola.rt.spring.parser.ModuleDefinitionParser;
import org.solmix.hola.rt.spring.parser.MonitorDefinitionParser;
import org.solmix.hola.rt.spring.parser.ProtocolDefinitionParser;
import org.solmix.hola.rt.spring.parser.ReferenceDefinitionParser;
import org.solmix.hola.rt.spring.parser.ServerDefinitionParser;
import org.solmix.hola.rt.spring.parser.ServiceDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月6日
 */

public class NamespaceHandler extends NamespaceHandlerSupport
{

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    @Override
    public void init() {
       registerBeanDefinitionParser("application", new ApplicationDefinitionParser());
       registerBeanDefinitionParser("module", new ModuleDefinitionParser());
       registerBeanDefinitionParser("discovery", new DiscoveryDefinitionParser());
       registerBeanDefinitionParser("monitor", new MonitorDefinitionParser());
       registerBeanDefinitionParser("protocol", new ProtocolDefinitionParser());
       registerBeanDefinitionParser("server", new ServerDefinitionParser());
       registerBeanDefinitionParser("client", new ClientDefinitionParser());
       registerBeanDefinitionParser("service", new ServiceDefinitionParser());
       registerBeanDefinitionParser("reference", new ReferenceDefinitionParser());

    }

}