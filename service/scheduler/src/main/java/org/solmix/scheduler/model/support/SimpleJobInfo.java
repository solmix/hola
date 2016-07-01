package org.solmix.scheduler.model.support;

import org.solmix.scheduler.job.AbstractSimpleJob;
import org.solmix.scheduler.job.JobType;

public class SimpleJobInfo<T extends AbstractSimpleJob> extends AbstractJobInfo<T> {
	
	private SimpleJobInfo(final String jobName,
			final Class<? extends T> jobClass, final int shardingTotalCount,
			final String cron, final String shardingItemParameters,
			final String jobParameter, final boolean monitorExecution,
			final int maxTimeDiffSeconds, final boolean isFailover,
			final boolean isMisfire, final int monitorPort,
			final String jobShardingStrategyClass, final String description,
			final boolean disabled, final boolean overwrite) {
		
		super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron,
				shardingItemParameters, jobParameter, monitorExecution,
				maxTimeDiffSeconds, isFailover, isMisfire, monitorPort,
				jobShardingStrategyClass, description, disabled, overwrite);
	}


	public static class SimpleJobInfoBuilder extends
			AbstractJobInfoBuilder<SimpleJobInfo<AbstractSimpleJob>,
			AbstractSimpleJob, 
			SimpleJobInfoBuilder> {

		public SimpleJobInfoBuilder(final String jobName,
				final Class<? extends AbstractSimpleJob> jobClass,
				final int shardingTotalCount, 
				final String cron) {
			super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron);
		}

		@Override
		protected SimpleJobInfo<AbstractSimpleJob> buildInternal() {
			return new SimpleJobInfo<AbstractSimpleJob>(getJobName(), getJobClass(),
					getShardingTotalCount(), getCron(),
					getShardingItemParameters(), getJobParameter(),
					isMonitorExecution(), getMaxTimeDiffSeconds(),
					isFailover(), isMisfire(), getMonitorPort(),
					getJobShardingStrategyClass(), getDescription(),
					isDisabled(), isOverwrite());
		}
	}
}
