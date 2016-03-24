package org.solmix.scheduler.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.scheduler.JobRegistry;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ExecutionNode;
import org.solmix.scheduler.services.FailoverNode;
import org.solmix.scheduler.services.FailoverService;
import org.solmix.scheduler.services.JobServerService;
import org.solmix.scheduler.services.ShardingService;
import org.solmix.scheduler.services.StorageService;
import org.solmix.scheduler.services.StorageService.LeaderExecutionCallback;


public class DefaultFailoverService implements FailoverService
{
    private static final Logger LOG  = LoggerFactory.getLogger(DefaultFailoverService.class);

    private final JobInfo info;
    
    private final StorageService storage;
    
    private final JobServerService jobServer;
    
    private final ShardingService sharding;

    public DefaultFailoverService(final SchedulerRegistry registry, JobInfo info)
    {
        this.info=info;
        storage = new DefaultStorageService(registry, info);
        jobServer= new DefaultJobServerService(registry, info);
        sharding= new DefaultShardingService(registry, info);
        
    }
    /**
     * 设置失效的分片项标记.
     * 
     * @param item 崩溃的作业项
     */
    public void setCrashedFailoverFlag(final int item) {
        if (!isFailoverAssigned(item)) {
            storage.createJobNodeIfNeeded(FailoverNode.getItemsNode(item));
        }
    }
    
    private boolean isFailoverAssigned(final Integer item) {
        return storage.isJobNodeExisted(FailoverNode.getExecutionFailoverNode(item));
    }
    
    /**
     * 如果需要失效转移, 则设置作业失效转移.
     */
    @Override
    public void failoverIfNecessary() {
        if (!needFailover()) {
            return;
        }
        storage.executeInLeader(FailoverNode.LATCH, new FailoverLeaderExecutionCallback());
    }
    
    private boolean needFailover() {
        return storage.isJobNodeExisted(FailoverNode.ITEMS_ROOT) && !storage.getJobNodeChildrenKeys(FailoverNode.ITEMS_ROOT).isEmpty() && jobServer.isAvailable();
    }
    
    /**
     * 更新执行完毕失效转移的分片项状态.
     * 
     * @param items 执行完毕失效转移的分片项列表
     */
    @Override
    public void updateFailoverComplete(final List<Integer> items) {
        for (int each : items) {
            storage.removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(each));
        }
    }
    
    /**
     * 获取运行在本作业服务器的失效转移序列号.
     * 
     * @return 运行在本作业服务器的失效转移序列号
     */
    @Override
    public List<Integer> getLocalHostFailoverItems() {
        List<String> items = storage.getJobNodeChildrenKeys(ExecutionNode.ROOT);
        List<Integer> result = new ArrayList<Integer>(items.size());
        String ip = NetUtils.getLocalIp();
        for (String each : items) {
            int item = Integer.parseInt(each);
            String node = FailoverNode.getExecutionFailoverNode(item);
            if (storage.isJobNodeExisted(node) && ip.equals(storage.getJobNodeDataDirectly(node))) {
                result.add(item);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * 获取运行在本作业服务器的被失效转移的序列号.
     * 
     * @return 运行在本作业服务器的被失效转移的序列号
     */
    @Override
    public List<Integer> getLocalHostTakeOffItems() {
        List<Integer> shardingItems = sharding.getLocalHostShardingItems();
        List<Integer> result = new ArrayList<Integer>(shardingItems.size());
        for (int each : shardingItems) {
            if (storage.isJobNodeExisted(FailoverNode.getExecutionFailoverNode(each))) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * 删除作业失效转移信息.
     */
    public void removeFailoverInfo() {
        for (String each : storage.getJobNodeChildrenKeys(ExecutionNode.ROOT)) {
            storage.removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(Integer.parseInt(each)));
        }
    }
    
    class FailoverLeaderExecutionCallback implements LeaderExecutionCallback {
        
        @Override
        public void execute() {
            if (!needFailover()) {
                return;
            }
            int crashedItem = Integer.parseInt(storage.getJobNodeChildrenKeys(FailoverNode.ITEMS_ROOT).get(0));
            LOG.debug("job: failover job begin, crashed item:{}.", crashedItem);
            storage.fillEphemeralJobNode(FailoverNode.getExecutionFailoverNode(crashedItem), NetUtils.getLocalIp());
            storage.removeJobNodeIfExisted(FailoverNode.getItemsNode(crashedItem));
            JobRegistry.getInstance().getSchedulerService(info.getJobName()).triggerJob();
        }
    }
}
