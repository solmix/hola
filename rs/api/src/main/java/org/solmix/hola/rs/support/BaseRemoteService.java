
package org.solmix.hola.rs.support;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.solmix.commons.util.Assert;
import org.solmix.exchange.Client;
import org.solmix.exchange.ClientCallback;
import org.solmix.exchange.model.OperationInfo;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.ClientFactory;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;

public abstract class BaseRemoteService<T> extends AbstractRemoteService<T> implements RemoteService<T>
{

    private RemoteReferenceImpl<T> refer;

    private Client client;

    private String address;
    

    protected ClientFactory clientFactory;

    private ReentrantLock lock = new ReentrantLock();

    public BaseRemoteService()
    {

    }

    public BaseRemoteService(ClientFactory clientFactory, RemoteReferenceImpl<T> refer)
    {
        Assert.assertNotNull(refer);
        refer.setRemoteService(this);
        this.refer = refer;
        this.address = PropertiesUtils.toAddress(refer.getServiceProperties());
        this.clientFactory = clientFactory;
    }

    @Override
    public ServiceProperties getServiceProperties() {
        return refer.getServiceProperties();
    }
    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void destroy() {
        //remoteservice 清空
        refer.setRemoteService(null);
        refer.destroy();
        if(client!=null){
            client.destroy();
        }
    }

    @Override
    public boolean isAvailable() {
        return client != null;
    }

    protected Class<?>[] getImplementingClasses() {
        Class<?> cls = getServiceClass();
        return new Class[] { cls, Closeable.class, Client.class };
    }

    @Override
    public Object[] invoke(ClientCallback callback, RemoteRequest request, boolean oneway) throws RemoteException {
        Method method = findMethod(request);
        Client client = slectedClient(request);
        OperationInfo oi = getOperationInfo(client, getServiceClass(), method);
        if (oi == null) {
            throw new RemoteException("Can't found OperationInfo for method :" + method.getName());
        }
        try {
            if (callback == null) {
                if (oneway) {
                    oi.setOutput(null, null);
                }
                return client.invoke(oi, request.getParameters(), request.getRequestContext());
            } else {
                client.invoke(callback, oi, request.getParameters(), request.getRequestContext());
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException(e);
        }
        return null;
    }

    public synchronized Client slectedClient(RemoteRequest request) {
        if (client == null) {
            lock.lock();
            try {
                Client cli = doGetClient(request);
                this.client = cli;
            } finally {
                lock.unlock();
            }
        }
        return client;
    }

    @Override
    public Class<T> getServiceClass() {
        return refer.getServiceClass();
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    protected RemoteReferenceImpl<T> getRemoteReference() {
        return refer;
    }

    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    protected abstract Client doGetClient(RemoteRequest request);

    protected Dictionary<String, ?> toDictionary(RemoteReference<?> reference) {
        Hashtable<String, Object> copyed = new Hashtable<String, Object>();
        for (String key : reference.getPropertyKeys()) {
            copyed.put(key, reference.getProperty(key));
        }
        return copyed;
    }
}
