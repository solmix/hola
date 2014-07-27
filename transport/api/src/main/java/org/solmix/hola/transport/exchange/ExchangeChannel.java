package org.solmix.hola.transport.exchange;

import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;


public interface ExchangeChannel extends Channel {

    /**
     * send request.
     * 
     * @param request
     * @return response future
     * @throws TransportException
     */
    ResponseFuture request(Object request) throws TransportException;

    /**
     * send request.
     * 
     * @param request
     * @param timeout
     * @return response future
     * @throws TransportException
     */
    ResponseFuture request(Object request, int timeout) throws TransportException;

    /**
     * get message handler.
     * 
     * @return message handler
     */
    ExchangeHandler getExchangeHandler();

    /**
     * graceful close.
     * 
     * @param timeout
     */
    void close(int timeout);

}