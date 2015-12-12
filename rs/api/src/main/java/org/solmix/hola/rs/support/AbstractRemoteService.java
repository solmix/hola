
package org.solmix.hola.rs.support;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Future;

import org.solmix.commons.util.Reflection;
import org.solmix.exchange.Client;
import org.solmix.exchange.ClientCallback;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.invoker.OperationDispatcher;
import org.solmix.exchange.model.OperationInfo;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.DefaultRemoteResponse;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteRequestListener;
import org.solmix.hola.rs.call.RemoteResponse;
import org.solmix.hola.rs.call.RemoteResponseFuture;

public abstract class AbstractRemoteService<T> implements RemoteService<T>
{

    @Override
    public RemoteResponse sync(RemoteRequest request) throws RemoteException {
        DefaultRemoteResponse res = new DefaultRemoteResponse();
        try {
            Object[] obj = doInvoke(null, request);
            if (obj != null && obj.length > 0) {
                res.setValue(obj[0]);
            } else if (obj != null && obj.length > 1) {
                res.setException(new IllegalArgumentException("sync return multi return values"));
            }
        } catch (Fault e) {
            res.setException(e.getCause());
        } catch (Exception e) {
            if (e instanceof RemoteException) {
                throw (RemoteException) e;
            }
            res.setException(e);

        }
        return res;
    }

    @SuppressWarnings("deprecation")
    protected Object[] doInvoke(ClientCallback callback, RemoteRequest request) throws Exception {
        return invoke(callback, request, false);
    }

    @Override
    public void async(RemoteRequest request, final RemoteRequestListener listener) {
        try {
            doInvoke(new ClientCallback() {

                @Override
                public void handleResponse(Map<String, Object> ctx, Object[] res) {
                    super.handleResponse(ctx, res);
                    if (res != null && res.length > 0) {
                        listener.handleResponse(res[0], ctx);
                    } else if (res != null &&res.length > 1) {
                        listener.handleException(new IllegalArgumentException("sync return multi return values"), ctx);
                    }
                }

                @Override
                public void handleException(Map<String, Object> ctx, Throwable ex) {
                    super.handleException(ctx, ex);
                    listener.handleException(ex, ctx);
                }
            }, request);
        } catch (Exception e) {
            throw new RemoteException("Failed to async call remoteService");
        }
    }

    @Override
    public Future<RemoteResponse> async(RemoteRequest request) {
        ClientCallback callback = new ClientCallback();
        RemoteResponseFuture future = new RemoteResponseFuture(callback);
        try {
            doInvoke(callback, request);
        } catch (Exception e) {
            throw new RemoteException("Failed to async call remoteService");
        }
        return future;
    }

    @Override
    public void fireAsync(RemoteRequest request) throws RemoteException {
        try {
            invoke(null, request, true);
        } catch (Exception e) {
            throw new RemoteException("Failed to call fireAsync", e);
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

    protected Method findMethod(RemoteRequest call) {
        Method method = call.getMethod();
        if (method == null) {
            method = Reflection.findMethod(getServiceClass(), call.getMethodName(), call.getParameterTypes());
        }
        if (method == null) {
            throw new RemoteException(
                "Method :" + call.getMethod() + "(" + call.getParameterTypes() + ") is not found in class :" + getServiceClass().getName());
        }
        return method;
    }

}
