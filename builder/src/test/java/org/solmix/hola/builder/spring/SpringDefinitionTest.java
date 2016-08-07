package org.solmix.hola.builder.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.solmix.hola.builder.ApplicationDefinition;
import org.solmix.hola.builder.ConsumerDefinition;
import org.solmix.hola.builder.DiscoveryDefinition;
import org.solmix.hola.builder.ModuleDefinition;
import org.solmix.hola.builder.MonitorDefinition;
import org.solmix.hola.builder.ProviderDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDefinitionTest {

	@Test
	public void test() {
		ClassPathXmlApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("test-ctx.xml");
			ApplicationDefinition def=context.getBean(ApplicationDefinition.class);
			assertNotNull(def);
			assertEquals("app", def.getId());
			assertEquals("appName", def.getName());
			assertEquals("0.0.1", def.getVersion());
			assertEquals("solmix", def.getOwner());
			assertEquals("solmix.org", def.getOrganization());
			assertEquals("test", def.getEnvironment());
			assertEquals(Boolean.TRUE, def.isDefault());
			
			ModuleDefinition module = context.getBean("module", ModuleDefinition.class);
			assertNotNull(module);
			assertEquals("module", module.getId());
			assertEquals("module1", module.getName());
			assertEquals("0.0.1", module.getVersion());
			assertEquals("solmix", module.getOwner());
			assertEquals("solmix.org", module.getOrganization());
			assertEquals(def, module.getApplication());
			
			DiscoveryDefinition dis=context.getBean("discovery",DiscoveryDefinition.class);
			assertNotNull(dis);
			assertEquals("discovery", dis.getId());
			assertEquals("discovery1", dis.getName());
			assertNotNull(dis.getProperties());
			assertEquals("dis-value", dis.getProperties().get("dis-key"));
			assertEquals("localhost:2181", dis.getAddress());
			assertEquals("user", dis.getUsername());
			assertEquals("psd", dis.getPassword());
			assertEquals("zk", dis.getProtocol());
			assertEquals(Integer.valueOf(2181), dis.getPort());
			assertEquals("failback", dis.getCluster());
			assertEquals("group1", dis.getGroup());
			assertEquals("0.22.1", dis.getVersion());
			assertEquals("aaa.dis", dis.getFile());
			assertEquals(Integer.valueOf(2322), dis.getTimeout());
			assertEquals(Boolean.TRUE, dis.isCheck());
			assertEquals(Boolean.TRUE, dis.isDynamic());
			assertEquals(Boolean.TRUE, dis.isPublish());
			assertEquals(Boolean.FALSE, dis.isSubscribe());
			assertEquals(Boolean.TRUE, dis.isDefault());
			
			MonitorDefinition monitor=context.getBean("monitor",MonitorDefinition.class);
			assertNotNull(monitor);
			assertEquals("monitor", monitor.getId());
			assertEquals("monitor1", monitor.getName());
			assertNotNull(monitor.getProperties());
			assertEquals("dis-value", monitor.getProperties().get("dis-key"));
			assertEquals("localhost:2181", monitor.getAddress());
			assertEquals("user", monitor.getUsername());
			assertEquals("psd", monitor.getPassword());
			assertEquals("zk", monitor.getProtocol());
			assertEquals("group1", monitor.getGroup());
			assertEquals("0.22.1", monitor.getVersion());
			assertEquals(Boolean.TRUE, monitor.isDefault());
			
			ConsumerDefinition consumer=context.getBean("consumer",ConsumerDefinition.class);
			assertNotNull(consumer);
			assertEquals("consumer", consumer.getId());
			assertNotNull(consumer.getProperties());
			assertEquals("dis-value", consumer.getProperties().get("dis-key"));
			assertEquals(Integer.valueOf(1111), consumer.getTimeout());
			assertEquals(Integer.valueOf(5), consumer.getRetries());
			assertEquals(Integer.valueOf(10),consumer.getActives());
			assertEquals("leastactive", consumer.getLoadbalance());
			assertEquals(Boolean.FALSE, consumer.isAsync());
			assertEquals(Boolean.TRUE, consumer.isAsyncwait());
			assertEquals("hola", consumer.getProtocol());
			assertEquals("jdk", consumer.getProxy());
			assertEquals("failback", consumer.getCluster());
			assertEquals("local", consumer.getScope());
			assertEquals(def, consumer.getApplication());
			assertEquals(module, consumer.getModule());
			assertEquals(monitor, consumer.getMonitor());
			assertEquals(dis, consumer.getDiscovery());
			assertEquals("0.6.1", consumer.getVersion());
			assertEquals("group2", consumer.getGroup());
			assertEquals(Boolean.FALSE, consumer.isCheck());
			assertEquals(Boolean.TRUE, consumer.isGeneric());
			assertEquals("12", consumer.getReconnect());
			assertEquals(Boolean.TRUE, consumer.isLazy());
			assertEquals(Integer.valueOf(1222), consumer.getConnectTimeout());
			assertEquals(Integer.valueOf(12), consumer.getPipelines());

			ReferenceFactoryBean<?> ref=context.getBean(ReferenceFactoryBean.class);
			assertNotNull(ref);
			assertEquals("hello", ref.getId());
			assertNotNull(ref.getProperties());
			assertEquals("dis-value", ref.getProperties().get("dis-key"));
			assertEquals(Integer.valueOf(1111), ref.getTimeout());
			assertEquals(Integer.valueOf(5), ref.getRetries());
			assertEquals(Integer.valueOf(10),ref.getActives());
			assertEquals("leastactive", ref.getLoadbalance());
			assertEquals(Boolean.FALSE, ref.isAsync());
			assertEquals(Boolean.TRUE, ref.isAsyncwait());
			assertEquals("hola", ref.getProtocol());
			assertEquals("jdk", ref.getProxy());
			assertEquals("failback", ref.getCluster());
			assertEquals("local", ref.getScope());
			assertEquals(def, ref.getApplication());
			assertEquals(module, ref.getModule());
			assertEquals(monitor, ref.getMonitor());
			assertEquals(dis, ref.getDiscovery());
			assertEquals("0.6.1", ref.getVersion());
			assertEquals("group2", ref.getGroup());
			assertEquals(Boolean.FALSE, ref.isCheck());
			assertEquals(Boolean.TRUE, ref.isGeneric());
			assertEquals("12", ref.getReconnect());
			assertEquals(Boolean.TRUE, ref.isLazy());
			assertEquals(Integer.valueOf(1222), ref.getConnectTimeout());
			assertEquals(Integer.valueOf(12), ref.getPipelines());
			

			ProviderDefinition provider=context.getBean("provider",ProviderDefinition.class);
			assertNotNull(provider);
			assertEquals("provider", provider.getId());
			assertNotNull(provider.getProperties());
			assertEquals("dis-value", provider.getProperties().get("dis-key"));
			assertEquals(Integer.valueOf(1111), provider.getTimeout());
			assertEquals(Integer.valueOf(5), provider.getRetries());
			assertEquals(Integer.valueOf(10),provider.getActives());
			assertEquals("leastactive", provider.getLoadbalance());
			assertEquals(Boolean.FALSE, provider.isAsync());
			assertEquals(Boolean.TRUE, provider.isAsyncwait());
			assertEquals("hola", provider.getProtocol());
			assertEquals("jdk", provider.getProxy());
			assertEquals("failback", provider.getCluster());
			assertEquals("local", provider.getScope());
			assertEquals(def, provider.getApplication());
			assertEquals(module, provider.getModule());
			assertEquals(monitor, provider.getMonitor());
			assertEquals(dis, provider.getDiscovery());
			assertEquals("0.6.1", provider.getVersion());
			assertEquals("group2", consumer.getGroup());
			assertEquals(Boolean.TRUE, consumer.isGeneric());

		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

}
