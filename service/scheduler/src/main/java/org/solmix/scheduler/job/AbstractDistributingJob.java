
package org.solmix.scheduler.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.scheduler.DistributingJob;
import org.solmix.scheduler.services.ServicesManager;

public abstract class AbstractDistributingJob implements DistributingJob
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDistributingJob.class);

    private ServicesManager manager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.trace("Execute job with context {}",context);
        manager.getConfigService().checkMaxTimeDiffSecondsTolerable();
        JobExecutionShardingContext shardingContext=  manager.getShardingContext();
        
        if(manager.misfireIfNecessary(shardingContext.getShardingItems())){
            LOG.debug("job: previous job is still running, new job will start after previous job completed. Misfired job had recorded.");
            return;
        }
        try {
            manager.beforeJobExecuted(shardingContext);
        } catch (final Throwable cause) {
            handleJobExecutionException(new JobExecutionException(cause));
        }
        executeJobInternal(shardingContext);
        LOG.trace("job: execute normal completed, sharding context:{}.", shardingContext);
        while (manager.isExecuteMisfired(shardingContext.getShardingItems())) {
            LOG.trace("job: execute misfired job, sharding context:{}.", shardingContext);
            manager.clearMisfire(shardingContext.getShardingItems());
            executeJobInternal(shardingContext);
            LOG.trace("job: misfired job completed, sharding context:{}.", shardingContext);
        }
        manager.failoverIfNecessary();
        try {
            manager.afterJobExecuted(shardingContext);
        } catch (final Throwable cause) {
            handleJobExecutionException(new JobExecutionException(cause));
        }
        LOG.trace("job: execute all completed, job execution context:{}.", context);
    }
    
    private void executeJobInternal(final JobExecutionShardingContext shardingContext) throws JobExecutionException {
        if (shardingContext.getShardingItems().isEmpty()) {
            LOG.trace("job: sharding item is empty, job execution context:{}.", shardingContext);
            return;
        }
        manager.registerJobBegin(shardingContext);
        try {
            executeJob(shardingContext);
        } catch (final Throwable cause) {
            handleJobExecutionException(new JobExecutionException(cause));
        } finally {
            manager.registerJobCompleted(shardingContext);
        }
    }
    
    protected abstract void executeJob(final JobExecutionShardingContext shardingContext);
    
    public void handleJobExecutionException(final JobExecutionException jobExecutionException) throws JobExecutionException {
        throw jobExecutionException;
    }
    
    public ServicesManager getServicesManager(){
        return manager;
    }
}
