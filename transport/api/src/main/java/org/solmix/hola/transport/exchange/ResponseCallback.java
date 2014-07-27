package org.solmix.hola.transport.exchange;

/**
 * Callback
 * 
 * @author william.liangf
 */
public interface ResponseCallback {

    /**
     * done.
     * 
     * @param response
     */
    void done(Object response);

    /**
     * caught exception.
     * 
     * @param exception
     */
    void caught(Throwable exception);

}