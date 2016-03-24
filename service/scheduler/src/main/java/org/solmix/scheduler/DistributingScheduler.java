
package org.solmix.scheduler;

import java.util.Date;

public interface DistributingScheduler
{

    void init();

    Date getNextFireTime();

    void stopJob();

    void resumeJob();

    void triggerJob();

    void shutdown();

    void rescheduleJob(final String cronExpression);
}
