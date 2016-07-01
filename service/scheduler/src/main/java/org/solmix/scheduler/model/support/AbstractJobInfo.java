package org.solmix.scheduler.model.support;

import org.solmix.commons.util.StringUtils;
import org.solmix.scheduler.DistributingJob;
import org.solmix.scheduler.job.JobType;
import org.solmix.scheduler.model.JobInfo;

import com.google.common.base.Preconditions;

public class AbstractJobInfo<T extends DistributingJob> implements JobInfo<T>{
	  /**
     * 作业名称.
     */
    private  String jobName;

    private  JobType jobType;
    /**
     * 作业实现类名称.
     */
    private  Class<? extends T> jobClass;

    /**
     * 作业分片总数.
     */
    private  int shardingTotalCount;

    /**
     * 作业启动时间的cron表达式.
     */
    private  String cron;

    /**
     * 分片序列号和个性化参数对照表.
     * 
     * <p>
     * 分片序列号和参数用等号分隔, 多个键值对用逗号分隔. 类似map. 分片序列号从0开始, 不可大于或等于作业分片总数. 如: 0=a,1=b,2=c
     * </p>
     */
    private String shardingItemParameters = "";

    /**
     * 作业自定义参数.
     * 
     * <p>
     * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
     * </p>
     */
    private String jobParameter = "";

    /**
     * 监控作业执行时状态.
     * 
     * <p>
     * 每次作业执行时间和间隔时间均非常短的情况, 建议不监控作业运行时状态以提升效率, 因为是瞬时状态, 所以无必要监控. 请用户自行增加数据堆积监控. 并且不能保证数据重复选取, 应在作业中实现幂等性. 也无法实现作业失效转移.
     * 每次作业执行时间和间隔时间均较长短的情况, 建议监控作业运行时状态, 可保证数据不会重复选取.
     * </p>
     */
    private boolean monitorExecution = true;

    /**
     * 统计作业处理数据数量的间隔时间.
     * 
     * <p>
     * 单位: 秒. 只对处理数据流类型作业起作用.
     * </p>
     */
    private int processCountIntervalSeconds = 300;

    /**
     * 处理数据的并发线程数.
     * 
     * <p>
     * 只对高吞吐量处理数据流类型作业起作用.
     * </p>
     */
    private int concurrentDataProcessThreadCount = 1;

    /**
     * 每次抓取的数据量.
     * 
     * <p>
     * 可在不重启作业的情况下灵活配置抓取数据量.
     * </p>
     */
    private int fetchDataCount = 1;

    /**
     * 最大容忍的本机与注册中心的时间误差秒数.
     * 
     * <p>
     * 如果时间误差超过配置秒数则作业启动时将抛异常. 配置为-1表示不检查时间误差.
     * </p>
     */
    private int maxTimeDiffSeconds = -1;

    /**
     * 是否开启失效转移.
     * 
     * <p>
     * 只有对monitorExecution的情况下才可以开启失效转移.
     * </p>
     */
    private boolean failover;

    /**
     * 是否开启misfire.
     */
    private boolean misfire = true;

    /**
     * 作业辅助监控端口.
     */
    private int monitorPort = -1;

    /**
     * 作业分片策略实现类全路径.
     * 
     */
    private String jobShardingStrategyClass = "";

    /**
     * 作业描述信息.
     */
    private String description = "";

    /**
     * 作业是否禁止启动. 可用于部署作业时, 先禁止启动, 部署结束后统一启动.
     */
    private boolean disabled;

    /**
     * 本地配置是否可覆盖注册中心配置. 如果可覆盖, 每次启动作业都以本地配置为准.
     */
    private boolean overwrite;
    
	  AbstractJobInfo(final String jobName, 
			 				JobType jobType,
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
			 				final boolean overwrite) {
		 this.jobName=jobName;
		 this.jobClass=jobClass;
		 this.jobType=jobType;
		 this.shardingTotalCount=shardingTotalCount;
		 this.cron=cron;
		 this.shardingItemParameters=shardingItemParameters;
		 this.jobParameter=jobParameter;
		 this.monitorExecution=monitorExecution;
		 this.maxTimeDiffSeconds=maxTimeDiffSeconds;
		 this.failover=isFailover;
		 this.misfire=isMisfire;
		 this.monitorPort=monitorPort;
		 this.jobShardingStrategyClass=jobShardingStrategyClass;
		 this.description=description;
		 this.disabled=disabled;
		 this.overwrite=overwrite;
	 }
	 @Override
    public String getJobName() {
		return jobName;
	}

	@Override
	public JobType getJobType() {
		return jobType;
	}

	@Override
	public Class<? extends T> getJobClass() {
		return jobClass;
	}

	@Override
	public int getShardingTotalCount() {
		return shardingTotalCount;
	}

	@Override
	public String getCron() {
		return cron;
	}

	@Override
	public String getShardingItemParameters() {
		return shardingItemParameters;
	}

	@Override
	public String getJobParameter() {
		return jobParameter;
	}

	@Override
	public boolean isMonitorExecution() {
		return monitorExecution;
	}

	public int getProcessCountIntervalSeconds() {
		return processCountIntervalSeconds;
	}

	public int getConcurrentDataProcessThreadCount() {
		return concurrentDataProcessThreadCount;
	}

	public int getFetchDataCount() {
		return fetchDataCount;
	}

	@Override
	public int getMaxTimeDiffSeconds() {
		return maxTimeDiffSeconds;
	}

	@Override
	public boolean isFailover() {
		return failover;
	}

	@Override
	public boolean isMisfire() {
		return misfire;
	}

	@Override
	public int getMonitorPort() {
		return monitorPort;
	}

	@Override
	public String getJobShardingStrategyClass() {
		return jobShardingStrategyClass;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public boolean isOverwrite() {
		return overwrite;
	}

	@SuppressWarnings("rawtypes")
	public abstract static class AbstractJobInfoBuilder<T extends AbstractJobInfo, J extends DistributingJob, B extends AbstractJobInfoBuilder> {
        
        private  String jobName;
        
        private  JobType jobType;
        
        private  Class<? extends J> jobClass;
        
        private  int shardingTotalCount;
        
        private  String cron;
    
        private String shardingItemParameters = "";
    
        private String jobParameter = "";
    
        private boolean monitorExecution = true;
    
        private int maxTimeDiffSeconds = -1;
    
        private boolean failover;
    
        private boolean misfire = true;
    
        private int monitorPort = -1;
    
        private String jobShardingStrategyClass = "";
    
        private String description = "";
    
        private boolean disabled;
    
        private boolean overwrite;
        
        public AbstractJobInfoBuilder(
        		String jobName, 
        		JobType simple,
				Class<? extends J> jobClass,
				int shardingTotalCount, String cron) {
        	this.jobName=jobName;
   		 	this.jobClass=jobClass;
   		 	this.shardingTotalCount=shardingTotalCount;
   		 	this.cron=cron;
		}

		public String getJobName() {
			return jobName;
		}

		public JobType getJobType() {
			return jobType;
		}

		public Class<? extends J> getJobClass() {
			return jobClass;
		}

		public int getShardingTotalCount() {
			return shardingTotalCount;
		}

		public String getCron() {
			return cron;
		}

		public String getShardingItemParameters() {
			return shardingItemParameters;
		}

		public String getJobParameter() {
			return jobParameter;
		}

		public boolean isMonitorExecution() {
			return monitorExecution;
		}

		public int getMaxTimeDiffSeconds() {
			return maxTimeDiffSeconds;
		}

		public boolean isFailover() {
			return failover;
		}

		public boolean isMisfire() {
			return misfire;
		}

		public int getMonitorPort() {
			return monitorPort;
		}

		public String getJobShardingStrategyClass() {
			return jobShardingStrategyClass;
		}

		public String getDescription() {
			return description;
		}

		public boolean isDisabled() {
			return disabled;
		}

		public boolean isOverwrite() {
			return overwrite;
		}

		/**
         * 设置分片序列号和个性化参数对照表.
         *
         * <p>
         * 分片序列号和参数用等号分隔, 多个键值对用逗号分隔. 类似map.
         * 分片序列号从0开始, 不可大于或等于作业分片总数.
         * 如:
         * 0=a,1=b,2=c
         * </p>
         *
         * @param shardingItemParameters 分片序列号和个性化参数对照表
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B shardingItemParameters(final String shardingItemParameters) {
            this.shardingItemParameters = shardingItemParameters;
            return (B) this;
        }
        
        /**
         * 设置作业自定义参数.
         *
         * <p>
         * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
         * </p>
         *
         * @param jobParameter 作业自定义参数
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B jobParameter(final String jobParameter) {
            this.jobParameter = jobParameter;
            return (B) this;
        }
        
        /**
         * 设置监控作业执行时状态.
         *
         * <p>
         * 每次作业执行时间和间隔时间均非常短的情况, 建议不监控作业运行时状态以提升效率, 因为是瞬时状态, 所以无必要监控. 请用户自行增加数据堆积监控. 并且不能保证数据重复选取, 应在作业中实现幂等性. 也无法实现作业失效转移.
         * 每次作业执行时间和间隔时间均较长短的情况, 建议监控作业运行时状态, 可保证数据不会重复选取.
         * </p>
         *
         * @param monitorExecution 监控作业执行时状态
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B monitorExecution(final boolean monitorExecution) {
            this.monitorExecution = monitorExecution;
            return (B) this;
        }
        
        /**
         * 设置最大容忍的本机与注册中心的时间误差秒数.
         *
         * <p>
         * 如果时间误差超过配置秒数则作业启动时将抛异常.
         * 配置为-1表示不检查时间误差.
         * </p>
         *
         * @param maxTimeDiffSeconds 最大容忍的本机与注册中心的时间误差秒数
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B maxTimeDiffSeconds(final int maxTimeDiffSeconds) {
            this.maxTimeDiffSeconds = maxTimeDiffSeconds;
            return (B) this;
        }
        
        /**
         * 设置是否开启失效转移.
         *
         * <p>
         * 只有对monitorExecution的情况下才可以开启失效转移.
         * </p> 
         *
         * @param failover 是否开启失效转移
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B failover(final boolean failover) {
            this.failover = failover;
            return (B) this;
        }
        
        /**
         * 设置是否开启misfire.
         * 
         * @param misfire 是否开启misfire
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B misfire(final boolean misfire) {
            this.misfire = misfire;
            return (B) this;
        }
        
        /**
         * 设置作业辅助监控端口.
         *
         * @param monitorPort 作业辅助监控端口
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B monitorPort(final int monitorPort) {
            this.monitorPort = monitorPort;
            return (B) this;
        }
        
        /**
         * 设置作业分片策略实现类全路径.
         *
         * <p>
         * 默认使用{@code com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy}.
         * </p>
         *
         * @param jobShardingStrategyClass 作业辅助监控端口
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B jobShardingStrategyClass(final String jobShardingStrategyClass) {
            this.jobShardingStrategyClass = jobShardingStrategyClass;
            return (B) this;
        }
        
        /**
         * 设置作业描述信息.
         *
         * @param description 作业描述信息
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B description(final String description) {
            this.description = description;
            return (B) this;
        }
        
        /**
         * 设置作业是否禁止启动.
         * 
         * <p>
         * 可用于部署作业时, 先禁止启动, 部署结束后统一启动.
         * </p>
         *
         * @param disabled 作业是否禁止启动
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B disabled(final boolean disabled) {
            this.disabled = disabled;
            return (B) this;
        }
        
        /**
         * 设置本地配置是否可覆盖注册中心配置.
         * 
         * <p>
         * 如果可覆盖, 每次启动作业都以本地配置为准.
         * </p>
         *
         * @param overwrite 本地配置是否可覆盖注册中心配置
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B overwrite(final boolean overwrite) {
            this.overwrite = overwrite;
            return (B) this;
        }
        
        /**
         * 构建作业配置对象.
         * 
         * @return 作业配置对象
         */
        public final T build() {
            Preconditions.checkArgument(!StringUtils.isEmpty(jobName), "jobName can not be empty.");
            Preconditions.checkArgument(DistributingJob.class.isAssignableFrom(jobClass), "job class should be an instance of ElasticJob.");
            Preconditions.checkArgument(shardingTotalCount > 0, "shardingTotalCount should larger than zero.");
            Preconditions.checkArgument(!StringUtils.isEmpty(cron), "cron can not be empty.");
            Preconditions.checkArgument(null != shardingItemParameters, "shardingItemParameters can not be null.");
            Preconditions.checkArgument(null != jobParameter, "jobParameter can not be null.");
            Preconditions.checkArgument(null != jobShardingStrategyClass, "jobShardingStrategyClass can not be null.");
            Preconditions.checkArgument(null != description, "description can not be null.");
            return buildInternal();
        }
        
        protected abstract T buildInternal();
    }

	
}
