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

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.rt.ServiceExportor;
import org.solmix.hola.rt.config.ApplicationConfig;
import org.solmix.hola.rt.config.ModuleConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月6日
 */

public class NamespaceHandlerTest
{

    @Test
    public void test() throws InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/test/test-application.xml");
        ctx.start();
        try {
            ApplicationConfig app= ctx.getBean("test", ApplicationConfig.class);
          Assert.assertNotNull(app);
          ApplicationConfig app2= ctx.getBean("test2", ApplicationConfig.class);
          Assert.assertNotNull(app2);
          Assert.assertEquals("1.0.2", app2.getVersion());
          ModuleConfig module = ctx.getBean("module", ModuleConfig.class);
          Assert.assertEquals(module.getVersion(), "1.0.2");
          ServiceExportor exportor= ctx.getBean("service", ServiceExportor.class);
          Assert.assertNotNull(exportor);
        } finally {
            ctx.stop();
            ctx.close();
        }
    }

}