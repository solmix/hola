package org.solmix.scheduler.model.support;

import org.solmix.scheduler.DataFlowJob;
import org.solmix.scheduler.job.JobType;
@SuppressWarnings("rawtypes")
public class FlowJobInfo<T extends DataFlowJob> extends AbstractJobInfo<T> {
	
	private final int processCountIntervalSeconds;
    
    private final int fetchDataCount;
    
    private final int concurrentDataProcessThreadCount;
    
    private final boolean streamingProcess;
	private FlowJobInfo(
			final String jobName,
			final Class<? extends T> jobClass, 
			final int shardingTotalCount,
			final String cron, 
			final String shardingItemParameters,
			final String jobParameter, 
			final boolean monitorExecution,
			final int maxTimeDiffSeconds, 
			final boolean isFailover,
			final boolean isMisfire, 
			final int monitorPort,
			final String jobShardingStrategyClass, 
			final String description,
			final boolean disabled, 
			final boolean overwrite,
			final int processCountIntervalSeconds, 
			final int fetchDataCount, 
			final int concurrentDataProcessThreadCount,
            final boolean streamingProcess) {
		
		super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron,
				shardingItemParameters, jobParameter, monitorExecution,
				maxTimeDiffSeconds, isFailover, isMisfire, monitorPort,
				jobShardingStrategyClass, description, disabled, overwrite);
			this.processCountIntervalSeconds = processCountIntervalSeconds;
	        this.fetchDataCount = fetchDataCount;
	        this.concurrentDataProcessThreadCount = concurrentDataProcessThreadCount;
	        this.streamingProcess = streamingProcess;
	}
	
	@Override
	public int getProcessCountIntervalSeconds() {
		return processCountIntervalSeconds;
	}

	@Override
	public int getFetchDataCount() {
		return fetchDataCount;
	}

	@Override
	public int getConcurrentDataProcessThreadCount() {
		return concurrentDataProcessThreadCount;
	}



	public boolean isStreamingProcess() {
		return streamingProcess;
	}

	public static class FlowJobInfoBuilder extends
			AbstractJobInfoBuilder<FlowJobInfo<DataFlowJob>,
			DataFlowJob, 
			FlowJobInfoBuilder> {
		 private int processCountIntervalSeconds = 300;
	        
	        private int fetchDataCount = 1;
	        
	        private int concurrentDataProcessThreadCount = Runtime.getRuntime().availableProcessors() * 2;
	        
	        private boolean streamingProcess;
		public FlowJobInfoBuilder(final String jobName,
				final Class<? extends DataFlowJob> jobClass,
				final int shardingTotalCount, 
				final String cron) {
			super(jobName, JobType.SIMPLE, jobClass, shardingTotalCount, cron);
		}
		 /**
         * 设置统计作业处理数据数量的间隔时间.
         *
         * <p>
         * 单位：秒, 不能小于1.
         * </p>
         *
         * @param processCountIntervalSeconds 统计作业处理数据数量的间隔时间
         *
         * @return 作业配置构建器
         */
        public final FlowJobInfoBuilder processCountIntervalSeconds(final int processCountIntervalSeconds) {
            this.processCountIntervalSeconds = processCountIntervalSeconds;
            return this;
        }
        
        /**
         * 设置每次抓取的数据量.
         *
         * <p>
         * 默认值: CPU核数 * 2. 不能小于1.
         * </p>
         *
         * @param fetchDataCount 每次抓取的数据量
         *
         * @return 作业配置构建器
         */
        public final FlowJobInfoBuilder fetchDataCount(final int fetchDataCount) {
            this.fetchDataCount = fetchDataCount;
            return this;
        }
        
        /**
         * 设置同时处理数据的并发线程数.
         *
         * <p>
         * 不能小于1.
         * </p>
         *
         * @param concurrentDataProcessThreadCount 同时处理数据的并发线程数
         *
         * @return 作业配置构建器
         */
        public final FlowJobInfoBuilder concurrentDataProcessThreadCount(final int concurrentDataProcessThreadCount) {
            this.concurrentDataProcessThreadCount = concurrentDataProcessThreadCount;
            return this;
        }
        
        /**
         * 设置是否流式处理数据.
         * 
         * <p>
         * 如果流式处理数据, 则fetchData不返回空结果将持续执行作业. 如果非流式处理数据, 则处理数据完成后作业结束.
         * </p>
         *
         * @param streamingProcess 是否流式处理数据
         * 
         * @return 作业配置构建器
         */
        public final FlowJobInfoBuilder streamingProcess(final boolean streamingProcess) {
            this.streamingProcess = streamingProcess;
            return this;
        }
        
		@Override
		protected FlowJobInfo<DataFlowJob> buildInternal() {
			return new FlowJobInfo<DataFlowJob>(
					getJobName(), 
					getJobClass(),
					getShardingTotalCount(), 
					getCron(),
					getShardingItemParameters(), 
					getJobParameter(),
					isMonitorExecution(), 
					getMaxTimeDiffSeconds(),
					isFailover(), 
					isMisfire(), 
					getMonitorPort(),
					getJobShardingStrategyClass(), 
					getDescription(),
					isDisabled(), 
					isOverwrite(), 
					processCountIntervalSeconds, 
					fetchDataCount, 
					concurrentDataProcessThreadCount, 
					streamingProcess);
		}
	}
}
