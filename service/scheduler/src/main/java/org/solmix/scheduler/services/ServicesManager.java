
package org.solmix.scheduler.services;

import java.util.List;

import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.event.JobListener;
import org.solmix.scheduler.job.JobExecutionShardingContext;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.support.DefaultConfigService;
import org.solmix.scheduler.support.DefaultElectionService;
import org.solmix.scheduler.support.DefaultExecutionService;
import org.solmix.scheduler.support.DefaultFailoverService;
import org.solmix.scheduler.support.DefaultJobServerService;
import org.solmix.scheduler.support.DefaultMonitorService;
import org.solmix.scheduler.support.DefaultOffsetService;
import org.solmix.scheduler.support.DefaultShardingService;
import org.solmix.scheduler.support.JobStatistics;

public class ServicesManager
{

    private final JobServerService jobServer;

    private final ElectionService election;

    private final ConfigService config;

    private final JobStatistics statistics;

    private final ShardingService sharding;

    private final MonitorService monitor;
    private final FailoverService failover;
    private final OffsetService offset;

    private final ExecutionService execution;
    private final List<JobListener> jobListeners;

    public ServicesManager(final SchedulerRegistry registry, JobInfo info, final List<JobListener> jobListeners)
    {
        this.jobListeners=jobListeners;
        jobServer = new DefaultJobServerService(registry, info);
        election = new DefaultElectionService(registry, info);
        config = new DefaultConfigService(registry, info);
        statistics = new JobStatistics(registry, info);
        sharding = new DefaultShardingService(registry, info);
        monitor = new DefaultMonitorService(registry, info);
        execution = new DefaultExecutionService(registry, info);
        failover= new DefaultFailoverService(registry, info);
        offset= new DefaultOffsetService(registry, info);
    }
    public void registryJobStart() {
        election.leaderElection();
        config.persistConfig();
        jobServer.persistServerOnline();
        jobServer.clearJobStoppedStatus();
        statistics.startProcessCountJob();
        sharding.setReshardingFlag();
        monitor.start();

    }

    public void clearPreviousServerStatus() {
        jobServer.clearPreviousServerStatus();
    }

    public ConfigService getConfigService() {
        return config;
    }

    public ElectionService getElectionService() {
        return election;
    }

    public JobServerService getJobServerService() {
        return jobServer;
    }

    public ShardingService getShardingService() {
        return sharding;
    }

    public MonitorService getMonitorService() {
        return monitor;
    }

    public ExecutionService getExecutionService() {
        return execution;
    }
    public void releaseJobResource() {
        monitor.close();
        statistics.stopProcessCountJob();
    }

    /**
     * 获取当前作业服务器运行时分片上下文.
     *
     * @return 当前作业服务器运行时分片上下文
     */
    public JobExecutionShardingContext getShardingContext() {
        sharding.shardingIfNecessary();
        return execution.getJobExecutionShardingContext();
    }
    public boolean misfireIfNecessary(final List<Integer> object) {
        return execution.misfireIfNecessary(object);
    }
    
    /**
     * 获取同时处理数据的并发线程数.
     *
     * <p>
     * 不能小于1.
     * 仅ThroughputDataFlow作业有效.
     * </p>
     *
     * @return 同时处理数据的并发线程数
     */
    public int getConcurrentDataProcessThreadCount() {
        return config.getConcurrentDataProcessThreadCount();
    }
    
    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    public void checkMaxTimeDiffSecondsTolerable() {
        config.checkMaxTimeDiffSecondsTolerable();
    }
    
    /**
     * 如果需要失效转移, 则设置作业失效转移.
     */
    public void failoverIfNecessary() {
        if (config.isFailover() && !jobServer.isJobStoppedManually()) {
            failover.failoverIfNecessary();
        }
    }
    
    /**
     * 注册作业启动信息.
     *
     * @param shardingContext 作业运行时分片上下文
     */
    public void registerJobBegin(final JobExecutionShardingContext shardingContext) {
        execution.registerJobBegin(shardingContext);
    }
    
    /**
     * 注册作业完成信息.
     *
     * @param shardingContext 作业运行时分片上下文
     */
    public void registerJobCompleted(final JobExecutionShardingContext shardingContext) {
        execution.registerJobCompleted(shardingContext);
        if (config.isFailover()) {
            failover.updateFailoverComplete(shardingContext.getShardingItems());
        }
    }
    
   
    
    /**
     * 清除任务被错过执行的标记.
     *
     * @param shardingItems 需要清除错过执行的任务分片项
     */
    public void clearMisfire(final List<Integer> shardingItems) {
        execution.clearMisfire(shardingItems);
    }
    
    /**
     * 判断作业是否需要执行错过的任务.
     * 
     * @param shardingItems 任务分片项集合
     * @return 作业是否需要执行错过的任务
     */
    public boolean isExecuteMisfired(final List<Integer> shardingItems) {
        return isEligibleForJobRunning() && config.isMisfire() && !execution.getMisfiredJobItems(shardingItems).isEmpty();
    }
    
    /**
     * 判断作业是否符合继续运行的条件.
     * 
     * <p>如果作业停止或需要重分片则作业将不会继续运行.</p>
     * 
     * @return 作业是否符合继续运行的条件
     */
    public boolean isEligibleForJobRunning() {
        return !jobServer.isJobStoppedManually() && !sharding.isNeedSharding();
    }
    
    /**判断是否需要重分片.
     *
     * @return 是否需要重分片
     */
    public boolean isNeedSharding() {
        return sharding.isNeedSharding();
    }
    
    /**
     * 更新数据处理位置.
     *
     * @param item 分片项
     * @param offset 数据处理位置
     */
    public void updateOffset(final int item, final String offset) {
        this.offset.updateOffset(item, offset);
    }
    
    /**
     * 作业执行前的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    public void beforeJobExecuted(final JobExecutionShardingContext shardingContext) {
        for (JobListener each : jobListeners) {
            each.beforeJobExecuted(shardingContext);
        }
    }
    
    /**
     * 作业执行后的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    public void afterJobExecuted(final JobExecutionShardingContext shardingContext) {
        for (JobListener each : jobListeners) {
            each.afterJobExecuted(shardingContext);
        }
    }
}
