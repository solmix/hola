package org.solmix.hola.cluster.support;

import java.util.List;

import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;


public class AvailableRemoteService<T> extends AbstractClusteredService<T>
{

    public AvailableRemoteService(Directory<T> directory,Container container)
    {
        super(directory, container);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback, boolean oneway)
        throws RemoteException {
        for (RemoteService<T> service : services) {
            if (service.isAvailable()) {
                return service.invoke(callback, request, oneway);
            }
        }
        throw new RemoteException("No provider available in " + services);
    }

}
