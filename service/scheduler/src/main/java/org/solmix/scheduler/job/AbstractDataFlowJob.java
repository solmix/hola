
package org.solmix.scheduler.job;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.scheduler.DataFlowJob;

import com.google.common.collect.Lists;

public abstract class AbstractDataFlowJob<T, C extends AbstractJobContext> extends AbstractDistributingJob implements DataFlowJob<T, C>
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataFlowJob.class);

    private final ExecutorService executorService;

    private final JobType type;

    public AbstractDataFlowJob()
    {
        executorService = getExecutorService();
        type = getJobType();
    }

    private JobType getJobType() {
        Class<?> target = getClass();
        while (true) {
            if (!(target.getGenericSuperclass() instanceof ParameterizedType)) {
                target = target.getSuperclass();
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType) target.getGenericSuperclass();
            if (2 != parameterizedType.getActualTypeArguments().length) {
                target = target.getSuperclass();
                continue;
            }
            Type type = parameterizedType.getActualTypeArguments()[1];
            if (JobExecutionShardingContext.class == type) {
                return JobType.CONCURRENT;
            } else if (JobExecutionSimpleContext.class == type) {
                return JobType.SEQUENCE;
            } else {
                throw new UnsupportedOperationException(String.format("Cannot support %s", type));
            }
        }
    }

    @Override
    public void updateOffset(int item, String offset) {
        getServicesManager().updateOffset(item, offset);
    }

    @Override
    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }

    @Override
    protected void executeJob(JobExecutionShardingContext shardingContext) {
        if (JobType.CONCURRENT == type) {
            if (isStreamingProcess()) {
                executeConcurrentStreamingJob(shardingContext);
            } else {
                executeConcurrentOneOffJob(shardingContext);
            }
        } else if (JobType.SEQUENCE == type) {
            if (isStreamingProcess()) {
                executeSequenceStreamingJob(shardingContext);
            } else {
                executeSequenceOneOffJob(shardingContext);
            }
        }
    }

    private void executeSequenceOneOffJob(JobExecutionShardingContext shardingContext) {
        List<T> data = fetchDataForThroughput(shardingContext);
        if (null != data && !data.isEmpty()) {
            processDataForThroughput(shardingContext, data);
        }

    }

    private void executeSequenceStreamingJob(JobExecutionShardingContext shardingContext) {
        Map<Integer, List<T>> data = fetchDataForSequence(shardingContext);
        while (!data.isEmpty() && getServicesManager().isEligibleForJobRunning()) {
            processDataForSequence(shardingContext, data);
            data = fetchDataForSequence(shardingContext);
        }
    }

    private void executeConcurrentOneOffJob(JobExecutionShardingContext shardingContext) {
        Map<Integer, List<T>> data = fetchDataForSequence(shardingContext);
        if (!data.isEmpty()) {
            processDataForSequence(shardingContext, data);
        }
    }

    private void executeConcurrentStreamingJob(JobExecutionShardingContext shardingContext) {
        List<T> data = fetchDataForThroughput(shardingContext);
        while (null != data && !data.isEmpty() && getServicesManager().isEligibleForJobRunning()) {
            processDataForThroughput(shardingContext, data);
            data = fetchDataForThroughput(shardingContext);
        }
    }

    private List<T> fetchDataForThroughput(final JobExecutionShardingContext shardingContext) {
        @SuppressWarnings("unchecked")
        List<T> result = fetchData((C) shardingContext);
        LOG.trace("job: fetch data size: {}.", result != null ? result.size() : 0);
        return result;
    }
    private Map<Integer, List<T>> fetchDataForSequence(final JobExecutionShardingContext shardingContext) {
        List<Integer> items = shardingContext.getShardingItems();
        final Map<Integer, List<T>> result = new ConcurrentHashMap<Integer, List<T>>(items.size());
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        @SuppressWarnings("unchecked")
                        List<T> data = fetchData((C) shardingContext.createJobExecutionSimpleContext(each));
                        if (null != data && !data.isEmpty()) {
                            result.put(each, data);
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
        LOG.trace("job: fetch data size: {}.", result.size());
        return result;
    }
    @SuppressWarnings("unchecked")
    private void processDataForSequence(final JobExecutionShardingContext shardingContext, final Map<Integer, List<T>> data) {
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Entry<Integer, List<T>> each : data.entrySet()) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataWithStatistics((C)shardingContext.createJobExecutionSimpleContext(each.getKey()), each.getValue());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    @SuppressWarnings("unchecked")
    private void processDataForThroughput(final JobExecutionShardingContext shardingContext, final List<T> data) {
        int threadCount = getServicesManager().getConcurrentDataProcessThreadCount();
        if (threadCount <= 1 || data.size() <= threadCount) {
            processDataWithStatistics((C) shardingContext, data);
            return;
        }
        List<List<T>> splitData = Lists.partition(data, data.size() / threadCount);
        final CountDownLatch latch = new CountDownLatch(splitData.size());
        for (final List<T> each : splitData) {
            executorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        processDataWithStatistics((C) shardingContext, each);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }

    protected abstract void processDataWithStatistics(C shardingContext, List<T> data);

    private void latchAwait(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
