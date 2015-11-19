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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.solmix.commons.util.NetUtils;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Pipeline;
import org.solmix.exchange.Processor;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.AbstractPipeline;
import org.solmix.exchange.support.AbstractTransporter;
import org.solmix.exchange.support.DefaultExchange;
import org.solmix.hola.common.io.MessageWrappedOutputStream;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public abstract class AbstractRemoteTransporter extends AbstractTransporter
{

    private TransporterRegistry registry;


    public AbstractRemoteTransporter(String address, EndpointInfo endpointInfo, Container container, TransporterRegistry registry)
    {
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
        ex.setOneWay(MessageUtils.getBoolean(outMsg, Message.ONEWAY));
        ex.setIn(inMsg);
        ex.setOut(outMsg);
        outMsg.setId(inMsg.getId());
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
                getLogger().trace("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
    }

    @Override
    public Pipeline getBackPipeline(Message msg) throws IOException {
        return new ReturnPipeline();
    }

    public class ReturnPipeline extends AbstractPipeline
    {


        public ReturnPipeline()
        {
            super("anonymous");
        }

        @Override
        public void prepare(Message message) throws IOException {
            OutputStream os = message.getContent(OutputStream.class);
            if (os == null) {
                message.setContent(OutputStream.class, new WrappedOutputStream(message));
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
            return AbstractRemoteTransporter.this.getLogger();
        }

    }

    private class WrappedOutputStream extends MessageWrappedOutputStream
    {

        public WrappedOutputStream(Message message)
        {
            super(message);
        }
        @Override
        public void send() throws IOException {
            
        }

        
    }
}
