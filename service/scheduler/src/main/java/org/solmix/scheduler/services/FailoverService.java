package org.solmix.scheduler.services;

import java.util.Collection;
import java.util.List;

public interface FailoverService
{

    void failoverIfNecessary();

    void updateFailoverComplete(List<Integer> shardingItems);

    Collection<?> getLocalHostTakeOffItems();

    List<Integer> getLocalHostFailoverItems();

}
