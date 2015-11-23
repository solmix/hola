
package org.solmix.hola.cluster.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;

public class FailbackRemoteService<T> extends AbstractClusteredService<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastRemoteService.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2,
        new NamedThreadFactory("failback-cluster-timer", true));

    private static final long RETRY_FAILED_PERIOD = 5 * 1000;

    private volatile ScheduledFuture<?> retryFuture;

    private final ConcurrentMap<RemoteRequest, FailbackRemoteService<?>> failed = new ConcurrentHashMap<RemoteRequest, FailbackRemoteService<?>>();

    public FailbackRemoteService(Directory<T> directory, Container container)
    {
        super(directory, container);
    }

    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback, boolean oneway)
        throws RemoteException {
        try {
            checkRemoteServices(services, request);
            RemoteService<T> service = select(loadbalance, services, request, null);
            return service.invoke(callback, request, oneway);
        } catch (Throwable e) {
            LOG.error(
                "Failback to call method " + request.getMethodName() + ", wait for retry in background. Ignored exception: " + e.getMessage() + ", ",
                e);
            addFailed(request, this);
        }
        return ObjectUtils.EMPTY_OBJECT_ARRAY;
    }

    private void addFailed(RemoteRequest request, FailbackRemoteService<T> rs) {
        if (retryFuture == null) {
            synchronized (this) {
                if (retryFuture == null) {
                    retryFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

                        @Override
                        public void run() {
                            // 收集统计信息
                            try {
                                retryFailed();
                            } catch (Throwable t) { // 防御性容错
                                LOG.error("Unexpected error occur at collect statistic", t);
                            }
                        }
                    }, RETRY_FAILED_PERIOD, RETRY_FAILED_PERIOD, TimeUnit.MILLISECONDS);
                }
            }
            failed.put(request, rs);
        }
    }

    void retryFailed() {
        if (failed.size() == 0) {
            return;
        }
        for (Map.Entry<RemoteRequest, FailbackRemoteService<?>> entry : new HashMap<RemoteRequest, FailbackRemoteService<?>>(failed).entrySet()) {
            RemoteRequest invocation = entry.getKey();
            FailbackRemoteService<?> invoker = entry.getValue();
            try {
                invoker.invoke(null, invocation, false);
                failed.remove(invocation);
            } catch (Throwable e) {
                LOG.error("Failed retry to invoke method " + invocation.getMethodName() + ", waiting again.", e);
            }
        }
    }

}
