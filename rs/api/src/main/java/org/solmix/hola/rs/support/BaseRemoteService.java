
package org.solmix.hola.rs.support;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.Reflection;
import org.solmix.exchange.Client;
import org.solmix.exchange.ClientCallback;
import org.solmix.exchange.event.ServiceFactoryEvent;
import org.solmix.exchange.invoker.OperationDispatcher;
import org.solmix.exchange.model.OperationInfo;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.ClientFactory;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteProxy;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.DefaultRemoteResponse;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteRequestListener;
import org.solmix.hola.rs.call.RemoteResponse;
import org.solmix.hola.rs.call.RemoteResponseFuture;
import org.solmix.runtime.helper.ProxyHelper;

public abstract class BaseRemoteService<T> implements RemoteService<T>
{

    private RemoteReferenceImpl<T> refer;

    private Client client;
    private String address;

    protected ClientFactory clientFactory;

    public BaseRemoteService()
    {

    }

    public BaseRemoteService(ClientFactory clientFactory, RemoteReferenceImpl<T> refer)
    {
        Assert.assertNotNull(refer);
        this.refer = refer;
        this.address=PropertiesUtils.toAddress(refer.properties);
        this.clientFactory = clientFactory;
    }
    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public RemoteResponse sync(RemoteRequest call) throws RemoteException {
        Method method = findMethod(call);
        Client client = getClient();
        OperationInfo oi = getOperationInfo(client, getServiceClass(), method);
        if (oi == null) {
            throw new RemoteException("Can't found OperationInfo for method :" + method.getName());
        }
        DefaultRemoteResponse res = new DefaultRemoteResponse();
        try {
            Object[] obj = client.invoke(oi, call.getParameters(), call.getRequestContext());
            if (obj != null && obj.length > 0) {
                res.setValue(obj[0]);
            } else if (obj.length > 1) {
                res.setException(new IllegalArgumentException("sync return multi return values"));
            }
        } catch (Exception e) {
            if (e instanceof RemoteException) {
                throw (RemoteException) e;
            }
            res.setException(e);
        }
        return res;
    }

    protected Method findMethod(RemoteRequest call) {
        Method method = Reflection.findMethod(getServiceClass(), call.getMethod(), call.getParameterTypes());
        if (method == null) {
            throw new RemoteException(
                "Method :" + call.getMethod() + "(" + call.getParameterTypes() + ") is not found in class :" + getServiceClass().getName());
        }
        return method;
    }

    @Override
    public void async(RemoteRequest call, final RemoteRequestListener listener) {
        Method method = findMethod(call);
        Client client = getClient();
        OperationInfo oi = getOperationInfo(client, getServiceClass(), method);
        if (oi == null) {
            throw new RemoteException("Can't found OperationInfo for method :" + method.getName());
        }
        try {
            client.invoke(new ClientCallback() {

                @Override
                public void handleResponse(Map<String, Object> ctx, Object[] res) {
                    super.handleResponse(ctx, res);
                    if (res != null && res.length > 0) {
                        listener.handleResponse(res[0], ctx);
                    } else if (res.length > 1) {
                        listener.handleException(new IllegalArgumentException("sync return multi return values"), ctx);
                    }
                }

                @Override
                public void handleException(Map<String, Object> ctx, Throwable ex) {
                    super.handleException(ctx, ex);
                    listener.handleException(ex, ctx);
                }
            }, oi, call.getParameters(), call.getRequestContext());
        } catch (Exception e) {
            throw new RemoteException("Failed to async call remoteService");
        }
    }

    protected OperationInfo getOperationInfo(Client client, Class<T> serviceClass, Method method) {
        OperationDispatcher od = (OperationDispatcher) client.getEndpoint().getService().get(OperationDispatcher.class.getName());
        if (od != null) {
            return od.getOperation(method);
        } else {
            return null;
        }
    }

    @Override
    public Future<RemoteResponse> async(RemoteRequest call) {
        Method method = findMethod(call);
        Client client = getClient();
        OperationInfo oi = getOperationInfo(client, getServiceClass(), method);
        if (oi == null) {
            throw new RemoteException("Can't found OperationInfo for method :" + method.getName());
        }
        ClientCallback callback = new ClientCallback();
        RemoteResponseFuture future = new RemoteResponseFuture(callback);
        try {
            client.invoke(callback, oi, call.getParameters(), call.getRequestContext());
        } catch (Exception e) {
            throw new RemoteException("Failed to async call remoteService");
        }
        return future;
    }

    @Override
    public void fireAsync(RemoteRequest call) throws RemoteException {
        Method method = findMethod(call);
        Client client = getClient();
        OperationInfo oi = getOperationInfo(client, getServiceClass(), method);
        if (oi == null) {
            throw new RemoteException("Can't found OperationInfo for method :" + method.getName());
        }
        // 设置方法的输出为空
        oi.setOutput(null, null);
        try {
            client.invoke(oi, call.getParameters(), call.getRequestContext());
        } catch (Exception e) {
            throw new RemoteException("Failed to call fireAsync", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getProxy() throws RemoteException {

        return (T) doGetProxy(getImplementingClasses());
    }

    @Override
    public Class<T> getServiceClass() {
        return refer.getServiceClass();
    }

    @Override
    public Object getProperty(String key) {
        return refer.getProperty(key);
    }

    @Override
    public String[] getPropertyKeys() {
        return refer.getPropertyKeys();
    }

    @Override
    public void destroy() {
        refer.destroy();
        client = null;
    }
    @Override
    public boolean isAvailable() {
        return client != null;
    }

    protected Class<?>[] getImplementingClasses() {
        Class<?> cls = getServiceClass();
        return new Class[] { cls, Closeable.class, Client.class };
    }

    protected Object doGetProxy(Class<?>[] interfaceClasses) {
        Client client = getClient();
        RemoteProxy proxyHandler = createRemoteProxy(client);
        Class<?> classes[] = getImplementingClasses();
        Object object = ProxyHelper.getProxy(getServiceClass().getClassLoader(), classes, proxyHandler);
        clientFactory.getServiceFactory().pulishEvent(ServiceFactoryEvent.PROXY_CREATED, classes, proxyHandler, object);
        return object;
    }

    protected RemoteProxy createRemoteProxy(Client client) {
        return new RemoteProxy(client);
    }

    public synchronized Client getClient() {
        Client cli = client;
        if (cli == null) {
            cli = refer.getClient();
            if (cli == null) {
                Assert.assertNotNull(clientFactory);
                cli = doGetClient();
                refer.setClient(cli);
            }
        }
        this.client = cli;
        return client;
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

    protected abstract Client doGetClient();

    protected Dictionary<String, ?> toDictionary(RemoteReference<?> reference) {
        Hashtable<String, Object> copyed = new Hashtable<String, Object>();
        for (String key : reference.getPropertyKeys()) {
            copyed.put(key, reference.getProperty(key));
        }
        return copyed;
    }
}
