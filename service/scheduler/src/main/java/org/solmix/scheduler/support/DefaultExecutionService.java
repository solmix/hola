package org.solmix.scheduler.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.solmix.scheduler.DistributingScheduler;
import org.solmix.scheduler.JobRegistry;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.job.JobExecutionShardingContext;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ConfigService;
import org.solmix.scheduler.services.ExecutionNode;
import org.solmix.scheduler.services.ExecutionService;
import org.solmix.scheduler.services.FailoverService;
import org.solmix.scheduler.services.JobServerService;
import org.solmix.scheduler.services.OffsetService;
import org.solmix.scheduler.services.ShardingService;
import org.solmix.scheduler.services.StorageService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;


public class DefaultExecutionService implements ExecutionService
{
    private final JobInfo info;
    
    private final StorageService storage;
    
    private final JobServerService jobServer;
    
    private final ShardingService sharding;
    
    private final ConfigService config;
    private final FailoverService failover;
    private final OffsetService offset;

    public DefaultExecutionService(final SchedulerRegistry registry, JobInfo info)
    {
        this.info=info;
        storage = new DefaultStorageService(registry, info);
        jobServer= new DefaultJobServerService(registry, info);
        sharding= new DefaultShardingService(registry, info);
        config = new DefaultConfigService(registry, info);
        failover = new DefaultFailoverService(registry, info);
        offset = new DefaultOffsetService(registry, info);
    }
    
    /**
     * 注册作业启动信息.
     * 
     * @param jobExecutionShardingContext 作业运行时分片上下文
     */
    @Override
    public void registerJobBegin(final JobExecutionShardingContext jobExecutionShardingContext) {
        if (!jobExecutionShardingContext.getShardingItems().isEmpty() && config.isMonitorExecution()) {
            jobServer.updateServerStatus(JobServerService.Status.RUNNING);
            for (int each : jobExecutionShardingContext.getShardingItems()) {
                storage.removeJobNodeIfExisted(ExecutionNode.getCompletedNode(each));
                storage.fillEphemeralJobNode(ExecutionNode.getRunningNode(each), "");
                storage.replaceJobNode(ExecutionNode.getLastBeginTimeNode(each), System.currentTimeMillis());
                DistributingScheduler jobScheduler = JobRegistry.getInstance().getSchedulerService(info.getJobName());
                if (null == jobScheduler) {
                    continue;
                }
                Date nextFireTime = jobScheduler.getNextFireTime();
                if (null != nextFireTime) {
                    storage.replaceJobNode(ExecutionNode.getNextFireTimeNode(each), nextFireTime.getTime());
                }
            }
        }
    }
    
    /**
     * 注册作业完成信息.
     * 
     * @param jobExecutionShardingContext 作业运行时分片上下文
     */
    @Override
    public void registerJobCompleted(final JobExecutionShardingContext jobExecutionShardingContext) {
        if (!config.isMonitorExecution()) {
            return;
        }
        jobServer.updateServerStatus(JobServerService.Status.READY);
        for (int each : jobExecutionShardingContext.getShardingItems()) {
            storage.createJobNodeIfNeeded(ExecutionNode.getCompletedNode(each));
            storage.removeJobNodeIfExisted(ExecutionNode.getRunningNode(each));
            storage.replaceJobNode(ExecutionNode.getLastCompleteTimeNode(each), System.currentTimeMillis());
        }
    }
    
    /**
     * 设置修复运行时分片信息标记的状态标志位.
     */
    public void setNeedFixExecutionInfoFlag() {
        storage.createJobNodeIfNeeded(ExecutionNode.NECESSARY);
    }
    
    /**
     * 清除分配分片序列号的运行状态.
     * 
     * <p>
     * 用于作业服务器恢复连接注册中心而重新上线的场景, 先清理上次运行时信息.
     * </p>
     * 
     * @param items 需要清理的分片项列表
     */
    public void clearRunningInfo(final List<Integer> items) {
        for (int each : items) {
            storage.removeJobNodeIfExisted(ExecutionNode.getRunningNode(each));
        }
    }
    
    /**
     * 如果满足条件，设置任务被错过执行的标记.
     * 
     * @param items 需要设置错过执行的任务分片项
     * @return 是否满足misfire条件
     */
    @Override
    public boolean misfireIfNecessary(final List<Integer> items) {
        if (hasRunningItems(items)) {
            setMisfire(items);
            return true;
        }
        return false;
    }
    
    /**
     * 设置任务被错过执行的标记.
     * 
     * @param items 需要设置错过执行的任务分片项
     */
    @Override
    public void setMisfire(final List<Integer> items) {
        if (!config.isMonitorExecution()) {
            return;
        }
        for (int each : items) {
            storage.createJobNodeIfNeeded(ExecutionNode.getMisfireNode(each));
        }
    }
    
    /**
     * 获取标记被错过执行的任务分片项.
     * 
     * @param items 需要获取标记被错过执行的任务分片项
     * @return 标记被错过执行的任务分片项
     */
    @Override
    public List<Integer> getMisfiredJobItems(final List<Integer> items) {
        List<Integer> result = new ArrayList<Integer>(items.size());
        for (int each : items) {
            if (storage.isJobNodeExisted(ExecutionNode.getMisfireNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * 清除任务被错过执行的标记.
     * 
     * @param items 需要清除错过执行的任务分片项
     */
    @Override
    public void clearMisfire(final List<Integer> items) {
        for (int each : items) {
            storage.removeJobNodeIfExisted(ExecutionNode.getMisfireNode(each));
        }
    }
    
    /**
     * 删除作业执行时信息.
     */
    public void removeExecutionInfo() {
        storage.removeJobNodeIfExisted(ExecutionNode.ROOT);
    }
    
    /**
     * 判断该分片是否已完成.
     * 
     * @param item 运行中的分片路径
     * @return 该分片是否已完成
     */
    public boolean isCompleted(final int item) {
        return storage.isJobNodeExisted(ExecutionNode.getCompletedNode(item));
    }
    
    /**
     * 判断分片项中是否还有执行中的作业.
     * 
     * @param items 需要判断的分片项列表
     * @return 分片项中是否还有执行中的作业
     */
    public boolean hasRunningItems(final List<Integer> items) {
        if (!config.isMonitorExecution()) {
            return false;
        }
        for (int each : items) {
            if (storage.isJobNodeExisted(ExecutionNode.getRunningNode(each))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是否还有执行中的作业.
     * 
     * @return 是否还有执行中的作业
     */
    @Override
    public boolean hasRunningItems() {
        return hasRunningItems(getAllItems());
    }
    
    private List<Integer> getAllItems() {
        return Lists.transform(storage.getJobNodeChildrenKeys(ExecutionNode.ROOT), new Function<String, Integer>() {
            
            @Override
            public Integer apply(final String input) {
                return Integer.parseInt(input);
            }
        });
    }

    @Override
    public JobExecutionShardingContext getJobExecutionShardingContext() {
        JobExecutionShardingContext result = new JobExecutionShardingContext();
        result.setJobName(info.getJobName());
        result.setShardingTotalCount(config.getShardingTotalCount());
        List<Integer> shardingItems = sharding.getLocalHostShardingItems();
        if (config.isFailover()) {
            List<Integer> failoverItems = failover.getLocalHostFailoverItems();
            if (!failoverItems.isEmpty()) {
                result.setShardingItems(failoverItems);
            } else {
                shardingItems.removeAll(failover.getLocalHostTakeOffItems());
                result.setShardingItems(shardingItems);
            }
        } else {
            result.setShardingItems(shardingItems);
        }
        boolean isMonitorExecution = config.isMonitorExecution();
        if (isMonitorExecution) {
            removeRunningItems(shardingItems);
        }
        result.setJobParameter(config.getJobParameter());
        result.setMonitorExecution(isMonitorExecution);
        result.setFetchDataCount(config.getFetchDataCount());
        if (result.getShardingItems().isEmpty()) {
            return result;
        }
        Map<Integer, String> shardingItemParameters = config.getShardingItemParameters();
        for (int each : result.getShardingItems()) {
            if (shardingItemParameters.containsKey(each)) {
                result.getShardingItemParameters().put(each, shardingItemParameters.get(each));
            }
        }
        result.setOffsets(offset.getOffsets(result.getShardingItems()));
        return result;
    }
    
    private void removeRunningItems(final List<Integer> items) {
        List<Integer> toBeRemovedItems = new ArrayList<Integer>(items.size());
        for (int each : items) {
            if (isRunningItem(each)) {
                toBeRemovedItems.add(each);
            }
        }
        items.removeAll(toBeRemovedItems);
    }
    
    private boolean isRunningItem(final int item) {
        return storage.isJobNodeExisted(ExecutionNode.getRunningNode(item));
    }
}
