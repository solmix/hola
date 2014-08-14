package org.solmix.hola.transport.protocol;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.ChannelInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.exchange.DefaultFuture;
import org.solmix.hola.transport.exchange.ExchangeChannel;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.Request;
import org.solmix.hola.transport.exchange.Response;
import org.solmix.hola.transport.exchange.ResponseFuture;


final class ProtocolExchangeChannel implements ExchangeChannel {

    private static final Logger logger      = LoggerFactory.getLogger(ProtocolExchangeChannel.class);

    private static final String CHANNEL_KEY = ProtocolExchangeChannel.class.getName() + ".CHANNEL";

    private final Channel       channel;

    private volatile boolean    closed      = false;

    ProtocolExchangeChannel(Channel channel){
        if (channel == null) {
            throw new IllegalArgumentException("channel == null");
        }
        this.channel = channel;
    }

    static ProtocolExchangeChannel getOrAddChannel(Channel ch) {
        if (ch == null) {
            return null;
        }
        ProtocolExchangeChannel ret = (ProtocolExchangeChannel) ch.getAttribute(CHANNEL_KEY);
        if (ret == null) {
            ret = new ProtocolExchangeChannel(ch);
            if (ch.isConnected()) {
                ch.setAttribute(CHANNEL_KEY, ret);
            }
        }
        return ret;
    }
    
    static void removeChannelIfDisconnected(Channel ch) {
        if (ch != null && ! ch.isConnected()) {
            ch.removeAttribute(CHANNEL_KEY);
        }
    }
    
    @Override
    public void send(Object message) throws TransportException {
        send(message, getInfo().getAwait(false));
    }
    
    @Override
    public void send(Object message, boolean sent) throws TransportException {
        if (closed) {
            throw new TransportException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The channel " + this + " is closed!");
        }
        if (message instanceof Request
                || message instanceof Response
                || message instanceof String) {
            channel.send(message, sent);
        } else {
            Request request = new Request();
            request.setVersion("2.0.0");
            request.setTwoWay(false);
            request.setData(message);
            channel.send(request, sent);
        }
    }

    @Override
    public ResponseFuture request(Object request) throws TransportException {
        return request(request, channel.getInfo().getTimeout( HolaConstants.DEFAULT_TIMEOUT));
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws TransportException {
        if (closed) {
            throw new TransportException(this.getLocalAddress(), null, "Failed to send request " + request + ", cause: The channel " + this + " is closed!");
        }
        // create request.
        Request req = new Request();
        req.setVersion("2.0.0");
        req.setTwoWay(true);
        req.setData(request);
        DefaultFuture future = new DefaultFuture(channel, req, timeout);
        try{
            channel.send(req);
        }catch (TransportException e) {
            future.cancel();
            throw e;
        }
        return future;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    // graceful close
    @Override
    public void close(int timeout) {
        if (closed) {
            return;
        }
        closed = true;
        if (timeout > 0) {
            long start = System.currentTimeMillis();
            while (DefaultFuture.hasFuture(ProtocolExchangeChannel.this) 
                    && System.currentTimeMillis() - start < timeout) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        close();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return channel.getLocalAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }
    @Override
    public ChannelInfo getInfo() {
        return channel.getInfo();
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channel.getChannelHandler();
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return (ExchangeHandler) channel.getChannelHandler();
    }
    
    @Override
    public Object getAttribute(String key) {
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        channel.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        channel.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        return channel.hasAttribute(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ProtocolExchangeChannel other = (ProtocolExchangeChannel) obj;
        if (channel == null) {
            if (other.channel != null) return false;
        } else if (!channel.equals(other.channel)) return false;
        return true;
    }

    @Override
    public String toString() {
        return channel.toString();
    }

}