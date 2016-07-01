package org.solmix.scheduler.support;

import org.solmix.scheduler.model.TestingZkInfo;

public class TestingZkSchedulerRegistry extends ZkSchedulerRegistry {

	private TestingZkInfo zkInfo;

	public TestingZkSchedulerRegistry(TestingZkInfo zkInfo) {
		super(zkInfo);
		this.zkInfo = zkInfo;
	}

	@Override
	protected void setupNestedIfNecessary() {
		if (zkInfo.isUseNestedZookeeper()) {
			ZkServers.getInstance().startServerIfNotStarted(
					zkInfo.getNestedPort(), zkInfo.getNestedDataDir());
		}
	}

	@Override
	protected void colseNestedIfNecessary() {
		if (zkInfo.isUseNestedZookeeper()) {
			ZkServers.getInstance().closeServer(zkInfo.getNestedPort());
		}
	}

	
}
