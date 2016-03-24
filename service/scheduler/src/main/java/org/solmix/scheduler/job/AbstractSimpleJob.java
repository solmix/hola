package org.solmix.scheduler.job;


public abstract class AbstractSimpleJob extends AbstractDistributingJob
{

    @Override
    protected void executeJob(JobExecutionShardingContext shardingContext) {
            process(shardingContext);
    }
    
    public abstract void process(final JobExecutionShardingContext shardingContext);
}
