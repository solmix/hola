package org.solmix.hola.cluster.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;


public class FailoverRemoteService<T> extends AbstractClusteredService<T> implements RemoteService<T>
{
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastRemoteService.class);
    public FailoverRemoteService(Directory<T> directory, Container container)
    {
        super(directory, container);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback, boolean oneway)
        throws RemoteException {
        List<RemoteService<T>> rs=services;
        checkRemoteServices(rs, request);
        int retry = PropertiesUtils.getInt(getConsumerInfo().getServiceProperties(),request.getMethodName() + "." + HOLA.CLUSTER_RETRY_KEY,HOLA.DEFAULT_CLUSTER_RETRY);
        List<RemoteService<T>> invoked = new ArrayList<RemoteService<T>>(rs.size());
        if(retry<=0){
            retry=1;
        }
        RemoteException re=null;
        Set<String> providers = new HashSet<String>(retry);
        for(int i=0;i<retry;i++){
            if(i>0){
                checkDestroyed();
                rs=directory.list(request);
                checkRemoteServices(rs, request);
            }
            RemoteService<T> service = select(loadbalance, services, request, null);
            invoked.add(service);
            try{
                Object[] result = service.invoke(callback, request, oneway);
                if(re!=null){
                    LOG.warn("Although retry the method " + request.getMethodName()
                    + " in the service " + getServiceClass().getName()
                    + " was successful by the provider " + service.getAddress()
                    + ", but there have been failed providers " + providers 
                    + " (" + providers.size() + "/" + rs.size()
                    + ") from the directory " + directory.getAddress()
                    + " on the consumer " + NetUtils.getLocalHost()
                    + ". Last error is: "
                    + re.getMessage(), re);
                }
                return result;
            }catch(RemoteException e){
                re=e;
            }catch (Exception e) {
                re=new RemoteException(e);
            }finally {
                providers.add(service.getAddress());
            }
        }
        
        throw new RemoteException("Failed to invoke the method "
            + request.getMethodName() + " in the service " + getServiceClass().getName() 
            + ". Tried " + retry + " times of the providers " + providers 
            + " (" + providers.size() + "/" + rs.size() 
            + ") from the registry " +directory.getAddress()
            + " on the consumer " + NetUtils.getLocalHost() + ". Last error is: "
            + (re != null ? re.getMessage() : ""), re != null && re.getCause() != null ? re.getCause() : re);
    }

}
