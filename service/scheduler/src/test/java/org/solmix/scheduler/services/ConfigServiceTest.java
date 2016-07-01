package org.solmix.scheduler.services;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.solmix.scheduler.DataFlowJob;
import org.solmix.scheduler.exception.JobConflictException;
import org.solmix.scheduler.job.ScriptJob;
import org.solmix.scheduler.jobs.FooSimpleJob;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.model.JobInfoFactory;
import org.solmix.scheduler.model.support.FlowJobInfo;
import org.solmix.scheduler.model.support.ScriptJobInfo;
import org.solmix.scheduler.support.DefaultConfigService;
import org.unitils.util.ReflectionUtils;
public class ConfigServiceTest extends Assert {

	@Mock
    private StorageService storage;
	
	private JobInfo jobInfo = JobInfoFactory.createSimpleJobConfigurationBuilder("foo", FooSimpleJob.class, 3, "0/1 * * * * ?").build();
	private ConfigService configService = new DefaultConfigService(null, jobInfo);
	
	
	 @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(configService, "storage", storage);
    }
	 
	 @Test
    public void testPersistNewJobInfo() {
        when(storage.getJobInfo()).thenReturn(jobInfo);
        configService.persistJobInfo();
        verifyPersistJobConfiguration();
    }
	
	@Test(expected = JobConflictException.class)
    public void testJobConflict() {
        when(storage.isJobNodeExisted(ConfigNode.JOB_CLASS)).thenReturn(true);
        when(storage.getJobNodeData(ConfigNode.JOB_CLASS)).thenReturn("ConflictJob");
        when(storage.getJobInfo()).thenReturn(jobInfo);
        try {
            configService.persistJobInfo();
        } finally {
            verify(storage).isJobNodeExisted(ConfigNode.JOB_CLASS);
            verify(storage).getJobNodeData(ConfigNode.JOB_CLASS);
            verify(storage, times(2)).getJobInfo();
        }
    }
	
	
	private void verifyPersistJobConfiguration() {
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.JOB_CLASS, FooSimpleJob.class.getCanonicalName());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.SHARDING_TOTAL_COUNT, jobInfo.getShardingTotalCount());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.SHARDING_ITEM_PARAMETERS, jobInfo.getShardingItemParameters());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.JOB_PARAMETER, jobInfo.getJobParameter());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.CRON, jobInfo.getCron());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.MONITOR_EXECUTION, jobInfo.isMonitorExecution());
        if (DataFlowJob.class.isAssignableFrom(jobInfo.getJobClass())) {
            FlowJobInfo dataFlowJobConfiguration = (FlowJobInfo) jobInfo;
            verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.PROCESS_COUNT_INTERVAL_SECONDS, dataFlowJobConfiguration.getProcessCountIntervalSeconds());
            verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT, dataFlowJobConfiguration.getConcurrentDataProcessThreadCount());
            verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.FETCH_DATA_COUNT, dataFlowJobConfiguration.getFetchDataCount());
            verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.STREAMING_PROCESS, dataFlowJobConfiguration.isStreamingProcess());
        }
        if (ScriptJob.class.isAssignableFrom(jobInfo.getJobClass())) {
        	ScriptJobInfo scriptJobConfiguration = (ScriptJobInfo) jobInfo;
            verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.SCRIPT_COMMAND_LINE, scriptJobConfiguration.getScriptCommandLine());
        }
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.MAX_TIME_DIFF_SECONDS, jobInfo.getMaxTimeDiffSeconds());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.FAILOVER, jobInfo.isFailover());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.MISFIRE, jobInfo.isMisfire());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.JOB_SHARDING_STRATEGY_CLASS, jobInfo.getJobShardingStrategyClass());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.DESCRIPTION, jobInfo.getDescription());
        verify(storage).fillJobNodeIfNullOrOverwrite(ConfigNode.MONITOR_PORT, jobInfo.getMonitorPort());
    }
    
}
