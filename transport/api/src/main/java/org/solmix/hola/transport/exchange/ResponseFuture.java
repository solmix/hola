package org.solmix.hola.transport.exchange;

import org.solmix.hola.transport.TransportException;


public interface ResponseFuture {

    /**
     * get result.
     * 
     * @return result.
     */
    Object get() throws TransportException;

    /**
     * get result with the specified timeout.
     * 
     * @param timeoutInMillis timeout.
     * @return result.
     */
    Object get(int timeoutInMillis) throws TransportException;

    /**
     * set callback.
     * 
     * @param callback
     */
    void setCallback(ResponseCallback callback);

    /**
     * check is done.
     * 
     * @return done or not.
     */
    boolean isDone();

}