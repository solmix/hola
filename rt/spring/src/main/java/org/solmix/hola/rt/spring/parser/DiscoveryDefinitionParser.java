/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.hola.rt.spring.parser;

import java.util.Map;

import org.solmix.hola.common.config.DiscoveryConfig;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月6日
 */

public class DiscoveryDefinitionParser extends AbstractBeanDefinitionParser
{

    /**
     * @param type
     */
    public DiscoveryDefinitionParser()
    {
        super();
        setBeanClass(DiscoveryConfig.class);
    }
    @Override
    protected void parseNameAttribute(Element element, ParserContext ctx,
        BeanDefinitionBuilder bean, String val) {
       attributeToProperty(bean, "name", val, ctx);
    }
    @Override
    protected void parseElement(ParserContext ctx, BeanDefinitionBuilder bean,
        Element e, String name) {
        if ("properties".equals(name)) {
            Map<?, ?> map = ctx.getDelegate().parseMapElement(e, bean.getBeanDefinition());
            bean.addPropertyValue("properties", map);
        }
    }
}
