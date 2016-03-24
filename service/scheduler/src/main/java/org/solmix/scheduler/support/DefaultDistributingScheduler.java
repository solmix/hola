
package org.solmix.scheduler.support;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.TriggerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.scheduler.JobRegistry;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.DistributingScheduler;
import org.solmix.scheduler.event.JobListener;
import org.solmix.scheduler.exception.JobException;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ServicesManager;

import com.google.common.base.Joiner;

public class DefaultDistributingScheduler implements DistributingScheduler
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDistributingScheduler.class);

    private static final String SCHEDULER_INSTANCE_NAME_SUFFIX = "Scheduler";

    private static final String CRON_TRIGGER_IDENTITY_SUFFIX = "Trigger";

    private final String jobName;

    private final SchedulerRegistry registry;

    private final List<JobListener> listeners;

    private final JobDetail jobDetail;

    private Scheduler scheduler;

   
    private final ServicesManager manager;

    public DefaultDistributingScheduler(final SchedulerRegistry registry, JobInfo info, final JobListener... jobListeners)
    {
        this.jobName = info.getJobName();
        this.registry = registry;
        this.listeners = Arrays.asList(jobListeners);
        jobDetail = JobBuilder.newJob(info.getJobClass()).withIdentity(jobName).build();
      
        manager = new ServicesManager(registry, info, listeners);
    }

    @Override
    public void init() {
        LOG.debug("Init SchedulerService name :{}", jobName);
        manager.clearPreviousServerStatus();
        registry.addCacheData("/" + jobName);
        jobDetail.getJobDataMap().put("manager", manager);
        manager.registryJobStart();
        try {
            scheduler = initializeScheduler(jobDetail.getKey().toString());
            scheduleJob(createTrigger(manager.getConfigService().getCron()));
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
        JobRegistry.getInstance().addSchedulerService(jobName, this);

    }

    private void scheduleJob(final CronTrigger trigger) throws SchedulerException {
        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
        }
        scheduler.start();
    }

    private Scheduler initializeScheduler(final String jobName) throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(getBaseQuartzProperties(jobName));
        Scheduler result = factory.getScheduler();
        result.getListenerManager().addTriggerListener(new TriggerListenerSupport(){

            @Override
            public String getName() {
                return "JobTriggerListener";
            }
            @Override
            public void triggerMisfired(final Trigger trigger) {
                manager.getExecutionService().setMisfire(manager.getShardingService().getLocalHostShardingItems());
            }
            
        });
        return result;
    }

    private Properties getBaseQuartzProperties(final String jobName) {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", Joiner.on("_").join(jobName, SCHEDULER_INSTANCE_NAME_SUFFIX));
        if (!manager.getConfigService().isMisfire()) {
            result.put("org.quartz.jobStore.misfireThreshold", "1");
        }
        prepareEnvironments(result);
        return result;
    }

    protected void prepareEnvironments(final Properties props) {
    }

    private CronTrigger createTrigger(final String cronExpression) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        if (manager.getConfigService().isMisfire()) {
            cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else {
            cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }
        return TriggerBuilder.newTrigger().withIdentity(Joiner.on("_").join(jobName, CRON_TRIGGER_IDENTITY_SUFFIX)).withSchedule(
            cronScheduleBuilder).build();
    }

   

    @Override
    public Date getNextFireTime() {
        List<? extends Trigger> triggers;
        try {
            triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
        } catch (final SchedulerException ex) {
            return null;
        }
        Date result = null;
        for (Trigger each : triggers) {
            Date nextFireTime = each.getNextFireTime();
            if (null == nextFireTime) {
                continue;
            }
            if (null == result) {
                result = nextFireTime;
            } else if (nextFireTime.getTime() < result.getTime()) {
                result = nextFireTime;
            }
        }
        return result;
    }

    @Override
    public void stopJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.pauseAll();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }

    }

    @Override
    public void resumeJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.resumeAll();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }

    }

    @Override
    public void triggerJob() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.triggerJob(jobDetail.getKey());
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }

    @Override
    public void shutdown() {
        manager.releaseJobResource();
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
    }

  
    @Override
    public void rescheduleJob(String cronExpression) {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.rescheduleJob(TriggerKey.triggerKey(Joiner.on("_").join(jobName, CRON_TRIGGER_IDENTITY_SUFFIX)), createTrigger(cronExpression));
            }
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        } 
    }

}
