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
package org.solmix.hola.rt.spring;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.rt.GenericExportor;
import org.solmix.hola.rt.config.ServiceType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月10日
 */

public class SpringExportor extends GenericExportor implements InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, BeanNameAware
{
    private static final Logger LOG= LoggerFactory.getLogger(SpringExportor.class);
    private  String beanName;
    
    private ApplicationContext applicationContext;
    private boolean supportedApplicationListener;
    /**
     * @param type
     */
    public SpringExportor()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name) {
        beanName=name;
        
    }

   
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (ContextRefreshedEvent.class.getName().equals(event.getClass().getName())) {
            if (isDelay() && ! isExported() && ! isUnexported()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("The service ready on spring started. service: " + getConfig().getInterface());
                }
                export();
            }
        }
        
    }

    private boolean isDelay() {
        ServiceType<?> config = getConfig();
        Integer delay = config.getDelay();
        if (delay == null&&config.getServer()!=null) {
            delay = config.getServer().getDelay();
        }
        return supportedApplicationListener&&(delay != null && delay > 0) ? true : false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext=applicationContext;
     if(applicationContext!=null){
         try {
             Method method = applicationContext.getClass().getMethod("addApplicationListener", new Class<?>[]{ApplicationListener.class}); // 兼容Spring2.0.1
             method.invoke(applicationContext, new Object[] {this});
             supportedApplicationListener = true;
         } catch (Throwable t) {
           if (applicationContext instanceof AbstractApplicationContext) {
             try {
                 Method method = AbstractApplicationContext.class.getDeclaredMethod("addListener", new Class<?>[]{ApplicationListener.class}); // 兼容Spring2.0.1
                   if (! method.isAccessible()) {
                       method.setAccessible(true);
                   }
                 method.invoke(applicationContext, new Object[] {this});
                   supportedApplicationListener = true;
             } catch (Throwable t2) {
             }
             }
         }
     }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
      unexport();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO 设置默认的server,module,monitor,applicaiton,ptotocol,module.
        ServiceType<?> cf = getConfig();
        if (cf.getPath() == null || cf.getPath().length() == 0) {
            if (beanName != null && beanName.length() > 0
                && cf.getInterface() != null && cf.getInterface().length() > 0
                && beanName.startsWith(cf.getInterface())) {
                cf.setPath(beanName);
            }
        }
        if (! isDelay()) {
            export();
        }
    }

}
