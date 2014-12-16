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

import org.solmix.hola.common.config.ServerConfig;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月7日
 */

public class ServerDefinitionParser extends AbstractBeanDefinitionParser
{

    /**
     * @param type
     */
    public ServerDefinitionParser()
    {
        super();
        setBeanClass(ServerConfig.class);
    }
    @Override
    protected void parseIdAttribute(BeanDefinitionBuilder bean, Element element,
        String name, String val, ParserContext ctx) {
        bean.addPropertyValue("id", val);
    }
    @Override
    protected void attributeToProperty(BeanDefinitionBuilder bean,
        String property, String val,ParserContext ctx) {
        if("application".equals(property)
            ||"module".equals(property)
            ||"monitor".equals(property)){
            bean.addPropertyReference(property, val);
        }else if ("discovery".equals(property)){
            if( val.indexOf(",") != -1){
                parseMultiRef("discoveries", val, bean,ctx);
            }else{
                bean.addPropertyReference("discovery", val);
            }
        }else{
            super.attributeToProperty(bean, property, val,ctx);
        }
    }
    @Override
    protected void parseElement(ParserContext ctx, BeanDefinitionBuilder bean,
        Element e, String name) {
        if("service".equals(name)){
            BeanDefinition service= ctx.getDelegate().parseCustomElement(e);
            if(service!=null){
                service.getPropertyValues().addPropertyValue("server", bean.getBeanDefinition());
            }
        }else if ("properties".equals(name)) {
            Map<?, ?> map = ctx.getDelegate().parseMapElement(e, bean.getBeanDefinition());
            bean.addPropertyValue("properties", map);
        }
    }
}
