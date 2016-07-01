package org.solmix.scheduler.model;

import org.solmix.scheduler.DataFlowJob;
import org.solmix.scheduler.job.AbstractSimpleJob;
import org.solmix.scheduler.model.support.FlowJobInfo.FlowJobInfoBuilder;
import org.solmix.scheduler.model.support.ScriptJobInfo.ScriptJobInfoBuilder;
import org.solmix.scheduler.model.support.SimpleJobInfo.SimpleJobInfoBuilder;

public class JobInfoFactory {

	/**
     * 创建简单作业配置.
     *
     * @param jobName 作业名称
     * @param jobClass 作业实现类名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @return 简单作业配置
     */
    public static SimpleJobInfoBuilder createSimpleJobConfigurationBuilder(final String jobName, 
                                      final Class<? extends AbstractSimpleJob> jobClass, final int shardingTotalCount, final String cron) {
        return new SimpleJobInfoBuilder(jobName, jobClass, shardingTotalCount, cron);
    }

    /**
     * 创建数据流作业配置.
     *
     * @param jobName 作业名称
     * @param jobClass 作业实现类名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @return 数据流作业配置
     */
    public static FlowJobInfoBuilder createDataFlowJobConfigurationBuilder(final String jobName, 
                                     final Class<? extends DataFlowJob> jobClass, final int shardingTotalCount, final String cron) {
        return new FlowJobInfoBuilder(jobName, jobClass, shardingTotalCount, cron);
    }

    /**
     * 创建脚本作业配置.
     *
     * @param jobName 作业名称
     * @param shardingTotalCount 分片总数
     * @param cron 作业启动时间的cron表达式
     * @param scriptCommandLine 作业脚本命令行
     * @return 脚本作业配置
     */
    public static ScriptJobInfoBuilder createScriptJobConfigurationBuilder(final String jobName, final int shardingTotalCount, 
                                    final String cron, final String scriptCommandLine) {
        return new ScriptJobInfoBuilder(jobName, shardingTotalCount, cron, scriptCommandLine);
    }
}
