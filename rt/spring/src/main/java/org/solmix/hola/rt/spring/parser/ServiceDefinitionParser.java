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

import org.solmix.commons.util.DOMUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.core.model.ArgumentInfo;
import org.solmix.hola.core.model.MethodInfo;
import org.solmix.hola.core.model.ServiceInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.solmix.runtime.support.spring.ContainerPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月7日
 */

public class ServiceDefinitionParser extends AbstractBeanDefinitionParser
{

    /**
     * @param type
     */
    public ServiceDefinitionParser()
    {
        super();
        setBeanClass(SpringRemoteServiceInfo.class);
    }
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder bean) {
        super.doParse(element, ctx, bean);
        String publish=element.getAttribute("publish");
        //默认暴露
        if(publish==null||"true".equals(publish)){
            bean.setInitMethodName("publish");
        }
        bean.setDestroyMethodName("stop");
        //不能懒加载,spring初始化后加载
        bean.setLazyInit(false);
    }
    @Override
    protected void parseIdAttribute(BeanDefinitionBuilder bean, Element element,
        String name, String val, ParserContext ctx) {
        bean.addPropertyValue("id", val);
    }
    @Override
    protected boolean parseContainerAttribute(Element element, ParserContext ctx,
        BeanDefinitionBuilder bean, String val) {
        if (val != null && val.trim().length() > 0) {
            //属性中包含Container,并且Spring中包含以val为id的Container.
            bean.addConstructorArgReference(val);
            return true;
        }
        return false;
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
        }else if ("server".equals(property)){
            if( val.indexOf(",") != -1){
                parseMultiRef("servers", val, bean,ctx);
            }else{
                bean.addPropertyReference("server", val);
            } 
        }else if ("ref".equals(property)){
            if (!StringUtils.isEmpty(val)) {
                if(ctx.getRegistry().containsBeanDefinition(val)){
                    BeanDefinition refBean = ctx.getRegistry().getBeanDefinition(val);
                    if (! refBean.isSingleton()) {
                        throw new IllegalStateException("The exported service ref " + val +
                            " must be singleton! Please set the " + val + " bean scope to singleton, eg: "
                                + "<bean id=\"" + val+ "\" scope=\"singleton\" ...>");
                    }
                }
                bean.addConstructorArgReference(val);
            }
        }else{
            super.attributeToProperty(bean, property, val,ctx);
        }
    }
    @Override
    protected void parseElement(ParserContext ctx, BeanDefinitionBuilder bean,
        Element e, String name) {
        if("ref".equals(name)){
            ctx.getDelegate().parseConstructorArgElement(e, bean.getBeanDefinition());
        }else if ("methods".equals(name)) {
            parseMethods(e.getChildNodes(),bean,ctx);
        }else if ("properties".equals(name)) {
            Map<?, ?> map = ctx.getDelegate().parseMapElement(e, bean.getBeanDefinition());
            bean.addPropertyValue("properties", map);
        }
    }

    static void parseMethods( NodeList nodeList,
        BeanDefinitionBuilder bean, ParserContext parserContext) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedList<BeanDefinitionHolder> methods = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if ("method".equals(node.getNodeName())
                        || "method".equals(node.getLocalName())) {
                        String methodName = element.getAttribute("name");
                        if (methodName == null || methodName.length() == 0) {
                            throw new IllegalStateException(
                                "<hola:method> name attribute == null");
                        }
                        if (methods == null) {
                            methods = new ManagedList<BeanDefinitionHolder>();
                        }
                        BeanDefinition methodBeanDefinition = parse(((Element) node), parserContext,MethodInfo.class);
                        String name = bean.getBeanDefinition().getPropertyValues().get(ID_ATTRIBUTE) + "." + methodName;
                        BeanDefinitionHolder methodBeanDefinitionHolder = new BeanDefinitionHolder(
                            methodBeanDefinition, name);
                        methods.add(methodBeanDefinitionHolder);
                    }
                }
            }
            if (methods != null) {
                bean.addPropertyValue("methods", methods);
            }
        }
    }
    static void parseArguments( NodeList nodeList,
        RootBeanDefinition bean, ParserContext parserContext) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedList<BeanDefinitionHolder> arguments = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if ("argument".equals(node.getNodeName())
                        || "argument".equals(node.getLocalName())) {
                        String index = element.getAttribute("index");
                        if (arguments == null) {
                            arguments = new ManagedList<BeanDefinitionHolder>();
                        }
                        BeanDefinition methodBeanDefinition = parse(((Element) node), parserContext,ArgumentInfo.class);
                        String name = bean.getPropertyValues().get(ID_ATTRIBUTE) + "." + index;
                        BeanDefinitionHolder methodBeanDefinitionHolder = new BeanDefinitionHolder(
                            methodBeanDefinition, name);
                        arguments.add(methodBeanDefinitionHolder);
                    }
                }
            }
            if (arguments != null) {
                bean.getPropertyValues().addPropertyValue("arguments", arguments);
            }
        }
    }
   
     static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass) {
         RootBeanDefinition beanDefinition = new RootBeanDefinition();
         beanDefinition.setBeanClass(beanClass);
         beanDefinition.setLazyInit(false);
         NamedNodeMap atts=  element.getAttributes();
         for (int i = 0; i < atts.getLength(); i++) {
             Attr node = (Attr) atts.item(i);
             String val = node.getValue();
             String pre = node.getPrefix();
             String name = node.getLocalName();
             if (val != null && val.trim().length() > 0) {
                 beanDefinition.getPropertyValues().addPropertyValue(name, val);
             }
         }
         Element el = DOMUtils.getFirstElement(element);
         while (el != null) {
             String name = el.getLocalName();
             if("argument".equals(name)){
                 parseArguments(element.getChildNodes(),beanDefinition,parserContext);
             }
             el = DOMUtils.getNextElement(el);
         }
        return beanDefinition;
    }

    public static class SpringRemoteServiceInfo<T>  extends ServiceInfo<T> implements ApplicationContextAware{

        private static final long serialVersionUID = 2154745084368911732L;

        public SpringRemoteServiceInfo(T o) {
           
            super(o instanceof Container ? (Container)o : null,
                o instanceof Container ? null : o);
        }

        public SpringRemoteServiceInfo(Container c, T implementor) {
            super(c, implementor);
        }
        
        /**
         * 暴露服务端
         */
        public void publish() {
            
        }
        
        /**
         * 停止服务端
         */
        public void stop() {
            
        }
        
        @Override
        public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
            //如果没有,添加一个默认的
            if(getContainer()==null){
                setContainer(ContainerPostProcessor.addDefault(applicationContext));
            }
            
        }
        
    }
}
