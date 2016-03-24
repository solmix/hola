package org.solmix.scheduler.event;

import org.solmix.scheduler.job.JobExecutionShardingContext;

public interface JobListener
{
    void handleEvent(JobEvent event);

    void beforeJobExecuted(JobExecutionShardingContext shardingContext);

    void afterJobExecuted(JobExecutionShardingContext shardingContext);
}
