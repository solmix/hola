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

package org.solmix.hola.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.solmix.commons.util.NetUtils;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.Pipeline;
import org.solmix.exchange.Processor;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.AbstractPipeline;
import org.solmix.exchange.support.AbstractTransporter;
import org.solmix.exchange.support.DefaultExchange;
import org.solmix.runtime.Container;
import org.solmix.runtime.io.AbstractWrappedOutputStream;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public abstract class AbstractTCPTransporter extends AbstractTransporter {

    private TransporterRegistry registry;

    public static final String REQUEST_BYTEBUF = "REQUEST.BYTEBUF";

    public static final String RESPONSE_BYTEBUF = "RESPONSE.BYTEBUF";

    public AbstractTCPTransporter(String address, EndpointInfo endpointInfo,
        Container container, TransporterRegistry registry) {
        super(address, endpointInfo, container);
        this.registry = registry;
    }

    public void releaseRegistry() {
        registry = null;
    }

    @Override
    protected void activate(Processor p) {
        synchronized (this) {
            if (registry != null) {
                registry.add(this);
            }
        }
    }

    @Override
    protected void deactivate(Processor p) {
        synchronized (this) {
            if (registry != null) {
                registry.remove(getAddress());
            }
        }
    }

    protected static EndpointInfo getAddressValue(EndpointInfo ei, boolean dp) {
        if (dp) {

            String eiAddress = ei.getAddress();
            if (eiAddress == null) {
                try {
                    ServerSocket s = new ServerSocket(0);
                    ei.setAddress("http://localhost:" + s.getLocalPort());
                    s.close();
                    return ei;
                } catch (IOException ex) {
                    // problem allocating a random port, go to the default one
                    ei.setAddress("http://localhost");
                }
            }
            NetUtils.getAvailablePort();
        }
        return ei;
    }
    public void finalizeConfig() {
        
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.Transporter#shutdown()
     */
    @Override
    public void shutdown() {
        synchronized (this) {
            if (registry != null) {
                registry.remove(getAddress());
            }
        }

    }

    protected void invoke(Message inMsg, Message outMsg) {
        Exchange ex = new DefaultExchange();
        inMsg.setExchange(ex);
        ex.setIn(inMsg);
        ex.setOut(outMsg);
        inMsg.put(Transporter.class, this);
        try {
            processor.process(inMsg);
        } catch (Fault e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw e;
            }
        } catch (RuntimeException re) {
            throw re;
        } finally {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(
                    "Finished servicing http request on thread: "
                        + Thread.currentThread());
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.Transporter#getBackPipeline(org.solmix.exchange.Message)
     */
    @Override
    public Pipeline getBackPipeline(Message msg) throws IOException {
        ByteBuf res = (ByteBuf) msg.get(RESPONSE_BYTEBUF);
        return new ReturnPipeline(res);
    }

    public class ReturnPipeline extends AbstractPipeline {

        private final ByteBuf responseBuffer;

        public ReturnPipeline(ByteBuf buf) {
            super("anonymous");
            responseBuffer = buf;
        }

        @Override
        public void prepare(Message message) throws IOException {
            message.put(RESPONSE_BYTEBUF, responseBuffer);
            OutputStream os = message.getContent(OutputStream.class);
            if (os == null) {
                message.setContent(ByteBuf.class, responseBuffer);
                message.setContent(OutputStream.class, new WrappedOutputStream(
                    message, responseBuffer));
            }

        }

        @Override
        public void close(Message msg) throws IOException {
            super.close(msg);
            if (msg.getExchange() == null) {
                return;
            }
            Message m = msg.getExchange().getIn();
            if (m == null) {
                return;
            }
            InputStream is = m.getContent(InputStream.class);
            if (is != null) {
                try {
                    is.close();
                    m.removeContent(InputStream.class);
                } catch (IOException ioex) {
                    // ignore
                }
            }
        }

        @Override
        protected Logger getLogger() {
            return AbstractTCPTransporter.this.getLogger();
        }

    }

    private class WrappedOutputStream extends AbstractWrappedOutputStream {

        private final Message outMessage;

        private final ByteBuf responseBuffer;

        public WrappedOutputStream(Message message, ByteBuf responseBuffer) {
            this.outMessage = message;
            this.responseBuffer = responseBuffer;
        }

        @Override
        protected void onFirstWrite() throws IOException {
            OutputStream os = getOutputStream();
            if (os != null) {
                wrappedStream = os;
            }
        }
        @Override
        public void close() throws IOException {
            if (!written && wrappedStream == null) {
                OutputStream os = getOutputStream();
                if (os != null) {
                    wrappedStream = os;
                }
            }
            if (wrappedStream != null) {
                wrappedStream.close();
            }
        }

        private OutputStream getOutputStream() {
            boolean oneWay = isOneWay(outMessage);
            if (oneWay) {
                outMessage.remove(RESPONSE_BYTEBUF);
            }
            return new ByteBufOutputStream(responseBuffer);
        }

        private boolean isOneWay(Message message) {
            Exchange ex = message.getExchange();
            return ex == null ? false : ex.isOneWay();
        }
    }
}
