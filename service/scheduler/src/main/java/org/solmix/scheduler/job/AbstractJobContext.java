package org.solmix.scheduler.job;


public abstract class AbstractJobContext
{
    /**
     * 作业名称.
     */
    private String jobName;
    
    /**
     * 分片总数.
     */
    private int shardingTotalCount;
    
    /**
     * 作业自定义参数.
     * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
     */
    private String jobParameter;
    
    /**
     * 监控作业执行时状态.
     */
    private boolean monitorExecution;
    
    /**
     * 每次抓取的数据量.
     */
    private int fetchDataCount;

    
    public String getJobName() {
        return jobName;
    }

    
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    
    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    
    public void setShardingTotalCount(int shardingTotalCount) {
        this.shardingTotalCount = shardingTotalCount;
    }

    
    public String getJobParameter() {
        return jobParameter;
    }

    
    public void setJobParameter(String jobParameter) {
        this.jobParameter = jobParameter;
    }

    
    public boolean isMonitorExecution() {
        return monitorExecution;
    }

    
    public void setMonitorExecution(boolean monitorExecution) {
        this.monitorExecution = monitorExecution;
    }

    
    public int getFetchDataCount() {
        return fetchDataCount;
    }

    
    public void setFetchDataCount(int fetchDataCount) {
        this.fetchDataCount = fetchDataCount;
    }
    
}
