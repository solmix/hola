package org.solmix.hola.cluster.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.util.AtomicPositiveInteger;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Extension;

@Extension( RoundRobinLoadBalance.NAME)
public class RoundRobinLoadBalance extends AbstractLoadBalance
{
    public static final String NAME = "roundrobin"; 
    
    private final ConcurrentMap<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();

    private final ConcurrentMap<String, AtomicPositiveInteger> weightSequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();

    @Override
    protected <T> RemoteService<T> doSelect(List<RemoteService<T>> services, RemoteRequest request) {
        String key =PropertiesUtils.getServiceKey(services.get(0).getServiceProperties()) + "." + request.getMethodName();
        int length = services.size(); // 总个数
        int maxWeight = 0; // 最大权重
        int minWeight = Integer.MAX_VALUE; // 最小权重
        for (int i = 0; i < length; i++) {
            int weight = getWeight(services.get(i), request);
            maxWeight = Math.max(maxWeight, weight); // 累计最大权重
            minWeight = Math.min(minWeight, weight); // 累计最小权重
        }
        if (maxWeight > 0 && minWeight < maxWeight) { // 权重不一样
            AtomicPositiveInteger weightSequence = weightSequences.get(key);
            if (weightSequence == null) {
                weightSequences.putIfAbsent(key, new AtomicPositiveInteger());
                weightSequence = weightSequences.get(key);
            }
            int currentWeight = weightSequence.getAndIncrement() % maxWeight;
            List<RemoteService<T>> weightInvokers = new ArrayList<RemoteService<T>>();
            for (RemoteService<T> invoker : services) { // 筛选权重大于当前权重基数的Invoker
                if (getWeight(invoker, request) > currentWeight) {
                    weightInvokers.add(invoker);
                }
            }
            int weightLength = weightInvokers.size();
            if (weightLength == 1) {
                return weightInvokers.get(0);
            } else if (weightLength > 1) {
                services = weightInvokers;
                length = services.size();
            }
        }
        AtomicPositiveInteger sequence = sequences.get(key);
        if (sequence == null) {
            sequences.putIfAbsent(key, new AtomicPositiveInteger());
            sequence = sequences.get(key);
        }
        // 取模轮循
        return services.get(sequence.getAndIncrement() % length);
    }

}
