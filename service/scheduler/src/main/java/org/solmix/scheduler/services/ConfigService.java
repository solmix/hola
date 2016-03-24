package org.solmix.scheduler.services;

import java.util.Map;

public interface ConfigService
{

    void persistConfig();

    int getProcessCountIntervalSeconds();

    boolean isMonitorExecution();

    int getShardingTotalCount();

    Map<Integer, String> getShardingItemParameters();

    String getJobShardingStrategyClass();

    String getCron();

    boolean isMisfire();

    void checkMaxTimeDiffSecondsTolerable();

    int getConcurrentDataProcessThreadCount();

    boolean isFailover();

    String getJobParameter();

    int getFetchDataCount();
}
