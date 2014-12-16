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

import org.solmix.hola.common.config.ModuleConfig;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月6日
 */

public class ModuleDefinitionParser extends AbstractBeanDefinitionParser
{

    /**
     * @param type
     */
    public ModuleDefinitionParser()
    {
        super();
        setBeanClass(ModuleConfig.class);
    }
    @Override
    protected void parseNameAttribute(Element element, ParserContext ctx,
        BeanDefinitionBuilder bean, String val) {
       attributeToProperty(bean, "name", val, ctx);
    }
    @Override
    protected void attributeToProperty(BeanDefinitionBuilder bean,
        String property, String val,ParserContext ctx) {
        if("discovery".equals(property)){
            if( val.indexOf(",") != -1){
                parseMultiRef("discoveries", val, bean,ctx);
            }else{
                bean.addPropertyReference("discovery", val);
            }
        }else{
            super.attributeToProperty(bean, property, val,ctx);
        }
    }
   /* protected void parserValue(RootBeanDefinition beanDefinition,
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
    }*/
}
