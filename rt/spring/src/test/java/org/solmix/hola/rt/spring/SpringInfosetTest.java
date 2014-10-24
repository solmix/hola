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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.hola.core.model.ApplicationInfo;
import org.solmix.hola.core.model.ModuleInfo;
import org.solmix.hola.core.model.MonitorInfo;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 集成spring测试.
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月6日
 */

public class SpringInfosetTest extends Assert
{
    private ClassPathXmlApplicationContext ctx ;

    
    @Before
    public void setup(){
        ctx =  new ClassPathXmlApplicationContext("/test/test-application.xml");
        ctx.start();
    }
    @After
    public void tearDown(){
        if(ctx!=null){
            ctx.stop();ctx.close();
        }
    }
    @Test
    public void testApplication() {
            ApplicationInfo app = ctx.getBean("test", ApplicationInfo.class);
            Assert.assertNotNull(app);
            ApplicationInfo app2 = ctx.getBean("test2", ApplicationInfo.class);
            Assert.assertNotNull(app2);
            assertEquals(app.getName(), "test");
            assertEquals(app.getVersion(), "1.0.2");
            assertEquals(app.getArchitecture(), "linux");
            assertEquals(app.getEnvironment(), "test");
            assertEquals(app.getOwner(), "solmix");
            assertEquals(app.getOrganization(), "solmix.org");
            assertEquals(app.isDefault(), Boolean.TRUE);
    }
  
//    @Test
    public void testModule()  {
        ModuleInfo m = ctx.getBean("module", ModuleInfo.class);
        assertNotNull(m);
        ModuleInfo m2 = ctx.getBean("module2", ModuleInfo.class);
        assertNotNull(m2);
        assertEquals( "module",m.getName());
        assertEquals( "module",m.getId());
        assertEquals( "1.0.2",m.getVersion());
        assertEquals("solmix",m.getOwner() );
        assertEquals("solmix.org",m.getOrganization() );
        assertEquals(Boolean.TRUE,m.isDefault() );
    }
    
//    @Test
    public void testMonitor()  {
        
        MonitorInfo m = ctx.getBean("monitor", MonitorInfo.class);
        assertNotNull(m);
        assertEquals( "monitor",m.getId());
        assertEquals( "hola://localhost:9812",m.getAddress());
        assertEquals( "hola",m.getUsername());
        assertEquals( "hola",m.getPassword());
        assertEquals( "default",m.getId());
        assertEquals( "1.0.0",m.getVersion());
        assertEquals(Boolean.TRUE,m.isDefault() );
        assertEquals( "value",m.getProperty("key"));
    }
}
