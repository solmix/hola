package org.solmix.scheduler.support;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
		ZkRegistryWithAuthTest.class, 
		ZkRegistryWithLocalTest.class,
		ZkServersTest.class ,
		ZkRegistryTest.class
		})
public class AllTests {

}
