package org.solmix.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobRegistry
{
    private static volatile JobRegistry instance;
    
    private Map<String, DistributingScheduler> schedulerMap = new ConcurrentHashMap<String, DistributingScheduler>();
    
    private ConcurrentHashMap<String, DistributingJob> instanceMap = new ConcurrentHashMap<String, DistributingJob>();
    
    /**
     * 获取作业注册表实例.
     * 
     * @return 作业注册表实例
     */
    public static JobRegistry getInstance() {
        if (null == instance) {
            synchronized (JobRegistry.class) {
                if (null == instance) {
                    instance = new JobRegistry();
                }
            }
        }
        return instance;
    }
    /**
     * 添加作业控制器.
     * 
     * @param jobName 作业名称
     * @param jobScheduler 作业控制器
     */
    public void addSchedulerService(final String jobName, final DistributingScheduler jobScheduler) {
        schedulerMap.put(jobName, jobScheduler);
    }
    
    /**
     * 获取作业控制器.
     * 
     * @param jobName 作业名称
     * @return 作业控制器
     */
    public DistributingScheduler getSchedulerService(final String jobName) {
        return schedulerMap.get(jobName);
    }
    
    /**
     * 添加作业实例.
     * 
     * @param jobName 作业名称
     * @param job 作业实例
     */
    public void addJobInstance(final String jobName, final DistributingJob job) {
        instanceMap.putIfAbsent(jobName, job);
    }
    
    /**
     * 获取作业实例.
     * 
     * @param jobName 作业名称
     * @return 作业实例
     */
    public DistributingJob getJobInstance(final String jobName) {
        return instanceMap.get(jobName);
    }
}
