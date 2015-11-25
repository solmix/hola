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

package org.solmix.hola.builder.blueprint;

import java.net.URL;
import java.util.Set;

import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.ParserContext;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年11月3日
 */

public class HolaNamespaceHandler implements NamespaceHandler
{

    @Override
    public ComponentMetadata decorate(Node arg0, ComponentMetadata arg1, ParserContext arg2) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Set<Class> getManagedClasses() {
        return null;
    }

    @Override
    public URL getSchemaLocation(String arg0) {
        if ("http://www.solmix.org/schema/hola".equals(arg0)) {
            return getClass().getClassLoader().getResource("schema/blueprint/hola-1.0.xsd");
        }
        return null;
    }

    @Override
    public Metadata parse(Element element, ParserContext context) {
        String s = element.getLocalName();
        if("application".equals(s)){
            return new ApplicationDefinitionParser().parse(element, context);
        }else if("module".equals(s)){
            return new ModuleDefinitionParser().parse(element, context);
        }else if("discovery".equals(s)){
            return new DiscoveryDefinitionParser().parse(element, context);
        }else if("monitor".equals(s)){
            return new MonitorDefinitionParser().parse(element, context);
        }else if("consumer".equals(s)){
            return new ConsumerDefinitionParser().parse(element, context);
        }else if("provider".equals(s)){
            return new ProviderDefinitionParser().parse(element, context);
        }else if("reference".equals(s)){
            return new ReferenceDefinitionParser().parse(element, context);
        }else if("service".equals(s)){
            return new ServiceDefinitionParser().parse(element, context);
        }
        return null;
    }

}
