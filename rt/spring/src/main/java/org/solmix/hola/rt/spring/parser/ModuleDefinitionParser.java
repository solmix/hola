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

import org.solmix.hola.rt.config.ModuleType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年9月6日
 */

public class ModuleDefinitionParser extends AbstractDefinitionParser
{

    /**
     * @param type
     */
    public ModuleDefinitionParser()
    {
        super(ModuleType.class);
    }

    @Override
    protected void parserValue(RootBeanDefinition beanDefinition,
        String property, Class<?> propertyType, String value,
        ParserContext parserContext) {
        if (isPrimitive(propertyType))  {
            beanDefinition.getPropertyValues().addPropertyValue(property, value);
        } else if ("discovery".equals(property) && value.indexOf(",") != -1) {
            parseMultiRef("discoveries", value, beanDefinition, parserContext);
        } else {
            if ("ref".equals(property)
                && parserContext.getRegistry().containsBeanDefinition(value)) {
                BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(
                    value);
                if (!refBean.isSingleton()) {
                    throw new IllegalStateException("The exported service ref "
                        + value + " must be singleton! Please set the " + value
                        + " bean scope to singleton, eg: <bean id=\"" + value
                        + "\" scope=\"singleton\" ...>");
                }
            }
            beanDefinition.getPropertyValues().addPropertyValue(property,
                new RuntimeBeanReference(value));
        }
    }
}
