
package org.solmix.hola.rs.call;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.solmix.exchange.ClientCallback;

public class RemoteResponseFuture implements Future<RemoteResponse>
{

    private ClientCallback callback;
   
    public RemoteResponseFuture(ClientCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return callback.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return callback.isCancelled();
    }

    @Override
    public boolean isDone() {
        return callback.isDone();
    }

    @Override
    public RemoteResponse get() throws InterruptedException, ExecutionException {
        Object[] objs = callback.get();
        DefaultRemoteResponse res = new DefaultRemoteResponse();
        if (objs != null && objs.length > 0) {
            res.setValue(objs[0]);
        } else if (objs.length > 1) {
            res.setException(new IllegalArgumentException("sync return multi return values"));
        }
        res.setException(callback.getException());
        return res;
    }

    @Override
    public RemoteResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Object[] objs = callback.get(timeout,unit);
        DefaultRemoteResponse res = new DefaultRemoteResponse();
        if (objs != null && objs.length > 0) {
            res.setValue(objs[0]);
        } else if (objs.length > 1) {
            res.setException(new IllegalArgumentException("sync return multi return values"));
        }
        res.setException(callback.getException());
        return res;
    }

}
