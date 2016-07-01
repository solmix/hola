package org.solmix.scheduler.support;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryOneTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.scheduler.model.TestingZkInfo;

public class ZkRegistryTest {
	private static TestingZkSchedulerRegistry registry;
	static TestingZkInfo zi = new TestingZkInfo(ZkServersTest.TEST_ZK_ADDRESS, ZkRegistryTest.class.getName());
	@BeforeClass
	public static void setup(){
		ZkServers.getInstance().startServerIfNotStarted(ZkServersTest.PORT, ZkServersTest.TEST_DATA_DIR);
		zi.setSessionTimeoutMilliseconds(6000);
		zi.setConnectionTimeoutMilliseconds(6000);
		zi.setLocalPropertiesPath("reg/path/local.properties");
		registry = new TestingZkSchedulerRegistry(zi);
		registry.init();
		registry.addCacheData("/test");
	}
	
	@AfterClass
	public  static void tearDown(){
		if(registry!=null){
			registry.close();
		}
	}
	
	@Test
    public void assertGetRawClient() {
        assertThat(registry.getRawClient(), instanceOf(CuratorFramework.class));
        assertThat(((CuratorFramework) registry.getRawClient()).getNamespace(), is(ZkRegistryTest.class.getName()));
    }
    
    @Test
    public void assertGetRawCache() {
        assertThat(registry.getRawCache("/test"), instanceOf(TreeCache.class));
    }
    
    @Test
    public void assertGetZkConfig() {
        assertThat((TestingZkInfo)registry.getZkInfo(), is(zi));
    }
    @Test
    public void testPersist(){
    	registry.persist("/foo", "foo-value");
    	registry.persist("/foo/sub-key", "foo-sub-value");
    	assertThat(registry.get("/foo"), is("foo-value"));
    	assertThat(registry.get("/foo/sub-key"), is("foo-sub-value"));
    }
    
    @Test
    public void testPersistEphemeral() throws Exception{
    	registry.persistEphemeral("/ephemeral", "ephemeral-value");
    	registry.persist("/foo", "foo-value");
    	assertThat(registry.get("/foo"), is("foo-value"));
    	assertThat(registry.get("/ephemeral"), is("ephemeral-value"));
    	registry.close();
    	CuratorFramework client = CuratorFrameworkFactory.newClient(ZkServersTest.TEST_ZK_ADDRESS, new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();

        assertThat(client.getData().forPath("/" + ZkRegistryTest.class.getName() + "/foo"), is("foo-value".getBytes()));
        assertNull(client.checkExists().forPath("/" + ZkRegistryTest.class.getName() + "/ephemeral"));
        registry.init();
    }
    
    @Test
    public void testPersistSequential() throws Exception {
    	
        assertTrue(registry.persistSequential("/sequential/test_sequential").startsWith("/sequential/test_sequential"));
        assertTrue(registry.persistSequential("/sequential/test_sequential").startsWith("/sequential/test_sequential"));
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZkServersTest.TEST_ZK_ADDRESS, new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        List<String> actual = client.getChildren().forPath("/" + ZkRegistryTest.class.getName() + "/sequential");
        assertThat(actual.size(), is(2));
        for (String each : actual) {
        	assertTrue(each. startsWith("test_sequential"));
        }
    }
    
    @Test
    public void testPersistEphemeralSequential() throws Exception {
        registry.persistEphemeralSequential("/esequential/ephemeral_sequential");
        registry.persistEphemeralSequential("/esequential/ephemeral_sequential");
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZkServersTest.TEST_ZK_ADDRESS, new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        List<String> actual = client.getChildren().forPath("/" + ZkRegistryTest.class.getName() + "/esequential");
        assertThat(actual.size(), is(2));
        for (String each : actual) {
        	assertTrue(each.startsWith("ephemeral_sequential"));
        }
        registry.close();
        actual = client.getChildren().forPath("/" + ZkRegistryTest.class.getName() + "/esequential");
        assertTrue(actual.isEmpty());
        registry.init();
    }
    
    @Test
    public void testUpdate() {
        registry.persist("/update", "before-update");
        registry.update("/update", "after-update");
        assertThat(registry.getDirectly("/update"), is("after-update"));
    }
    
    @Test
    public void testRemove() {
    	registry.remove("/test-remove");
        assertFalse(registry.isExisted("/test-remove"));
    }
   
    @Test
    public void testIsExisted() {
        assertTrue(registry.isExisted("/test"));
        assertTrue(registry.isExisted("/test/deep/path"));
        assertFalse(registry.isExisted("/test/not-exist"));
    }
    
    @Test
    public void testGetRegistryCenterTime() {
        assertTrue(registry.getRegistryCenterTime("/_systemTime/current") <= System.currentTimeMillis());
    }
    
    @Test
    public void testGetChildrenKeys() {
        assertThat(registry.getChildrenKeys("/test"), is(Arrays.asList("deep", "child")));
        assertThat(registry.getChildrenKeys("/test/deep"), is(Collections.singletonList("path")));
        assertThat(registry.getChildrenKeys("/test/child"), is(Collections.<String>emptyList()));
        assertThat(registry.getChildrenKeys("/test/notExisted"), is(Collections.<String>emptyList()));
    }
    
}
