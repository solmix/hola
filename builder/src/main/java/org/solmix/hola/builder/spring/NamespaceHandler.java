/**
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

package org.solmix.hola.builder.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年7月29日
 */

public class NamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("application", new ApplicationDefinitionParser());
        registerBeanDefinitionParser("module", new ModuleDefinitionParser());
        registerBeanDefinitionParser("discovery", new DiscoveryDefinitionParser());
        registerBeanDefinitionParser("monitor", new MonitorDefinitionParser());
        registerBeanDefinitionParser("consumer", new ConsumerDefinitionParser());
        registerBeanDefinitionParser("provider", new ProviderDefinitionParser());
        registerBeanDefinitionParser("reference", new ReferenceDefinitionParser());
        registerBeanDefinitionParser("service", new ServiceDefinitionParser());
    }

}
