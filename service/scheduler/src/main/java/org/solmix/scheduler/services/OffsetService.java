package org.solmix.scheduler.services;

import java.util.List;
import java.util.Map;

public interface OffsetService
{

    void updateOffset(int item, String offset);

    Map<Integer, String> getOffsets(List<Integer> shardingItems);

}
