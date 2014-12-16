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
import org.solmix.hola.common.config.ApplicationConfig;
import org.solmix.hola.common.config.ArgumentConfig;
import org.solmix.hola.common.config.ClientConfig;
import org.solmix.hola.common.config.DiscoveryConfig;
import org.solmix.hola.common.config.MethodConfig;
import org.solmix.hola.common.config.ModuleConfig;
import org.solmix.hola.common.config.MonitorConfig;
import org.solmix.hola.common.config.ReferenceConfig;
import org.solmix.hola.common.config.ServiceConfig;
import org.solmix.hola.common.config.ServerConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 集成spring测试.
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月6日
 */

public class SpringInfosetTest extends Assert {

    private ClassPathXmlApplicationContext ctx;

    @Before
    public void setup() {
        ctx = new ClassPathXmlApplicationContext("/test/test-application.xml");
        ctx.start();
    }

    @After
    public void tearDown() {
        if (ctx != null) {
            ctx.stop();
            ctx.close();
        }
    }

    @Test
    public void testApplication() {
        ApplicationConfig app = ctx.getBean("test", ApplicationConfig.class);
        Assert.assertNotNull(app);
        ApplicationConfig app2 = ctx.getBean("test2", ApplicationConfig.class);
        Assert.assertNotNull(app2);
        assertEquals(app.getName(), "test");
        assertEquals(app.getVersion(), "1.0.2");
        assertEquals(app.getArchitecture(), "linux");
        assertEquals(app.getEnvironment(), "test");
        assertEquals(app.getOwner(), "solmix");
        assertEquals(app.getOrganization(), "solmix.org");
        assertEquals(app.isDefault(), Boolean.TRUE);
        assertEquals(1, app.getDiscoveries().size());
    }

    @Test
    public void testModule() {
        ModuleConfig m = ctx.getBean("module", ModuleConfig.class);
        assertNotNull(m);
        ModuleConfig m2 = ctx.getBean("module2", ModuleConfig.class);
        assertNotNull(m2);
        assertNotNull(m2.getId(), "module2");
        assertEquals("module", m.getName());
        assertEquals("module", m.getId());
        assertEquals("1.0.2", m.getVersion());
        assertEquals("solmix", m.getOwner());
        assertEquals("solmix.org", m.getOrganization());
        assertEquals(Boolean.TRUE, m.isDefault());
        assertEquals(2, m.getDiscoveries().size());
    }

    @Test
    public void testMonitor() {

        MonitorConfig m = ctx.getBean("monitor", MonitorConfig.class);
        assertNotNull(m);
        assertEquals("monitor", m.getName());
        assertEquals("hola://localhost:9812", m.getAddress());
        assertEquals("hola", m.getUsername());
        assertEquals("hola", m.getPassword());
        assertEquals("monitor", m.getId());
        assertEquals("1.0.0", m.getVersion());
        assertEquals(Boolean.TRUE, m.isDefault());
        assertEquals("value", m.getProperty("key"));
    }

    @Test
    public void testDiscovery() {
        DiscoveryConfig m = ctx.getBean("discovery", DiscoveryConfig.class);
        assertNotNull(m);
        assertEquals("discovery", m.getName());
        assertEquals("zk://localhost:2340", m.getAddress());
        assertEquals("/home/solmix/a", m.getFile());
        assertEquals("hola", m.getUsername());
        assertEquals("hola", m.getPassword());
        assertEquals("discovery", m.getId());
        assertEquals("1.0.0", m.getVersion());
        assertEquals(Boolean.TRUE, m.isDefault());
        assertEquals(Boolean.TRUE, m.isDynamic());
        assertEquals(Boolean.TRUE, m.isSubscribe());
        assertEquals(Boolean.FALSE, m.isCheck());
        assertEquals(Integer.valueOf(5000), m.getTimeout());
        assertEquals(Integer.valueOf(2340), m.getPort());
        assertEquals("value2", m.getProperty("key"));
    }

    @Test
    public void testServer() {
        ServerConfig m = ctx.getBean("server", ServerConfig.class);
        assertNotNull(m);
        assertEquals(Integer.valueOf(5000), m.getTimeout());
        assertEquals(Integer.valueOf(3), m.getRetries());
        assertEquals(Integer.valueOf(200), m.getActives());
        assertEquals(Integer.valueOf(100), m.getConnections());
        assertEquals(Boolean.TRUE, m.isAsync());
        assertEquals(Boolean.FALSE, m.isAsyncwait());
        assertEquals(Boolean.TRUE, m.isDefault());
        assertEquals("jdk", m.getProxy());
        assertEquals("hola", m.getProtocol());
        assertEquals("failover", m.getCluster());
        assertEquals("local", m.getScope());
        assertEquals(1, m.getDiscoveries().size());
        assertNotNull(m.getApplication());
        assertNotNull(m.getModule());
        assertNotNull(m.getMonitor());
        assertEquals("1.0.9", m.getVersion());
        assertEquals("group1", m.getGroup());
        assertEquals(Integer.valueOf(9999), m.getWeight());
        assertEquals("http://localhost/docs/docserver.html", m.getDocument());
        assertEquals(Boolean.TRUE, m.isDynamic());
        assertEquals(Integer.valueOf(1000), m.getExecutes());
        assertEquals(Boolean.TRUE, m.isPublish());

        assertEquals("localhost", m.getHost());
        assertEquals(Integer.valueOf(1314), m.getPort());
        assertEquals("/slx", m.getContextpath());
        assertEquals("fixed", m.getExecutor());
        assertEquals(Integer.valueOf(188), m.getThreads());
        assertEquals(Integer.valueOf(20), m.getIothreads());
        assertEquals(Integer.valueOf(89), m.getQueues());
        assertEquals(Integer.valueOf(45), m.getAccepts());
        assertEquals("hola", m.getCodec());
        assertEquals("jdk", m.getSerial());
        assertEquals("utf-8", m.getCharset());
        assertEquals(Integer.valueOf(8096), m.getPayload());
        assertEquals(Integer.valueOf(6000), m.getHeartbeat());
        assertEquals("netty", m.getTransporter());
        assertEquals(Integer.valueOf(10000), m.getBuffer());
        assertEquals("generic", m.getExchanger());
        assertEquals("normal", m.getDispatcher());
        assertEquals(Boolean.TRUE, m.isAdvertise());
        assertEquals("default", m.getNetworker());
        assertEquals("value1", m.getProperty("key1"));

        ServiceConfig<?> inner = ctx.getBean("innerService",
            ServiceConfig.class);
        assertNotNull(inner);
        assertEquals(m.getDocument(), inner.getServer().getDocument());

    }

    @Test
    public void testService() {
        ServiceConfig<?> m = ctx.getBean("service", ServiceConfig.class);
        assertNotNull(m);
        assertEquals(Integer.valueOf(5000), m.getTimeout());
        assertEquals(Integer.valueOf(3), m.getRetries());
        assertEquals(Integer.valueOf(200), m.getActives());
        assertEquals(Integer.valueOf(100), m.getConnections());
        assertEquals(Boolean.TRUE, m.isAsync());
        assertEquals(Boolean.FALSE, m.isAsyncwait());
        assertEquals("jdk", m.getProxy());
        assertEquals("hola", m.getProtocol());
        assertEquals("failover", m.getCluster());
        assertEquals("local", m.getScope());
        assertEquals(1, m.getDiscoveries().size());
        assertEquals(1, m.getServers().size());
        assertNotNull(m.getApplication());
        assertNotNull(m.getModule());
        assertNotNull(m.getMonitor());
        assertEquals("1.0.9", m.getVersion());
        assertEquals("group1", m.getGroup());
        assertEquals(Integer.valueOf(9999), m.getWeight());
        assertEquals("http://localhost/docs/docserver.html", m.getDocument());
        assertEquals(Boolean.TRUE, m.isDynamic());
        assertEquals(Integer.valueOf(1000), m.getExecutes());
        assertEquals(Boolean.TRUE, m.isPublish());

        assertEquals(Integer.valueOf(3000), m.getDelay());
        assertEquals("/service", m.getPath());
        assertEquals(Boolean.FALSE, m.isGeneric());
        assertEquals("org.solmix.hola.rt.spring.HelloService", m.getInterface());
        assertNotNull(m.getRef());

        ServiceConfig<?> m2 = ctx.getBean("service2",
            ServiceConfig.class);
        assertNotNull(m2.getRef());
        assertEquals(1, m2.getMethods().size());
        MethodConfig mi = m2.getMethods().get(0);
        assertEquals("sayhello", mi.getName());

        assertEquals(1, mi.getArguments().size());
        ArgumentConfig arg = mi.getArguments().get(0);
        assertEquals(Integer.valueOf(0), arg.getIndex());
    }

    @Test
    public void testinit() {

    }

    @Test
    public void testClient() {
        ClientConfig m = ctx.getBean("client", ClientConfig.class);
        assertNotNull(m);
        assertEquals(Integer.valueOf(5000), m.getTimeout());
        assertEquals(Integer.valueOf(3), m.getRetries());
        assertEquals(Integer.valueOf(200), m.getActives());
        assertEquals(Integer.valueOf(100), m.getConnections());
        assertEquals(Boolean.TRUE, m.isAsync());
        assertEquals(Boolean.FALSE, m.isAsyncwait());
        assertEquals(Boolean.TRUE, m.isDefault());
        assertEquals("jdk", m.getProxy());
        assertEquals("hola", m.getProtocol());
        assertEquals("failover", m.getCluster());
        assertEquals("local", m.getScope());
        assertEquals(1, m.getDiscoveries().size());
        assertNotNull(m.getApplication());
        assertNotNull(m.getModule());
        assertNotNull(m.getMonitor());
        assertEquals("1.0.9", m.getVersion());
        assertEquals("group1", m.getGroup());
        assertEquals(Boolean.TRUE, m.isCheck());
        assertEquals(Boolean.TRUE, m.isGeneric());
        assertEquals("rec", m.getReconnect());
        assertEquals(Boolean.TRUE, m.isLazy());
        assertEquals(Boolean.TRUE, m.isDefault());
        assertEquals("value1", m.getProperty("key1"));
    }

    @Test
    public void testReference() {
        ReferenceConfig m = ctx.getBean("reference", ReferenceConfig.class);
        assertNotNull(m);
        assertEquals(Integer.valueOf(5000), m.getTimeout());
        assertEquals(Integer.valueOf(3), m.getRetries());
        assertEquals(Integer.valueOf(200), m.getActives());
        assertEquals(Integer.valueOf(100), m.getConnections());
        assertEquals(Boolean.TRUE, m.isAsync());
        assertEquals(Boolean.FALSE, m.isAsyncwait());
        assertEquals("jdk", m.getProxy());
        assertEquals("hola", m.getProtocol());
        assertEquals("failover", m.getCluster());
        assertEquals("local", m.getScope());
        assertEquals(1, m.getDiscoveries().size());
        assertNotNull(m.getApplication());
        assertNotNull(m.getModule());
        assertNotNull(m.getMonitor());
        assertEquals("1.0.9", m.getVersion());
        assertEquals("group1", m.getGroup());
        assertEquals(Boolean.TRUE, m.isCheck());
        assertEquals(Boolean.TRUE, m.isGeneric());
        assertEquals("rec", m.getReconnect());
        assertEquals(Boolean.TRUE, m.isLazy());
        assertEquals("hola://localhost:1314", m.getUrl());
        assertEquals("value1", m.getProperty("key1"));
        assertNotNull(m.getClient());
    }
}
