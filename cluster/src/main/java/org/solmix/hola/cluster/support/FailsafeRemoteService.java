package org.solmix.hola.cluster.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;


public class FailsafeRemoteService<T> extends AbstractClusteredService<T> implements RemoteService<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(FailsafeRemoteService.class);
    public FailsafeRemoteService(Directory<T> directory, Container container)
    {
        super(directory, container);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback, boolean oneway)
        throws RemoteException {
        try {
            checkRemoteServices(services, request);
            RemoteService<T> service = select(loadbalance, services, request, null);
            return service.invoke(callback, request, oneway);
        } catch (Throwable e) {
            LOG.error("Failsafe ignore exception: " + e.getMessage(), e);
            return ObjectUtils.EMPTY_OBJECT_ARRAY;
        }
    }

}
