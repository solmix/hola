package org.solmix.hola.transport.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;


public class ChannelHandlerDispatcher implements ChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChannelHandlerDispatcher.class);

    private final Collection<ChannelHandler> channelHandlers = new CopyOnWriteArraySet<ChannelHandler>();
    
    public ChannelHandlerDispatcher() {
    }
    
    public ChannelHandlerDispatcher(ChannelHandler... handlers) {
        this(handlers == null ? null : Arrays.asList(handlers));
    }

    public ChannelHandlerDispatcher(Collection<ChannelHandler> handlers) {
        if (handlers != null && handlers.size() > 0) {
            this.channelHandlers.addAll(handlers);
        }
    }

    public Collection<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    public ChannelHandlerDispatcher addChannelHandler(ChannelHandler handler) {
        this.channelHandlers.add(handler);
        return this;
    }

    public ChannelHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
        this.channelHandlers.remove(handler);
        return this;
    }

    @Override
    public void connected(Channel channel) {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.connected(channel);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void disconnected(Channel channel) {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.disconnected(channel);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void sent(Channel channel, Object message) {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.sent(channel, message);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void received(Channel channel, Object message) {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.received(channel, message);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.caught(channel, exception);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }
    
}