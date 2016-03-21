package org.solmix.scheduler;

import java.util.List;
import java.util.Map;

import org.solmix.scheduler.model.ShardingStrategyInfo;

public interface ShardingStrategy
{

    /**
     * 进行作业分片.
     * 
     * @param serversList 所有参与分片的服务器列表
     * @param option 作业分片策略选项
     * @return 分配分片的服务器IP和分片集合的映射
     */
    Map<String, List<Integer>> sharding(List<String> serversList, ShardingStrategyInfo option);
}
