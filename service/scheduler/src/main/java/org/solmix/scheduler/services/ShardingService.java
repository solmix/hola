package org.solmix.scheduler.services;

import java.util.List;

public interface ShardingService
{

    void setReshardingFlag();

    List<Integer> getLocalHostShardingItems();

    void shardingIfNecessary();

    boolean isNeedSharding();

}
