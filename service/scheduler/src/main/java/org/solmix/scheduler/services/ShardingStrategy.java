package org.solmix.scheduler.services;

import java.util.List;
import java.util.Map;

public interface ShardingStrategy
{
    public class Option{
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

        public Option(String jobName,int shardingTotalCount,Map<Integer, String> shardingItemParameters){
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
    /**
     * 进行作业分片.
     * 
     * @param serversList 所有参与分片的服务器列表
     * @param option 作业分片策略选项
     * @return 分配分片的服务器IP和分片集合的映射
     */
    Map<String, List<Integer>> sharding(List<String> serversList, Option option);
}
