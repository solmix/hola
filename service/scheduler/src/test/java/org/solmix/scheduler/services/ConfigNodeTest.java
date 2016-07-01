package org.solmix.scheduler.services;

import org.junit.Assert;
import org.junit.Test;

public class ConfigNodeTest extends Assert {

	private ConfigNode cn = new ConfigNode("foo");

	@Test
	public void assertIsShardingTotalCountPath() {
		assertTrue(cn
				.isShardingTotalCountPath("/foo/config/shardingTotalCount"));
	}

	@Test
	public void assertIsNotShardingTotalCountPath() {
		assertFalse(cn
				.isShardingTotalCountPath("/foo/config/shardingTotalCount1"));
	}

	@Test
	public void assertIsMonitorExecutionPath() {
		assertTrue(cn.isMonitorExecutionPath("/foo/config/monitorExecution"));
	}

	@Test
	public void assertIsNotMonitorExecutionPath() {
		assertFalse(cn.isMonitorExecutionPath("/foo/config/monitorExecution1"));
	}

	@Test
	public void assertIsFailoverPath() {
		assertTrue(cn.isFailoverPath("/foo/config/failover"));
	}

	@Test
	public void assertIsNotFailoverPath() {
		assertFalse(cn.isFailoverPath("/foo/config/failover1"));
	}

	@Test
	public void assertIsCronPath() {
		assertTrue(cn.isCronPath("/foo/config/cron"));
	}

	@Test
	public void assertIsNotCronPath() {
		assertFalse(cn.isCronPath("/foo/config/cron1"));
	}
}
