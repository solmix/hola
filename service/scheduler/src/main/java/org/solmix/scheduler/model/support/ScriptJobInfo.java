package org.solmix.scheduler.model.support;

import org.solmix.scheduler.job.JobType;
import org.solmix.scheduler.job.ScriptJob;

public class ScriptJobInfo extends AbstractJobInfo<ScriptJob> {
	
	private final String scriptCommandLine;
	private ScriptJobInfo(final String jobName,
			final Class<? extends ScriptJob> jobClass, final int shardingTotalCount,
			final String cron, final String shardingItemParameters,
			final String jobParameter, final boolean monitorExecution,
			final int maxTimeDiffSeconds, final boolean isFailover,
			final boolean isMisfire, final int monitorPort,
			final String jobShardingStrategyClass, final String description,
			final boolean disabled, final boolean overwrite
			, final String scriptCommandLine) {
		
		super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron,
				shardingItemParameters, jobParameter, monitorExecution,
				maxTimeDiffSeconds, isFailover, isMisfire, monitorPort,
				jobShardingStrategyClass, description, disabled, overwrite);
		this.scriptCommandLine = scriptCommandLine;
	}


	public static class ScriptJobInfoBuilder extends
			AbstractJobInfoBuilder<ScriptJobInfo,
			ScriptJob, 
			ScriptJobInfoBuilder> {
		private String scriptCommandLine;
		public ScriptJobInfoBuilder(final String jobName,
				final int shardingTotalCount, final String cron, final String scriptCommandLine) {
			super(jobName, JobType.SCRIPT, ScriptJob.class, shardingTotalCount, cron);
            this.scriptCommandLine = scriptCommandLine;
		}
		 public ScriptJobInfoBuilder scriptCommandLine(final String scriptCommandLine) {
	            this.scriptCommandLine = scriptCommandLine;
	            return this;
	        }

		@Override
		protected ScriptJobInfo buildInternal() {
			return new ScriptJobInfo(getJobName(), getJobClass(),
					getShardingTotalCount(), getCron(),
					getShardingItemParameters(), getJobParameter(),
					isMonitorExecution(), getMaxTimeDiffSeconds(),
					isFailover(), isMisfire(), getMonitorPort(),
					getJobShardingStrategyClass(), getDescription(),
					isDisabled(), isOverwrite(), scriptCommandLine);
		}
	}


	public String getScriptCommandLine() {
		return scriptCommandLine;
	}
}
