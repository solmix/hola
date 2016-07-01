package org.solmix.scheduler.services;

import java.util.Map;

public interface ConfigService
{
	/**
     * 持久化分布式作业配置信息.
     */
    void persistJobInfo();
    /**
     * 获取统计作业处理数据数量的间隔时间.
     * 
     * @return 统计作业处理数据数量的间隔时间
     */
    int getProcessCountIntervalSeconds();
    /**
     * 获取是否监控作业运行时状态.
     * 
     * @return 是否监控作业运行时状态
     */
    boolean isMonitorExecution();
    /**
     * 获取作业分片总数.
     * 
     * @return 作业分片总数
     */
    int getShardingTotalCount();
    /**
     * 获取分片序列号和个性化参数对照表.
     * 
     * @return 分片序列号和个性化参数对照表
     */
    Map<Integer, String> getShardingItemParameters();
    /**
     * 获取作业分片策略实现类全路径.
     * 
     * @return 作业分片策略实现类全路径
     */
    String getJobShardingStrategyClass();
    /**
     * 获取作业启动时间的cron表达式.
     * 
     * @return 作业启动时间的cron表达式
     */
    String getCron();

    /**
     * 获取是否开启misfire.
     * 
     * @return 是否开启misfire
     */
    boolean isMisfire();
    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    void checkMaxTimeDiffSecondsTolerable();
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
    int getConcurrentDataProcessThreadCount();
    /**
     * 获取是否开启失效转移.
     * 
     * @return 是否开启失效转移
     */
    boolean isFailover();
    /**
     * 获取作业自定义参数.
     * 
     * @return 作业自定义参数
     */
    String getJobParameter();
    /**
     * 获取每次抓取的数据量.
     * 
     * @return 每次抓取的数据量
     */
    int getFetchDataCount();

	String getScriptCommandLine();
}
