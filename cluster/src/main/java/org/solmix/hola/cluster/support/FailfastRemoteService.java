package org.solmix.hola.cluster.support;

import java.util.List;

import org.solmix.commons.util.NetUtils;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;


public class FailfastRemoteService<T> extends AbstractClusteredService<T> implements RemoteService<T>
{

    public FailfastRemoteService(Directory<T> directory, Container container)
    {
        super(directory, container);
    }

    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback, boolean oneway)
        throws RemoteException {
        checkRemoteServices(services, request);
        RemoteService<T> service = select(loadbalance, services, request, null);
        try {
            return service.invoke(callback,request,oneway);
        } catch (RemoteException e) {
            throw e;
        }catch (Exception e) {
            throw new RemoteException("Failfast invoke providers " + service.getAddress() + " " 
                + loadbalance.getClass().getSimpleName() + " select from all providers " + services 
                + " for service " + getServiceClass().getName() + " method " 
                + request.getMethodName() + " on consumer " + NetUtils.getLocalHost() 
                + ", but no luck to perform the invocation. Last error is: " + e.getMessage(), e.getCause() != null ? e.getCause() : e);
        }
    }

}
