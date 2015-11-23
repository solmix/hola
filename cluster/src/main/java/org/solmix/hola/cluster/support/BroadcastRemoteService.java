
package org.solmix.hola.cluster.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;

public class BroadcastRemoteService<T> extends AbstractClusteredService<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastRemoteService.class);

    public BroadcastRemoteService(Directory<T> directory, Container container)
    {
        super(directory, container);
    }

    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback, boolean oneway)
        throws RemoteException {
        checkRemoteServices(services, request);

        RemoteException exception = null;
        Object[] result = null;
        for (RemoteService<T> rs : services) {
            try {
                rs.invoke(callback, request, oneway);
            } catch (RemoteException e) {
                exception = e;
                LOG.warn(e.getMessage(), e);
            } catch (Throwable e) {
                exception = new RemoteException(e.getMessage(), e);
                LOG.warn(e.getMessage(), e);
            }
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }

}
