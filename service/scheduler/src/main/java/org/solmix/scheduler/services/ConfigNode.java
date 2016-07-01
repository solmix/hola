package org.solmix.scheduler.services;


public final class ConfigNode
{
    public static final String ROOT = "config";
    
    public static final String JOB_CLASS = ROOT + "/jobClass";
    
    public static final String SHARDING_TOTAL_COUNT = ROOT + "/shardingTotalCount";
    
    public static final String CRON = ROOT + "/cron";
    
    public static final String SHARDING_ITEM_PARAMETERS = ROOT + "/shardingItemParameters";
    
    public static final String JOB_PARAMETER = ROOT + "/jobParameter";
    
    public static final String MONITOR_EXECUTION = ROOT + "/monitorExecution";
    
    public static final String PROCESS_COUNT_INTERVAL_SECONDS = ROOT + "/processCountIntervalSeconds";
    
    public static final String CONCURRENT_DATA_PROCESS_THREAD_COUNT = ROOT + "/concurrentDataProcessThreadCount";
    
    public static final String FETCH_DATA_COUNT = ROOT + "/fetchDataCount";
    
    public static final String MAX_TIME_DIFF_SECONDS = ROOT + "/maxTimeDiffSeconds";
    
    public static final String FAILOVER = ROOT + "/failover";
    
    public static final String MISFIRE = ROOT + "/misfire";
    
    public static final String JOB_SHARDING_STRATEGY_CLASS = ROOT + "/jobShardingStrategyClass";
    
    public static final String DESCRIPTION = ROOT + "/description";
    
    public static final String MONITOR_PORT = ROOT + "/monitorPort";

    public static final String SCRIPT_COMMAND_LINE = ROOT + "/scriptCommandLine";

	public static final String STREAMING_PROCESS = ROOT + "/streamingProcess";
    
    private final JobNodePath jobNodePath;
    
    public ConfigNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * 判断是否为作业分片总数路径.
     * 
     * @param path 节点路径
     * @return 是否为作业分片总数路径
     */
    public boolean isShardingTotalCountPath(final String path) {
        return jobNodePath.getFullPath(SHARDING_TOTAL_COUNT).equals(path);
    }
    
    /**
     * 判断是否为监控作业执行时状态路径.
     * 
     * @param path 节点路径
     * @return 是否为监控作业执行时状态路径
     */
    public boolean isMonitorExecutionPath(final String path) {
        return jobNodePath.getFullPath(MONITOR_EXECUTION).equals(path);
    }
    
    /**
     * 判断是否为失效转移设置路径.
     * 
     * @param path 节点路径
     * @return 是否为失效转移设置路径
     */
    public boolean isFailoverPath(final String path) {
        return jobNodePath.getFullPath(FAILOVER).equals(path);
    }
    
    /**
     * 判断是否为作业调度配置路径.
     * 
     * @param path 节点路径
     * @return 是否为作业调度配置路径
     */
    public boolean isCronPath(final String path) {
        return jobNodePath.getFullPath(CRON).equals(path);
    }

}
