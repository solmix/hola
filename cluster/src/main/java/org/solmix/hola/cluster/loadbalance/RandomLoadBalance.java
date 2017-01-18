
package org.solmix.hola.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Extension;

@Extension( RandomLoadBalance.NAME)
public class RandomLoadBalance extends AbstractLoadBalance
{

    public static final String NAME = "random";

    private final Random random = new Random();

    @Override
    protected <T> RemoteService<T> doSelect(List<RemoteService<T>> services, RemoteRequest request) {
        int length = services.size(); // 总个数
        int totalWeight = 0; // 总权重
        boolean sameWeight = true; // 权重是否都一样
        for (int i = 0; i < length; i++) {
            int weight = getWeight(services.get(i), request);
            totalWeight += weight; // 累计总权重
            if (sameWeight && i > 0 && weight != getWeight(services.get(i - 1), request)) {
                sameWeight = false; // 计算所有权重是否一样
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < length; i++) {
                offset -= getWeight(services.get(i), request);
                if (offset < 0) {
                    return services.get(i);
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return services.get(random.nextInt(length));
    }

}
