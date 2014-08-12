package org.solmix.hola.transport.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.core.model.EndpointInfo;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.channel.ChannelHandler;

public class WrappedChannelHandler implements ChannelHandlerDelegate {
    
    protected static final Logger logger = LoggerFactory.getLogger(WrappedChannelHandler.class);

    protected static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory("DubboSharedHandler", true));
    
    protected  ExecutorService executor;
    
    protected  ChannelHandler handler;

    protected final EndpointInfo param;
    
    public WrappedChannelHandler(ChannelHandler handler, EndpointInfo param) {
        this.handler = handler;
        this.param = param;
        //TODO used extensionpoint.
//        executor = (ExecutorService) ExtensionLoader.getExtensionLoader(ThreadPool.class).getAdaptiveExtension().getExecutor(url);
//
//        String componentKey = Constants.EXECUTOR_SERVICE_COMPONENT_KEY;
//        if (Constants.CONSUMER_SIDE.equalsIgnoreCase(url.getParameter(Constants.SIDE_KEY))) {
//            componentKey = Constants.CONSUMER_SIDE;
//        }
//        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
//        
//        dataStore.put(componentKey, Integer.toString(url.getPort()), executor);
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
    
   
}