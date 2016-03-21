package org.solmix.scheduler.model;

import java.util.Map;

public class ShardingStrategyInfo
{
    /**
     * 作业名称.
     */
    private final String jobName;
    
    /**
     * 作业分片总数.
     */
    private final int shardingTotalCount;
    
    /**
     * 分片序列号和个性化参数对照表.
     */
    private final Map<Integer, String> shardingItemParameters;

    public ShardingStrategyInfo(String jobName, int shardingTotalCount,Map<Integer, String> shardingItemParameters){
        this.jobName=jobName;
        this.shardingTotalCount=shardingTotalCount;
        this.shardingItemParameters=shardingItemParameters;
    }
    
    public String getJobName() {
        return jobName;
    }

    
    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    
    public Map<Integer, String> getShardingItemParameters() {
        return shardingItemParameters;
    }
    
}
