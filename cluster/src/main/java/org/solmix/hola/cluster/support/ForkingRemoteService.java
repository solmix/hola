package org.solmix.hola.cluster.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;


public class ForkingRemoteService<T> extends AbstractClusteredService<T> implements RemoteService<T>
{
    private final ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("forking-cluster-timer", true)); 

    public ForkingRemoteService(Directory<T> directory, Container container)
    {
        super(directory, container);
    }

    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services,final RemoteRequest request, final ClientCallback callback,final boolean oneway)
        throws RemoteException {
        checkRemoteServices(services, request);
        final  List<RemoteService<T>> selected;
        final int forks = PropertiesUtils.getInt(getConsumerInfo().getServiceProperties(), HOLA.CLUSTER_FORK_KEY,HOLA.DEFAULT_CLUSTER_FORK);
        final int timeout =  PropertiesUtils.getInt(getConsumerInfo().getServiceProperties(), HOLA.TIMEOUT_KEY,HOLA.DEFAULT_TIMEOUT);
        if(forks<=0||forks>=services.size()){
            selected=services;
        }else{
            selected = new ArrayList<RemoteService<T>>();
            for(int i=0;i<forks;i++){
                RemoteService<T> service = select(loadbalance, services, request, null);
                if(!selected.contains(service)){
                    selected.add(service);
                }
            }
        }
        final AtomicInteger count = new AtomicInteger();
        final BlockingQueue<Object[]> ref = new LinkedBlockingQueue<Object[]>();
        for(final RemoteService<T> service :selected){
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try{
                  Object[] result=  service.invoke(callback, request, oneway);
                    ref.offer(result);
                    }catch(Throwable e){
                        int value = count.incrementAndGet();
                        if (value >= selected.size()) {
                            ref.offer(new Object[]{e});
                        }
                    }
                }
            });
        }
        try {
            Object[] ret = ref.poll(timeout, TimeUnit.MILLISECONDS);
            if (ret instanceof Throwable[]) {
                Throwable e = ((Throwable[]) ret)[0];
                throw new RemoteException("Failed to forking invoke provider " + selected + ", but no luck to perform the invocation. Last error is: " + e.getMessage(), e.getCause() != null ? e.getCause() : e);
            }
            return  ret;
        } catch (InterruptedException e) {
            throw new RemoteException("Failed to forking invoke provider " + selected + ", but no luck to perform the invocation. Last error is: " + e.getMessage(), e);
        }
    }

}
