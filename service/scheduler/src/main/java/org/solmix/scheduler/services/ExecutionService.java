package org.solmix.scheduler.services;

import java.util.List;

import org.solmix.scheduler.job.JobExecutionShardingContext;

public interface ExecutionService
{

    boolean hasRunningItems();


    boolean misfireIfNecessary(List<Integer> shardingItems);

    void registerJobBegin(JobExecutionShardingContext shardingContext);

    void registerJobCompleted(JobExecutionShardingContext shardingContext);

    void clearMisfire(List<Integer> shardingItems);

    List<Integer> getMisfiredJobItems(List<Integer> shardingItems);


    void setMisfire(List<Integer> localHostShardingItems);


    JobExecutionShardingContext getJobExecutionShardingContext();

}
