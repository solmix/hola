package org.solmix.scheduler.model;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ZkInfoTest {

	@Test
	public void testConstruct() {
		ZkInfo zk  = new ZkInfo("127.0.0.1:2181", "schedulerTest");
		assertThat(zk.getAddress(),is("127.0.0.1:2181"));
		assertThat(zk.getNamespace(),is("schedulerTest"));
		assertThat(zk.getMaxRetries(),is(3));
		assertThat(zk.getBaseSleepTimeMilliseconds(),is(1000));
		assertThat(zk.getMaxSleepTimeMilliseconds(),is(3000));
	}
	@Test
	public void testConstruct2() {
		ZkInfo zk  = new ZkInfo("127.0.0.1:2181", "schedulerTest",5000,12000,5);
		assertThat(zk.getAddress(),is("127.0.0.1:2181"));
		assertThat(zk.getNamespace(),is("schedulerTest"));
		assertThat(zk.getMaxRetries(),is(5));
		assertThat(zk.getBaseSleepTimeMilliseconds(),is(5000));
		assertThat(zk.getMaxSleepTimeMilliseconds(),is(12000));
	}
	
	@Test
	public void testNestedConstruct2() {
		TestingZkInfo zk  = new TestingZkInfo("","schedulerTest");
		zk.setNestedPort(12311);
		zk.setNestedDataDir("target");
		assertTrue(zk.isUseNestedZookeeper());
	}

}
