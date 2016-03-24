package org.solmix.scheduler.support;

import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.MonitorService;


public class DefaultMonitorService implements MonitorService
{
    private final String jobName;
    public DefaultMonitorService(final SchedulerRegistry registry, JobInfo info)
    {
        jobName = info.getJobName();
    }
    @Override
    public void start() {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }

}
