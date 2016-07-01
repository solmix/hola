package org.solmix.scheduler.support;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.solmix.scheduler.model.TestingZkInfo;

public class ZkRegistryWithAuthTest {

	private TestingZkSchedulerRegistry registry;
	TestingZkInfo zi = new TestingZkInfo(ZkServersTest.TEST_ZK_ADDRESS, ZkRegistryWithAuthTest.class.getName());
	
	@Test(expected=NoAuthException.class)
	public void testDigestAuthFailed() throws Exception{
		registry.init();
		registry.close();
		CuratorFramework client = CuratorFrameworkFactory.builder()
		            .connectString(ZkServersTest.TEST_ZK_ADDRESS)
		            .retryPolicy(new RetryOneTime(2000))
		            .build();
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData()
        		.forPath("/" + ZkRegistryWithAuthTest.class.getName() + "/test/deep/path"), 
        		is("deepPath".getBytes()));
	}
	
	@Test
	public void testDigestAuthSuccess() throws Exception{
		registry.init();
		registry.close();
		CuratorFramework client = CuratorFrameworkFactory.builder()
		            .connectString(ZkServersTest.TEST_ZK_ADDRESS)
		            .retryPolicy(new RetryOneTime(2000))
		            .authorization(ZkSchedulerRegistry.ZK_ACL_SCHEME, "digest:password".getBytes()).build();
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData()
        		.forPath("/" + ZkRegistryWithAuthTest.class.getName() + "/test/deep/path"), 
        		is("deepPath".getBytes()));
	}
	
	@Before
	public void setup(){
		ZkServers.getInstance().startServerIfNotStarted(ZkServersTest.PORT, ZkServersTest.TEST_DATA_DIR);
		zi.setDigest("digest:password");
		zi.setLocalPropertiesPath("reg/path/local.properties");
		zi.setSessionTimeoutMilliseconds(6000);
		zi.setConnectionTimeoutMilliseconds(6000);
		registry = new TestingZkSchedulerRegistry(zi);
	}
	
	@After
	public void tearDown(){
		if(registry!=null){
			registry.close();
		}
	}
}
