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

import org.solmix.hola.rt.config.ServiceConfig;
import org.solmix.hola.rt.spring.SpringExportor;
import org.solmix.runtime.support.spring.AbstractRootBeanDefinitionParser;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Element;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月7日
 */

public class ServiceDefinitionParser extends AbstractRootBeanDefinitionParser implements
    BeanDefinitionParser
{

    /**
     * @param type
     */
    public ServiceDefinitionParser()
    {
        super(SpringRemoteServiceInfo.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinition bf= parse(element, parserContext, ServiceConfig.class);
       
        RootBeanDefinition exporter = new RootBeanDefinition();
        exporter.setBeanClass(SpringExportor.class);
        exporter.setLazyInit(false);
        exporter.getPropertyValues().add("config", bf);
        parserContext.getRegistry().registerBeanDefinition(bf.getPropertyValues().get("id").toString(), exporter);
        return exporter;
    }
    @Override
    protected void parserValue(RootBeanDefinition beanDefinition,
        String property, Class<?> propertyType, String value,
        ParserContext parserContext) {
        if (isPrimitive(propertyType)) {
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

    public static class SpringRemoteServiceInfo implements ApplicationContextAware{

        /**
         * {@inheritDoc}
         * 
         * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
         */
        @Override
        public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
            // TODO Auto-generated method stub
            
        }
        
    }
}
