package org.solmix.scheduler.event;

public interface JobListener
{
    void handleEvent(JobEvent event);
}
