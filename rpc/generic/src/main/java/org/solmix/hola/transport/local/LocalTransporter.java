/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.transport.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.runtime.Container;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Pipeline;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.AbstractPipeline;
import org.solmix.exchange.support.AbstractTransporter;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.runtime.io.AbstractWrappedOutputStream;
import org.solmix.runtime.io.CachedOutputStream;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月18日
 */

public class LocalTransporter extends AbstractTransporter {

    private static final Logger LOG = LoggerFactory.getLogger(LocalTransporter.class);
    
    private final LocalTransportFactory factory;

    public LocalTransporter(LocalTransportFactory tfactory, String address,
        EndpointInfo info, Container container) {
        super(address, info, container);
        factory = tfactory;
    }
    
    @Override
    public void shutdown() {
        factory.remove(this);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    
    @Override
    public Pipeline getBackPipeline(Message msg) throws IOException {
        Pipeline pipeline = (Pipeline)msg.get(LocalPipeline.IN_PIPE);
        if (pipeline instanceof LocalPipeline) {
            return new SynchronousPipeline((LocalPipeline)pipeline);
        }
        return null;
    }

    public Container getContainer() {
        return container;
    }
    
    class SynchronousPipeline extends AbstractPipeline {
        

        private final LocalPipeline conduit;

        public SynchronousPipeline(LocalPipeline conduit) {
            super(null);
            this.conduit = conduit;
        }

        @Override
        public void prepare(final Message message) throws IOException {            
            if (!Boolean.TRUE.equals(message.getExchange().get(LocalPipeline.DIRECT_DISPATCH))) {
                final Exchange exchange = (Exchange)message.getExchange().get(LocalPipeline.IN_EXCHANGE);

                AbstractWrappedOutputStream cout 
                    = new LocalTransporterOutputStream(exchange, message);
                
                message.setContent(OutputStream.class, cout);    
                
            } else {
                CachedOutputStream stream = new CachedOutputStream();
                message.setContent(OutputStream.class, stream);
                message.setContent(CachedOutputStream.class, stream);
                stream.holdTempFile();
            }
        }

        @Override
        public void close(Message message) throws IOException {
            // set the pseudo status code if not set (REVISIT add this method in MessageUtils to be reused elsewhere?)
            Integer i = (Integer)message.get(Message.RESPONSE_CODE);
            if (i == null) {
                int code = (message.getExchange().isOneWay() 
                    || MessageUtils.isEmptyPartialResponse(message)) ? 202 : 200;
                message.put(Message.RESPONSE_CODE, code);
            }
            if (Boolean.TRUE.equals(message.getExchange().get(LocalPipeline.DIRECT_DISPATCH))) {
                final Exchange exchange = (Exchange)message.getExchange().get(LocalPipeline.IN_EXCHANGE);
                
                DefaultMessage copy = new DefaultMessage();
                copy.putAll(message);
                message.getContent(OutputStream.class).close();
                CachedOutputStream stream = message.getContent(CachedOutputStream.class);
                message.setContent(OutputStream.class, stream);
                DefaultMessage.copyContent(message, copy);
                copy.setContent(InputStream.class, stream.getInputStream());
                stream.releaseTempFileHold();
                if (exchange != null && exchange.getIn() == null) {
                    exchange.setIn(copy);
                }                
                conduit.getProcessor().process(copy);
                return;
            }
            
            super.close(message);
        }

        @Override
        protected Logger getLogger() {
            return LOG;
        }
        private final class LocalTransporterOutputStream extends AbstractWrappedOutputStream {
            private final Exchange exchange;
            private final Message message;

            private LocalTransporterOutputStream(Exchange exchange, Message message) {
                this.exchange = exchange;
                this.message = message;
            }

            @Override
            public void close() throws IOException {
                if (!written) {
                    dispatchToClient(true);
                }
                super.close();
            }

            @Override
            protected void onFirstWrite() throws IOException {
                dispatchToClient(false);
            }

            protected void dispatchToClient(boolean empty) throws IOException {
                final DefaultMessage m = new DefaultMessage();
                factory.copy(message, m);
                if (!empty) {
                    final PipedInputStream stream = new PipedInputStream();
                    wrappedStream = new PipedOutputStream(stream);
                    m.setContent(InputStream.class, stream);
                }

                final Runnable receiver = new Runnable() {
                    @Override
                    public void run() {                                    
                        if (exchange != null) {
                            exchange.setIn(m);
                        }
                        conduit.getProcessor().process(m);
                    }
                };
                Executor ex = message.getExchange() != null
                    ? message.getExchange().get(Executor.class) : null;
                // Need to avoid to get the SynchronousExecutor
                if (ex == null ) {
                    if (exchange == null) {
                        ex = factory.getExecutor(container);
                    } else {
                        ex = factory.getExecutor(exchange.getContainer());
                    }
                    if (ex != null) {
                        ex.execute(receiver);
                    } else {
                        new Thread(receiver).start();
                    }
                } else {
                    ex.execute(receiver);
                }
            }
        }
    }

   
    @Override
    public int getDefaultPort() {
        throw new UnsupportedAddressTypeException();
    }
}
