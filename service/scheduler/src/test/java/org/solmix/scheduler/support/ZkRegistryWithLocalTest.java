package org.solmix.scheduler.support;
import static org.hamcrest.core.Is.is;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.scheduler.exception.FileNotFoundException;
import org.solmix.scheduler.model.TestingZkInfo;

public class ZkRegistryWithLocalTest extends Assert {
	private TestingZkSchedulerRegistry registry;
	
	@Test
	public void testNotExitstedPath(){
		registry.init();
		assertFalse(registry.isExisted("/othered"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testWithLocalProperties(){
		registry.getZkInfo().setLocalPropertiesPath("/others/ss.properties");
		try {
			registry.init();
		} catch (Exception e) {
			throw (FileNotFoundException)e.getCause();
		}
	}
	
	@Test
	public void testWithOverrideProperties(){
		registry.getZkInfo().setLocalPropertiesPath("reg/path/local.properties");
		registry.getZkInfo().setOverwrite(true);
		registry.init();
		registry.getZkInfo().setLocalPropertiesPath("reg/path/overwrite.properties");
		//override
		registry.init();
		assertThat(registry.get("/test"), is("test_over"));
	}
	
	@Test
	public void testNotOverrideProperties(){
		int port = NetUtils.getAvailablePort();
		ZkServers.getInstance().startServerIfNotStarted(port, String.format("target/test_zk_data/%s/", System.nanoTime()));
		TestingZkInfo zi = new TestingZkInfo("localhost:"+port, TestingZkSchedulerRegistry.class.getName());
		TestingZkSchedulerRegistry registry = new TestingZkSchedulerRegistry(zi);
		registry.getZkInfo().setLocalPropertiesPath("reg/path/local.properties");
		registry.getZkInfo().setOverwrite(false);
		registry.init();
		registry.getZkInfo().setLocalPropertiesPath("reg/path/overwrite.properties");
		//override
		registry.init();
		assertThat(registry.get("/test"), is("test"));
	}
	
	
	@Before
	public void setup(){
		ZkServers.getInstance().startServerIfNotStarted(ZkServersTest.PORT, String.format("target/test_zk_data/%s/", System.nanoTime()));
		TestingZkInfo zi = new TestingZkInfo(ZkServersTest.TEST_ZK_ADDRESS, TestingZkSchedulerRegistry.class.getName());
		registry = new TestingZkSchedulerRegistry(zi);
	}
	
	
	@After
	public void tearDown(){
		if(registry!=null){
			registry.close();
		}
	}
}
