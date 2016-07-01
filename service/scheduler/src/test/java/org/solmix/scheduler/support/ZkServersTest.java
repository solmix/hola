package org.solmix.scheduler.support;

import java.util.Map;

import org.apache.curator.test.TestingServer;
import org.junit.Assert;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.unitils.util.ReflectionUtils;

public class ZkServersTest extends Assert {

	ZkServers servers = ZkServers.getInstance();
	public static final int PORT = NetUtils.getAvailablePort();
	public static final String TEST_DATA_DIR= String.format("target/zookeeper-test-dir/%s/", System.nanoTime());
	public static final String TEST_ZK_ADDRESS = "127.0.0.1:"+PORT;
	
	@Test
	public void testStartServerIfNotStarted() throws NoSuchFieldException {
		servers.startServerIfNotStarted(PORT, getDataDir());
		servers.startServerIfNotStarted(PORT, getDataDir());
		assertTrue(getNestedServers().containsKey(PORT));
	}

	@Test
	public void testCloseServer() throws NoSuchFieldException {
		servers.closeServer(PORT);
		assertFalse(getNestedServers().containsKey(PORT));
	}
	
	@Test
	public void closeServerAfterStarted() throws NoSuchFieldException {
		servers.startServerIfNotStarted(PORT, getDataDir());
		servers.closeServer(PORT);
		assertFalse(getNestedServers().containsKey(PORT));
	}
	
	private String getDataDir() {
		return String.format("target/test_zk_data/" + PORT + "/%s/",
				System.nanoTime());
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, TestingServer> getNestedServers()
			throws NoSuchFieldException {
		return (Map<Integer, TestingServer>) ReflectionUtils.getFieldValue(
				servers, ZkServers.class.getDeclaredField("nestedServers"));
	}
}
