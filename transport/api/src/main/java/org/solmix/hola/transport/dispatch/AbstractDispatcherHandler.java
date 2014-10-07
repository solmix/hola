package org.solmix.hola.transport.dispatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.executor.ExecutorProvider;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.handler.ChannelHandlerDelegate;
import org.solmix.runtime.Container;

public class AbstractDispatcherHandler implements ChannelHandlerDelegate {
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractDispatcherHandler.class);

    protected static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory("DubboSharedHandler", true));
    
    protected  ExecutorService executor;
    
    protected  ChannelHandler handler;

    protected final RemoteInfo info;
    
    private final Container container;
    
    public AbstractDispatcherHandler(ChannelHandler handler, RemoteInfo info,Container container) {
        this.handler = handler;
        this.info = info;
        this.container=container;
        String eprovider=info.getThreadPool(HolaConstants.DEFAULT_THREADPOOL);
        executor= (ExecutorService) container.getExtensionLoader(ExecutorProvider.class).getExtension(eprovider).getExecutor(info);
        //XXX 使用object缓存技术
    }
    
    public void close() {
        try {
            if (executor instanceof ExecutorService) {
                executor.shutdown();
            }
        } catch (Throwable t) {
            logger.warn("fail to destroy thread pool of server: " + t.getMessage(), t);
        }
    }

    @Override
    public void connected(Channel channel) throws TransportException {
        handler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws TransportException {
        handler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws TransportException {
        handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws TransportException {
        handler.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws TransportException {
        handler.caught(channel, exception);
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }
    
    @Override
    public ChannelHandler getHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getHandler();
        } else {
            return handler;
        }
    }
    
    protected Container getContainer(){
        Assert.isNotNull(container);
        return container;
    }
   
}