/**
 * Copyright (c) 2015 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.hola.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.Processor;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorChain;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.AbstractPipeline;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.io.MessageWrappedOutputStream;
import org.solmix.hola.transport.RemoteProtocol;
import org.solmix.hola.transport.ResponseCallback;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.support.RemoteResponses;
import org.solmix.runtime.Container;
import org.solmix.runtime.threadpool.ThreadPool;
import org.solmix.runtime.threadpool.ThreadPoolManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */

public class NettyPipeline extends AbstractPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(NettyPipeline.class);

    private final EndpointInfo endpointInfo;


    private final Bootstrap bootstrap;

    protected URI defaultEndpointURI;

    /**缓存Address String*/
    protected String defaultEndpointURIString;
    
    private NettyConfiguration clientInfo;
    
    volatile Channel channel;
    
    /**
     * @param address
     */
    public NettyPipeline(Container container, EndpointInfo ei) {
        super(ei.getAddress());
        this.clientInfo=ei.getExtension(NettyConfiguration.class);
        this.endpointInfo = ei;
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientInfo.getConnectTimeout());
        EventLoopGroup eventLoopGroup = container.getExtension(EventLoopGroup.class);
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
       
       
    }

    @Override
    public void prepare(Message message) throws IOException {
        if(!isConnected()){
            try {
                URI uri = getEndpointAddressURI();
                connect(uri);
            } catch (URISyntaxException e) {
               throw new TransportException(e);
            }
        }
        URI currentURI;
        try {
            currentURI = setupURI(message);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        message.setContent(OutputStream.class, createOutputStream(message, clientInfo, currentURI));

    }

    protected OutputStream createOutputStream(Message message, NettyConfiguration config, URI url) throws IOException {
        return new NettyWrappedOutputStream(message);
    }
    
    protected void connect(URI url) {
        RemoteProtocol protocol = (RemoteProtocol)getProtocol();
        bootstrap.handler(new NettyClientChannelFactory(clientInfo,protocol));
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort()));
        try{
            boolean ret = future.awaitUninterruptibly(clientInfo.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if(ret&&future.isSuccess()){
                Channel newChannel = future.channel();
                try {
                    Channel old = NettyPipeline.this.channel;
                    if(old!=null){
                        if(LOG.isDebugEnabled()){
                            LOG.debug("Closing old netty channel {} on created new netty channel {}",old,newChannel);
                        }
                        old.close();
                    }
                }finally{
                    NettyPipeline.this.channel=newChannel;
                }
            }else if (future.cause() != null) {
                throw new TransportException(" failed connect to server"+url,future.cause());
            }else{
                throw new TransportException(" failed connect to server"+url+" client timeout ("+clientInfo.getConnectTimeout()+")ms",future.cause());
            }
            
        }finally{
            if (! isConnected()) {
                future.cancel(true);
            }
        }
        
    }
   
    private boolean isConnected()  {
        Channel channel = getChannel();
        if (channel == null)
            return false;
        return channel.isOpen()||channel.isActive();
    }

    /***/
    protected synchronized void setChannel(Channel ch) {
        channel = ch;
        notifyAll();
    }

    protected Channel getChannel() {
        return channel;
    }
    
    public class NettyWrappedOutputStream extends MessageWrappedOutputStream {

        Throwable exception;
        boolean isAsync;
        Message inMessage;
        public NettyWrappedOutputStream(Message message)
        {
            super(message);
        }
       
        @Override
        public void send() throws IOException {
            try {
                boolean exceptionSet = outMessage.getContent(Exception.class) != null;
                if (!exceptionSet) {
                    ResponseCallback callBack = new ResponseCallback() {
                        @Override
                        public void process(Message response) {
                            setInMessage(response);
                        }
                    };
                    outMessage.getExchange().put(ResponseCallback.class, callBack);
                    outMessage.put(HOLA.TIMEOUT_KEY, clientInfo.getTimeout());
                    RemoteResponses.bind(NettyPipeline.this, outMessage);
                    
                    ChannelFutureListener listener = new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {

                            if (!future.isSuccess()) {
                                setException(future.cause());
                            }
                        }
                    };
                    ChannelFuture channelFuture = getChannel().writeAndFlush(outMessage);
                    channelFuture.addListener(listener);
                }
                handleResponse(outMessage);
            } catch (IOException e) {
                String origMessage = e.getMessage();
                if (origMessage != null && origMessage.contains(defaultEndpointURIString)) {
                    throw e;
                }
                throw mapException(e.getClass().getSimpleName() + " invoking "
                    + defaultEndpointURIString + ": " + e.getMessage(), e, IOException.class);
            } catch (RuntimeException e) {
                throw mapException(e.getClass().getSimpleName() + " invoking "
                    + defaultEndpointURIString , e, RuntimeException.class);
            }
            
        }
        private <T extends Exception> T mapException(String msg, T ex, Class<T> cls) {
            T ex2 = ex;
            try {
                ex2 = cls.cast(ex.getClass().getConstructor(String.class).newInstance(msg));
                ex2.initCause(ex);
            } catch (Throwable e) {
                ex2 = ex;
            }
            return ex2;
        }
        private synchronized void setInMessage(Message message){
            this.inMessage=message;
            RemoteResponses.unbind(message);
            if (isAsync) {
                //got a response, need to start the response processing now
                try {
                    handleResponseInThreadpool(false, true);
                    isAsync = false; // don't trigger another start on next block. :-)
                } catch (Exception ex) {
                    //ignore, we'll try again on the next consume;
                }
            }
            notifyAll();
        }
        
        public synchronized Message getInMessage() throws IOException {
            while (inMessage == null) {
                if (exception == null) { //already have an exception, skip waiting
                    try {
                        wait(clientInfo.getTimeout());
                    } catch (InterruptedException e) {
                        RemoteResponses.unbind(outMessage);
                        throw new IOException(e);
                    }
                }
                if (inMessage == null) {

                    if (exception != null) {
                        if (exception instanceof IOException) {
                            throw (IOException)exception;
                        }
                        if (exception instanceof RuntimeException) {
                            throw (RuntimeException)exception;
                        }
                        throw new IOException(exception);
                    }

                    throw new SocketTimeoutException("Read Timeout");
                }
            }
            return inMessage;
        }

        /**
         * outputStream关闭的时候调用该方法,准备返回消息.
         */
        protected void handleResponse(Message message) throws IOException {
            if (message == null || message.getExchange() == null || message.getExchange().isSync()) {
                handleResponseSync();
            } else {
                handleResponseAsync();
            }

        }
        
        protected void handleResponseAsync() {
            isAsync=true;
        }
        protected synchronized void setException(Throwable ex) {
            exception = ex;
            RemoteResponses.unbind(outMessage);
            if (isAsync) {
                // got a response, need to start the response processing now
                try {
                    handleResponseInThreadpool(false, true);
                    isAsync = false; 
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
            notifyAll();
        }
        
        protected void handleResponseSync() throws IOException {
            Exchange exchange = outMessage.getExchange();
            Message inMessage=null;
            try {
                inMessage = getInMessage();
            } catch (IOException e) {
                RemoteResponses.unbind(outMessage);
                throw e;
            }catch (RuntimeException e) {
                RemoteResponses.unbind(outMessage);
                throw e;
            }catch (Throwable e) {
                RemoteResponses.unbind(outMessage);
                throw new IOException("get response message fialed", e);
            }
            inMessage.setExchange(exchange);
            getProcessor().process(inMessage);
        }
        
        protected boolean isOneWay(Message msg) {
            Exchange ex = msg.getExchange();
            return ex != null && ex.isOneWay();
        }
        
        protected void handleResponseInThreadpool(boolean allowCurrentThead, boolean forePool) throws IOException {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        handleResponseSync();
                    } catch (Throwable e) {
                        PhaseInterceptorChain pic = ((PhaseInterceptorChain) outMessage.getInterceptorChain());
                        pic.abort();
                        outMessage.setContent(Exception.class, e);
                        pic.handleException(outMessage);
                        Processor p = pic.getFaultProcessor();
                        if (p == null) {
                            p = outMessage.getExchange().get(Processor.class);
                        }
                        p.process(outMessage);
                    }
                }
            };
            boolean exceptionSet = outMessage.getContent(Exception.class) != null;
            if (!exceptionSet) {
                try {
                    Executor ex = outMessage.getExchange().get(Executor.class);
                    if (forePool && ex != null) {
                        final Executor ex2 = ex;
                        final Runnable origRunnable = runnable;
                        runnable = new Runnable() {

                            @Override
                            public void run() {
                                outMessage.getExchange().put(Executor.class.getName() + ".EXECUTOR_USED", Boolean.TRUE);
                                ex2.execute(origRunnable);
                            }
                        };
                    }
                    if (forePool || ex == null) {
                        ThreadPoolManager tpm = outMessage.getExchange().get(Container.class).getExtension(ThreadPoolManager.class);
                        ThreadPool tp = tpm.getThreadPool("netty-pipeline");
                        if (tp == null) {
                            tp = tpm.getDefaultThreadPool();
                        }
                        long timeout = 1000;
                        if (clientInfo != null) {
                            timeout = clientInfo.getAsyncExecuteTimeout();
                        }
                        if (timeout > 0) {
                            tp.execute(runnable, timeout);
                        } else {
                            tp.execute(runnable);
                        }
                    } else {
                        outMessage.getExchange().put(Executor.class.getName() + ".EXECUTOR_USED", Boolean.TRUE);
                        ex.execute(runnable);
                    }
                } catch (RejectedExecutionException rex) {
                    if (allowCurrentThead) {
                        throw rex;
                    }
                    LOG.error("executor pool is full");
                    handleResponseSync();
                }
            }
        }
        
    }

    private URI setupURI(Message message) throws URISyntaxException {
        String result = (String) message.get(Message.ENDPOINT_ADDRESS);
        //如果没有设置，使用当前Endpoint的地址
        URI uri = getEndpointAddressURI();
        if (result == null) {
            message.put(Message.ENDPOINT_ADDRESS, defaultEndpointURIString);
        }
        return uri;
      //如果Message中已经配置，按配置生成地址
        //不支持在message中定义地址，动态更改pipeline address，同一个pipleline发送的消息地址必须是相同的
        /*else{
            
            String pathInfo = (String) message.get(Message.PATH_INFO);
            String queryString = (String) message.get(Message.QUERY_STRING);
            if (null != pathInfo && !result.endsWith(pathInfo)) {
                result = result + pathInfo;
            }
            if (queryString != null) {
                result = result + "?" + queryString;
            }
            return new URI(result);
        }*/
    }

    protected URI getEndpointAddressURI() throws URISyntaxException {
        return getEndpointAddressURI(true);
    }

    protected synchronized URI getEndpointAddressURI(boolean createOnDemand)
        throws URISyntaxException {
        if (defaultEndpointURI == null && createOnDemand) {

            if (endpointInfo.getAddress() == null) {
                throw new URISyntaxException("<null>", "Invalid address. Endpoint address cannot be null.", 0);
            }
            defaultEndpointURI = new URI(endpointInfo.getAddress());
            defaultEndpointURIString = defaultEndpointURI.toString();
        }
        return defaultEndpointURI;
    }


    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public String getBeanName() {
        if (endpointInfo.getName() != null) {
            return endpointInfo.getName().toString() + ".pipeline";
        }
        return null;
    }
}