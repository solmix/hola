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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.ClientCallback;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.Processor;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorChain;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.AbstractPipeline;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.transport.codec.Codec;
import org.solmix.runtime.Container;
import org.solmix.runtime.io.AbstractWrappedOutputStream;
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

    private final Container container;

    private final Bootstrap bootstrap;

    protected URI defaultEndpointURI;

    protected String defaultEndpointURIString;
    
    private NettyConfiguration clientInfo;

    /**
     * @param address
     */
    public NettyPipeline(Container container, EndpointInfo ei) {
        super(ei.getAddress());
        this.endpointInfo = ei;
        this.container = container;
        bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = container.getExtension(EventLoopGroup.class);
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        clientInfo=ei.getExtension(NettyConfiguration.class);
    }

    @Override
    public void prepare(Message message) throws IOException {
        URI currentURI;
        try {
            currentURI = setupURI(message);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        setupConnection(currentURI, message, clientInfo);

        message.setContent(OutputStream.class,
            createOutputStream(message, clientInfo, currentURI));

    }

    protected OutputStream createOutputStream(Message message,
        NettyConfiguration config, URI url) {
        NettyWrappedOutputStream out = new NettyWrappedOutputStream(message,
            config, url);
        return out;
    }

    private URI setupURI(Message message) throws URISyntaxException {
        String result = (String) message.get(Message.ENDPOINT_ADDRESS);
        String pathInfo = (String) message.get(Message.PATH_INFO);
        String queryString = (String) message.get(Message.QUERY_STRING);
        if (result == null) {
            if (pathInfo == null && queryString == null) {
                URI uri = getURI();
                message.put(Message.ENDPOINT_ADDRESS, defaultEndpointURIString);
                return uri;
            }
            result = getURI().toString();
            message.put(Message.ENDPOINT_ADDRESS, result);
        }

        // REVISIT: is this really correct?
        if (null != pathInfo && !result.endsWith(pathInfo)) {
            result = result + pathInfo;
        }
        if (queryString != null) {
            result = result + "?" + queryString;
        }
        return new URI(result);
    }

    protected URI getURI() throws URISyntaxException {
        return getURI(true);
    }

    protected synchronized URI getURI(boolean createOnDemand)
        throws URISyntaxException {
        if (defaultEndpointURI == null && createOnDemand) {

            if (endpointInfo.getAddress() == null) {
                throw new URISyntaxException("<null>",
                    "Invalid address. Endpoint address cannot be null.", 0);
            }
            defaultEndpointURI = new URI(endpointInfo.getAddress());
            defaultEndpointURIString = defaultEndpointURI.toString();
        }
        return defaultEndpointURI;
    }

    protected void setupConnection(URI currentURI, Message message,
        NettyConfiguration config) {

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
    
    public class NettyWrappedOutputStream extends AbstractWrappedOutputStream {

        private final Message outMessage;

        private final URI url;

        private final NettyConfiguration config;

        ByteBuf outBuffer;
        ByteBuf inBuffer;

        OutputStream outputStream;

        volatile Channel channel;

        volatile Throwable exception;
        boolean isAsync;
        /**
         * @param message
         * @param config
         */
        public NettyWrappedOutputStream(Message message,NettyConfiguration config, URI url) {
            this.outMessage = message;
            this.url = url;
            this.config = config;
            outBuffer = Unpooled.buffer(config.getBufferSize());
            outputStream = new ByteBufOutputStream(outBuffer);
            message.setContent(ByteBuf.class, outBuffer);
        }

        public ByteBuf getOutBuffer() {
            return outBuffer;
        }

        @Override
        protected void onFirstWrite() throws IOException {
            handleRequest();
        }

        @Override
        public void close() throws IOException {
            try {
                boolean exceptionSet = outMessage.getContent(Exception.class) != null;
                if (!written && !exceptionSet) {
                    handleRequest();
                }
                super.close();
                handleResponse();
            } catch (IOException e) {
                String origMessage = e.getMessage();
                if (origMessage != null && origMessage.contains(url.toString())) {
                    throw e;
                }
                throw mapException(e.getClass().getSimpleName() + " invoking "
                    + url + ": " + e.getMessage(), e, IOException.class);
            } catch (RuntimeException e) {
                throw mapException(e.getClass().getSimpleName() + " invoking "
                    + url + ": " + e.getMessage(), e, RuntimeException.class);
            }
        }

        private <T extends Exception> T mapException(String msg, T ex,
            Class<T> cls) {
            T ex2 = ex;
            try {
                ex2 = cls.cast(ex.getClass().getConstructor(String.class).newInstance(msg));
                ex2.initCause(ex);
            } catch (Throwable e) {
                ex2 = ex;
            }

            return ex2;
        }
        
        protected void handleRequest() throws IOException {
            connect();
            wrappedStream= new OutputStream() {
                @Override
                public void write(byte b[], int off, int len) throws IOException {
                    outputStream.write(b, off, len);
                }
                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }
                @Override
                public void close() throws IOException {
                    // Setup the call back for sending the message
                    ChannelFutureListener listener = new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                setException(future.cause());
                            }
                        }
                    };
                    ChannelFuture channelFuture = getChannel().write(outMessage);
                    channelFuture.addListener(listener);
                    outputStream.close();
                }
            };
        }
        
        protected synchronized Channel getChannel() throws IOException {
            while (channel == null) {
                if (exception == null) { //already have an exception, skip waiting
                    try {
                        // connection timeout
                        wait(config.getConnectionTimeout());
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                }
                if (channel == null) {

                    if (exception != null) {
                        if (exception instanceof IOException) {
                            throw (IOException)exception;
                        }
                        if (exception instanceof RuntimeException) {
                            throw (RuntimeException)exception;
                        }
                        throw new IOException(exception);
                    }

                    throw new SocketTimeoutException("Connection Timeout");
                }
            }
            return channel;

        }
        
        protected Codec getCodec(String codec){
          return  container.getExtensionLoader(Codec.class).getExtension(codec);
        }

        protected void connect() {
            NettyConfiguration tci = endpointInfo.getExtension(NettyConfiguration.class);
            String codec =tci.getCodec();
            int bufferSize = tci.getBufferSize();
            bootstrap.handler(new NettyClientChannelFactory(getCodec(codec),bufferSize));
            ChannelFuture connFuture = bootstrap.connect(new InetSocketAddress(
                url.getHost(), url.getPort() != -1 ? url.getPort() : 1314));
            connFuture.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future)
                    throws Exception {
                    if (future.isSuccess()) {
                        setChannel(future.channel());
                    } else {
                        setException(future.cause());
                    }

                }
            });
            ResponseCallBack callBack = new ResponseCallBack() {
                @Override
                public void responseReceived(ByteBuf response) {
                    setInBuffer(response);
                }
            };
            outMessage.put(ResponseCallBack.class, callBack);
        }

        protected synchronized void setException(Throwable ex) {
            exception = ex;
            if (isAsync) {
                // got a response, need to start the response processing now
                try {
                    handleResponseInThreadpool(false, true);
                    isAsync = false; // don't trigger another start on next
                                     // block. :-)
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
            notifyAll();
        }

        protected void handleResponseInThreadpool(boolean allowCurrentThead, boolean forePool) throws IOException {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try{
                        handleResponseSync();
                    }catch (Throwable e) {
                        PhaseInterceptorChain pic =   ((PhaseInterceptorChain)outMessage.getInterceptorChain());
                        pic.abort();
                        outMessage.setContent(Exception.class, e);
                        pic.handleException(outMessage);
                        Processor p = pic.getFaultProcessor();
                        if(p==null){
                            p = outMessage.getExchange().get(Processor.class);
                        }
                        p.process(outMessage);
                    }
                }
            };
            boolean exceptionSet = outMessage.getContent(Exception.class) != null;
            if(!exceptionSet){
                try{
                Executor ex = outMessage.getExchange().get(Executor.class);
                if(forePool&&ex!=null){
                    final Executor ex2 = ex;
                    final Runnable origRunnable = runnable;
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            outMessage.getExchange().put(Executor.class.getName() 
                                                         + ".EXECUTOR_USED", Boolean.TRUE);
                            ex2.execute(origRunnable);
                        }
                    };
                }
                if(forePool || ex == null){
                    ThreadPoolManager tpm =  outMessage.getExchange().get(Container.class).getExtension(ThreadPoolManager.class);
                    ThreadPool tp= tpm.getThreadPool("netty-pipeline");
                    if(tp==null){
                        tp = tpm.getDefaultThreadPool();
                    }
                    long timeout = 1000;
                    if(clientInfo!=null){
                        timeout=clientInfo.getAsyncExecuteTimeout();
                    }
                    if(timeout>0){
                        tp.execute(runnable, timeout);
                    }else{
                        tp.execute(runnable);
                    }
                }else{
                    outMessage.getExchange().put(Executor.class.getName() 
                        + ".EXECUTOR_USED", Boolean.TRUE);
                    ex.execute(runnable);
                }
            }catch (RejectedExecutionException rex) {
                if (allowCurrentThead) {
                    throw rex;
                }
                LOG.trace("EXECUTOR_FULL");
                handleResponseSync();
            }
        }
            
        }

        protected synchronized void setChannel(Channel ch) {
            channel = ch;
            notifyAll();
        }
        
        /**
         * outputStream关闭的时候调用该方法,准备返回消息.
         */
        protected void handleResponse() throws IOException {
            if (outMessage == null || outMessage.getExchange() == null
                || outMessage.getExchange().isSync()) {
                handleResponseSync();
            } else {
                handleResponseAsync();
            }

        }
        /**
         * @throws IOException 
         * 
         */
        protected void handleResponseSync() throws IOException {
            Exchange exchange = outMessage.getExchange();
            Message inMessage = new DefaultMessage();
            inMessage.setExchange(exchange);
            InputStream in = null;
            if(!isOneWay(outMessage)){
                in = getInputStream();
                if(in==null){
                    ClientCallback cc = exchange.get(ClientCallback.class);
                    if(cc!=null){
                        closeInputStream();
                    }
                }
            }else{
                outMessage.removeContent(OutputStream.class);
                outMessage.removeContent(ByteBuf.class);
            }
            if(in==null){
                in = new ByteArrayInputStream(new byte[] {});
            }
            inMessage.setContent(InputStream.class, in);
            getProcessor().process(inMessage);
        }

        private void closeInputStream() throws IOException {
           getInputBuffer().clear();
        }

        private InputStream getInputStream() throws IOException {
               return new ByteBufInputStream(getInputBuffer());
        }

        protected ByteBuf getInputBuffer() throws IOException {
            while (inBuffer == null) {
                if (exception == null) { //already have an exception, skip waiting
                    try {
                        wait(config.getReceiveTimeout());
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                }
                if (inBuffer == null) {

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
            return inBuffer;
        }
        
        protected synchronized void setInBuffer(ByteBuf in){
                inBuffer = in;
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

        private boolean isOneWay(Message msg) {
            Exchange ex = msg.getExchange();
            return ex!=null&&ex.isOneWay();
        }

        protected void handleResponseAsync() {
            isAsync=true;
        }
    }

   
}
