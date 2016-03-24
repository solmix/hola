package org.solmix.scheduler.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.commons.util.StringUtils;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.exception.JobException;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ConfigService;
import org.solmix.scheduler.services.ElectionService;
import org.solmix.scheduler.services.ExecutionService;
import org.solmix.scheduler.services.JobNodePath;
import org.solmix.scheduler.services.JobServerService;
import org.solmix.scheduler.services.ShardingNode;
import org.solmix.scheduler.services.ShardingService;
import org.solmix.scheduler.services.ShardingStrategy;
import org.solmix.scheduler.services.StorageService;
import org.solmix.scheduler.services.StorageService.TransactionExecutionCallback;


public class DefaultShardingService implements ShardingService
{
    private static final Logger LOG  = LoggerFactory.getLogger(DefaultShardingService.class);
    private final String jobName;
    private final StorageService storage;
    private final ElectionService election;
    private final JobServerService jobServer;
    private final ExecutionService execution;
    private final ConfigService config;
    private final JobNodePath path;

    public DefaultShardingService(final SchedulerRegistry registry, final JobInfo info)
    {
        this.jobName=info.getJobName();
        this.storage= new DefaultStorageService(registry, info);
        this.election = new DefaultElectionService(registry, info);
        this.jobServer= new DefaultJobServerService(registry, info);
        this.execution= new DefaultExecutionService(registry, info);
        this.path= new JobNodePath(info.getJobName());
        this.config= new DefaultConfigService(registry, info);
    }

    /**
     * 设置需要重新分片的标记.
     */
    @Override
    public void setReshardingFlag() {
        storage.createJobNodeIfNeeded(ShardingNode.NECESSARY);
    }
    
    /**判断是否需要重分片.
     * 
     * @return 是否需要重分片
     */
    @Override
    public boolean isNeedSharding() {
        return storage.isJobNodeExisted(ShardingNode.NECESSARY);
    }
    
    /**
     * 如果需要分片且当前节点为主节点, 则作业分片.
     * @throws Exception 
     */
    @Override
    public void shardingIfNecessary() {
        if (!isNeedSharding()) {
            return;
        }
        if (!election.isLeader()) {
            blockUntilShardingCompleted();
            return;
        }
        if (config.isMonitorExecution()) {
            waitingOtherJobCompleted();
        }
        LOG.debug("job: sharding begin.");
        storage.fillEphemeralJobNode(ShardingNode.PROCESSING, "");
        clearShardingInfo();
        String className =config.getJobShardingStrategyClass();
        Object ss=null;
        try {
            ss = Reflection.newInstance(className);
        } catch (Exception e) {
           throw new JobException(e);
        }
        ShardingStrategy jobShardingStrategy =(ShardingStrategy)ss;
        ShardingStrategy.Option option = new ShardingStrategy.Option(jobName, config.getShardingTotalCount(), config.getShardingItemParameters());
        storage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(jobShardingStrategy.sharding(jobServer.getAvailableNodes(), option)));
        LOG.debug("job: sharding completed.");
    }
    
    
    private void blockUntilShardingCompleted() {
        while (!election.isLeader() && (storage.isJobNodeExisted(ShardingNode.NECESSARY) || storage.isJobNodeExisted(ShardingNode.PROCESSING))) {
            LOG.debug("job: sleep short time until sharding completed.");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void waitingOtherJobCompleted() {
        while (execution.hasRunningItems()) {
            LOG.debug("job: sleep short time until other job completed.");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void clearShardingInfo() {
        for (String each : jobServer.getAllNodes()) {
            storage.removeJobNodeIfExisted(ShardingNode.getShardingNode(each));
        }
    }
    
    /**
     * 获取运行在本作业服务器的分片序列号.
     * 
     * @return 运行在本作业服务器的分片序列号
     */
    @Override
    public List<Integer> getLocalHostShardingItems() {
        String ip = NetUtils.getLocalIp();
        if (!storage.isJobNodeExisted(ShardingNode.getShardingNode(ip))) {
            return Collections.emptyList();
        }
        return toItemList(storage.getJobNodeDataDirectly(ShardingNode.getShardingNode(ip)));
    }
    public static List<Integer> toItemList(final String itemsString) {
        if (StringUtils.isEmpty(itemsString)) {
            return Collections.emptyList();
        }
        String[] items = itemsString.split(",");
        List<Integer> result = new ArrayList<Integer>(items.length);
        for (String each : items) {
            int item = Integer.parseInt(each);
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }
    
    public static String toItemsString(final List<Integer> coll) {
        if (coll.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Object s : coll) {
            if (isFirst)
                isFirst = false;
            else
                sb.append(",");
            sb.append(s);
        }
        return sb.toString();
    }
    class PersistShardingInfoTransactionExecutionCallback implements TransactionExecutionCallback {
        
        private final Map<String, List<Integer>> shardingItems;
        
        public PersistShardingInfoTransactionExecutionCallback(Map<String, List<Integer>> sharding)
        {
            shardingItems=sharding;
        }

        @Override
        public void execute(final CuratorTransactionFinal curatorTransactionFinal) throws Exception {
            for (Entry<String, List<Integer>> entry : shardingItems.entrySet()) {
                curatorTransactionFinal.create().forPath(path.getFullPath(ShardingNode.getShardingNode(entry.getKey())), toItemsString(entry.getValue()).getBytes()).and();
            }
            curatorTransactionFinal.delete().forPath(path.getFullPath(ShardingNode.NECESSARY)).and();
            curatorTransactionFinal.delete().forPath(path.getFullPath(ShardingNode.PROCESSING)).and();
        }
    }
}
