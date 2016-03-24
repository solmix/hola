package org.solmix.scheduler.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.solmix.commons.util.StringUtils;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.exception.JobConflictException;
import org.solmix.scheduler.exception.ShardingItemParametersException;
import org.solmix.scheduler.exception.TimeDiffIntolerableException;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ConfigNode;
import org.solmix.scheduler.services.ConfigService;
import org.solmix.scheduler.services.StorageService;


public class DefaultConfigService implements ConfigService
{

    private final StorageService storage;

    public DefaultConfigService(final SchedulerRegistry registry,JobInfo info){
        storage = new DefaultStorageService(registry, info);
    }
    @Override
    public void persistConfig() {
        checkConflictJob();
        registerJobInfo();
    }
    
    private void checkConflictJob() {
        if (storage.isJobNodeExisted(ConfigNode.JOB_CLASS)) {
            String registeredJobClassName = storage.getJobNodeData(ConfigNode.JOB_CLASS);
            String toBeRegisteredJobClassName = storage.getJobInfo().getJobClass().getCanonicalName();
            if (!toBeRegisteredJobClassName.equals(registeredJobClassName)) {
                throw new JobConflictException(storage.getJobInfo().getJobName(), registeredJobClassName, toBeRegisteredJobClassName);
            }
        }
    }
    
    private void registerJobInfo() {
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.JOB_CLASS, storage.getJobInfo().getJobClass().getCanonicalName());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.SHARDING_TOTAL_COUNT, storage.getJobInfo().getShardingTotalCount());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.SHARDING_ITEM_PARAMETERS, storage.getJobInfo().getShardingItemParameters());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.JOB_PARAMETER, storage.getJobInfo().getJobParameter());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.CRON, storage.getJobInfo().getCron());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.MONITOR_EXECUTION, storage.getJobInfo().isMonitorExecution());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.PROCESS_COUNT_INTERVAL_SECONDS, storage.getJobInfo().getProcessCountIntervalSeconds());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT, storage.getJobInfo().getConcurrentDataProcessThreadCount());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.FETCH_DATA_COUNT, storage.getJobInfo().getFetchDataCount());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.MAX_TIME_DIFF_SECONDS, storage.getJobInfo().getMaxTimeDiffSeconds());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.FAILOVER, storage.getJobInfo().isFailover());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.MISFIRE, storage.getJobInfo().isMisfire());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.JOB_SHARDING_STRATEGY_CLASS, storage.getJobInfo().getJobShardingStrategyClass());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.DESCRIPTION, storage.getJobInfo().getDescription());
        storage.fillJobNodeIfNullOrOverwrite(ConfigNode.MONITOR_PORT, storage.getJobInfo().getMonitorPort());
    }
    
    /**
     * 获取作业分片总数.
     * 
     * @return 作业分片总数
     */
    public int getShardingTotalCount() {
        return Integer.parseInt(storage.getJobNodeDataDirectly(ConfigNode.SHARDING_TOTAL_COUNT));
    }
    
    /**
     * 获取分片序列号和个性化参数对照表.
     * 
     * @return 分片序列号和个性化参数对照表
     */
    public Map<Integer, String> getShardingItemParameters() {
        String value = storage.getJobNodeDataDirectly(ConfigNode.SHARDING_ITEM_PARAMETERS);
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyMap();
        }
        String[] shardingItemParameters = value.split(",");
        Map<Integer, String> result = new HashMap<Integer, String>(shardingItemParameters.length);
        for (String each : shardingItemParameters) {
            String[] pair = each.trim().split("=");
            if (2 != pair.length) {
                throw new ShardingItemParametersException("Sharding item parameters '%s' format error, should be int=xx,int=xx", value);
            }
            try {
                result.put(Integer.parseInt(pair[0].trim()), pair[1].trim());
            } catch (final NumberFormatException ex) {
                throw new ShardingItemParametersException("Sharding item parameters key '%s' is not an integer.", pair[0]);
            }
        }
        return result;
    }
    
    /**
     * 获取作业自定义参数.
     * 
     * @return 作业自定义参数
     */
    public String getJobParameter() {
        return storage.getJobNodeDataDirectly(ConfigNode.JOB_PARAMETER);
    }
    
    /**
     * 获取作业启动时间的cron表达式.
     * 
     * @return 作业启动时间的cron表达式
     */
    public String getCron() {
        return storage.getJobNodeDataDirectly(ConfigNode.CRON);
    }
    
    /**
     * 获取是否监控作业运行时状态.
     * 
     * @return 是否监控作业运行时状态
     */
    @Override
    public boolean isMonitorExecution() {
        return Boolean.valueOf(storage.getJobNodeData(ConfigNode.MONITOR_EXECUTION));
    }
    
    /**
     * 获取统计作业处理数据数量的间隔时间.
     * 
     * @return 统计作业处理数据数量的间隔时间
     */
    @Override
    public int getProcessCountIntervalSeconds() {
        return Integer.parseInt(storage.getJobNodeData(ConfigNode.PROCESS_COUNT_INTERVAL_SECONDS));
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
        return Integer.parseInt(storage.getJobNodeData(ConfigNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT));
    }
    
    /**
     * 获取每次抓取的数据量.
     * 
     * @return 每次抓取的数据量
     */
    public int getFetchDataCount() {
        return Integer.parseInt(storage.getJobNodeData(ConfigNode.FETCH_DATA_COUNT));
    }
    
    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    public void checkMaxTimeDiffSecondsTolerable() {
        int maxTimeDiffSeconds =  Integer.parseInt(storage.getJobNodeData(ConfigNode.MAX_TIME_DIFF_SECONDS));
        if (-1  == maxTimeDiffSeconds) {
            return;
        }
        long timeDiff = Math.abs(System.currentTimeMillis() - storage.getRegistryCenterTime());
        if (timeDiff > maxTimeDiffSeconds * 1000L) {
            throw new TimeDiffIntolerableException(Long.valueOf(timeDiff / 1000).intValue(), maxTimeDiffSeconds);
        }
    }
    
    /**
     * 获取是否开启失效转移.
     * 
     * @return 是否开启失效转移
     */
    public boolean isFailover() {
        return isMonitorExecution() && Boolean.valueOf(storage.getJobNodeData(ConfigNode.FAILOVER));
    }
    
    /**
     * 获取是否开启misfire.
     * 
     * @return 是否开启misfire
     */
    public boolean isMisfire() {
        return Boolean.valueOf(storage.getJobNodeData(ConfigNode.MISFIRE));
    }
    
    /**
     * 获取作业分片策略实现类全路径.
     * 
     * @return 作业分片策略实现类全路径
     */
    public String getJobShardingStrategyClass() {
        return storage.getJobNodeData(ConfigNode.JOB_SHARDING_STRATEGY_CLASS);
    }
    
    /**
     * 获取作业监控端口.
     * 
     * @return 作业监控端口
     */
    public int getMonitorPort() {
        return Integer.valueOf(storage.getJobNodeData(ConfigNode.MONITOR_PORT));
    }
    
    /**
     * 获取作业名称.
     * 
     * @return 作业名称
     */
    public String getJobName() {
        return storage.getJobInfo().getJobName();
    }

}
