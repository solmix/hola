package org.solmix.scheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.solmix.scheduler.job.AbstractJobContext;

public interface DataFlowJob<T,C extends AbstractJobContext> extends DistributingJob
{
    
    /**
     * 获取待处理的数据.
     * 
     * @param shardingContext 作业分片规则配置上下文
     * @return 待处理的数据集合
     */
    List<T> fetchData(final C shardingContext);
    
    /**
     * 配置是否流式处理数据.
     * 如果流式处理数据, 则fetchData不返回空结果将持续执行作业.
     * 如果非流式处理数据, 则处理数据完成后作业结束.
     * 
     * @return 是否流式处理数据
     */
    boolean isStreamingProcess();
    
    /**
     * 更新数据处理位置.
     * 
     * @param item 分片项
     * @param offset 数据处理位置
     */
    void updateOffset(final int item, final String offset);

    /**
     * 获取线程执行服务.
     */
    ExecutorService getExecutorService();
}
