package org.solmix.hola.transport.handler;

import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;


/**
 * ChannelHandlerAdapter.
 * 
 * @author qian.lei
 */
public class ChannelHandlerAdapter implements ChannelHandler {

    @Override
    public void connected(Channel channel) throws TransportException {
    }

    @Override
    public void disconnected(Channel channel) throws TransportException {
    }

    @Override
    public void sent(Channel channel, Object message) throws TransportException {
    }

    @Override
    public void received(Channel channel, Object message) throws TransportException {
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws TransportException {
    }

}