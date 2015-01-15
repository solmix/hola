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
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.transport.TransportClientInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.model.EndpointInfo;
import org.solmix.runtime.exchange.support.AbstractPipeline;
import org.solmix.runtime.io.AbstractWrappedOutputStream;

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
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.Pipeline#prepare(org.solmix.runtime.exchange.Message)
     */
    @Override
    public void prepare(Message message) throws IOException {
        URI currentURI;
        try {
            currentURI = setupURI(message);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        TransportClientInfo config = getConfig(message);
        setupConnection(currentURI, message, config);

        message.setContent(OutputStream.class,
            createOutputStream(message, config, currentURI));

    }

    protected TransportClientInfo getConfig(Message message) {
        return message.get(TransportClientInfo.class);
    }

    protected OutputStream createOutputStream(Message message,
        TransportClientInfo config, URI url) {
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
        TransportClientInfo config) {

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.support.AbstractPipeline#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public String getBeanName() {
        if (endpointInfo.getName() != null) {
            return endpointInfo.getName().toString() + ".http-conduit";
        }
        return null;
    }

    /**
     * 
     * @author solmix.f@gmail.com
     * @version $Id$  2015年1月15日
     */
    public class NettyWrappedOutputStream extends AbstractWrappedOutputStream {

        private final Message outMessage;

        private final URI url;

        private final TransportClientInfo config;

        ByteBuf outBuffer;

        OutputStream outputStream;

        volatile Channel channel;

        volatile Throwable exception;
        boolean isAsync;
        /**
         * @param message
         * @param config
         */
        public NettyWrappedOutputStream(Message message,
            TransportClientInfo config, URI url) {
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
                ex2 = cls.cast(ex.getClass().getConstructor(String.class).newInstance(
                    msg));
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

        protected void connect() {
            bootstrap.handler(new NettyChannelFactory());
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
        }

        protected synchronized void setException(Throwable ex) {
            exception = ex;
            if (isAsync) {
                // got a response, need to start the response processing now
                try {
                    handleResponseOnWorkqueue(false, true);
                    isAsync = false; // don't trigger another start on next
                                     // block. :-)
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
            notifyAll();
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
         * 
         */
        protected void handleResponseSync() {
            // TODO Auto-generated method stub
            
        }

        protected void handleResponseAsync() {
            isAsync=true;
        }
    }

   
}
