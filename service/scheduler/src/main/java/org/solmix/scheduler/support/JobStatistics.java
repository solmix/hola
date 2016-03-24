package org.solmix.scheduler.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ConfigService;
import org.solmix.scheduler.services.JobServerService;

public class JobStatistics
{
    private ConfigService service ;
    private final JobInfo info;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final JobServerService jobServer;
    private static ConcurrentMap<String, AtomicInteger> processSuccessCount = new ConcurrentHashMap<String, AtomicInteger>();
    
    private static ConcurrentMap<String, AtomicInteger> processFailureCount = new ConcurrentHashMap<String, AtomicInteger>();
    
    public JobStatistics(final SchedulerRegistry registry, JobInfo info)
    {
        service = new DefaultConfigService(registry, info);
        jobServer = new DefaultJobServerService(registry, info);
        this.info=info;
    }
    
    
    /**
     * 开启统计处理数据数量的作业.
     */
    public void startProcessCountJob() {
        int processCountIntervalSeconds = service.getProcessCountIntervalSeconds();
        if (processCountIntervalSeconds > 0) {
            scheduledExecutorService.scheduleAtFixedRate(new ProcessCountJob(), processCountIntervalSeconds, processCountIntervalSeconds, TimeUnit.SECONDS);
        }
    }
    
    /**
     * 停止统计处理数据数量的作业.
     */
    public void stopProcessCountJob() {
        scheduledExecutorService.shutdown();
    }
    /**
     * 增加本作业服务器处理数据正确的数量.
     * 
     * @param jobName 作业名称
     */
    public static void incrementProcessSuccessCount(final String jobName) {
        incrementProcessCount(jobName, processSuccessCount);
    }
    
    /**
     * 增加本作业服务器处理数据正确的数量.
     * 
     * @param jobName 作业名称
     * @param successCount 处理数据正确的数量
     */
    public static void incrementProcessSuccessCount(final String jobName, final int successCount) {
        incrementProcessCount(jobName, successCount, processSuccessCount);
    }
    
    /**
     * 增加本作业服务器处理数据错误的数量.
     * 
     * @param jobName 作业名称
     */
    public static void incrementProcessFailureCount(final String jobName) {
        incrementProcessCount(jobName, processFailureCount);
    }
    
    /**
     * 增加本作业服务器处理数据错误的数量.
     * 
     * @param jobName 作业名称
     * @param failureCount 处理数据错误的数量
     */
    public static void incrementProcessFailureCount(final String jobName, final int failureCount) {
        incrementProcessCount(jobName, failureCount, processFailureCount);
    }
    
    private static void incrementProcessCount(final String jobName, final ConcurrentMap<String, AtomicInteger> processCountMap) {
        processCountMap.putIfAbsent(jobName, new AtomicInteger(0));
        processCountMap.get(jobName).incrementAndGet();
    }
    
    private static void incrementProcessCount(final String jobName, final int count, final ConcurrentMap<String, AtomicInteger> processCountMap) {
        processCountMap.putIfAbsent(jobName, new AtomicInteger(0));
        processCountMap.get(jobName).addAndGet(count);
    }
    
    /**
     * 获取本作业服务器处理数据正确的数量.
     * 
     * @param jobName 作业名称
     * @return 本作业服务器处理数据正确的数量
     */
    public static int getProcessSuccessCount(final String jobName) {
        return null == processSuccessCount.get(jobName) ? 0 : processSuccessCount.get(jobName).get();
    }
    
    /**
     * 获取本作业服务器处理数据错误的数量.
     * 
     * @param jobName 作业名称
     * @return 本作业服务器处理数据错误的数量
     */
    public static int getProcessFailureCount(final String jobName) {
        return null == processFailureCount.get(jobName) ? 0 : processFailureCount.get(jobName).get();
    }
    
    /**
     * 重置统计信息.
     * 
     * @param jobName 作业名称
     */
    public static void reset(final String jobName) {
        if (processSuccessCount.containsKey(jobName)) {
            processSuccessCount.get(jobName).set(0);
        }
        if (processFailureCount.containsKey(jobName)) {
            processFailureCount.get(jobName).set(0);
        }
    }
    class  ProcessCountJob implements Runnable {
        
        
        
        public ProcessCountJob() {
        }
        
        @Override
        public void run() {
            String jobName = info.getJobName();
            jobServer.persistSuccessCount(getProcessSuccessCount(jobName));
            jobServer.persistFailureCount(getProcessFailureCount(jobName));
            reset(jobName);
        }
    }
}
