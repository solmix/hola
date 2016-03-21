
package org.solmix.scheduler.support;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.SchedulerService;
import org.solmix.scheduler.event.JobListener;
import org.solmix.scheduler.model.JobInfo;

public class DefaultSchedulerService implements SchedulerService
{

    private static final Logger LOG  = LoggerFactory.getLogger(DefaultSchedulerService.class);
    
    private final String jobName;

    private final SchedulerRegistry registry;

    private final List<JobListener> listeners;

    private final JobDetail detail;

    public DefaultSchedulerService(final SchedulerRegistry registry, JobInfo info, final JobListener... jobListeners)
    {
        this.jobName = info.getJobName();
        this.registry = registry;
        this.listeners = Arrays.asList(jobListeners);
        detail = JobBuilder.newJob(info.getJobClass()).withIdentity(jobName).build();

    }

    @Override
    public void init() {
        LOG.debug("Init SchedulerService name :{}",jobName);
        

    }

    @Override
    public Date getNextFireTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void stopJob() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resumeJob() {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggerJob() {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void rescheduleJob(String cronExpression) {
        // TODO Auto-generated method stub

    }

}
