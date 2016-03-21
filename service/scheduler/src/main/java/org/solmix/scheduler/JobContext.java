
package org.solmix.scheduler;

public class JobContext
{

    private String jobName;

    private int shardingCount;

    private String jobParameter;

    private boolean monitor;

    private int fetchCount;

    public String getJobName() {
        return jobName;
    }

    public int getShardingCount() {
        return shardingCount;
    }

    public String getJobParameter() {
        return jobParameter;
    }

    public boolean isMonitor() {
        return monitor;
    }

    public int getFetchCount() {
        return fetchCount;
    }
}
