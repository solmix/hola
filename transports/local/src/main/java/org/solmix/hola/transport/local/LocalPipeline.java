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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.support.AbstractPipeline;
import org.solmix.exchange.support.DefaultExchange;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.runtime.Container;
import org.solmix.runtime.io.AbstractWrappedOutputStream;
import org.solmix.runtime.io.CachedOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月18日
 */

public class LocalPipeline extends AbstractPipeline {
    
    public static final String IN_PIPE=LocalPipeline.class.getName()+".IN_PIPE";
    
    public static final String IN_EXCHANGE=LocalPipeline.class.getName()+".IN_EXCHANGE";
    
    /**
     * 表示是直接传送还是通过PipedStream传送,用法 <code>
     * Message.put( DIRECT_DISPATCH,Boolean.TRUE)  
     * </code>
     * */
    public static final String DIRECT_DISPATCH = LocalPipeline.class.getName()+ ".DIRECT_DISPATCH";

    private static final Logger LOG = LoggerFactory.getLogger(LocalPipeline.class);

    private final LocalTransportFactory transportFactory;

    private final LocalTransporter transporter;

    public LocalPipeline(LocalTransportFactory factory,
        LocalTransporter transporter) {
        super(transporter.getAddress());
        transportFactory = factory;
        this.transporter = transporter;
    }
    
    
    @Override
    public void prepare(Message message) throws IOException {
        if(!MessageUtils.isTrue(message.get(DIRECT_DISPATCH))){
            dispatchViaPipe(message);
        }else{
            CachedOutputStream stream = new CachedOutputStream();
            message.setContent(OutputStream.class, stream);
            //save the original stream
            message.put(CachedOutputStream.class, stream);
            stream.holdTempFile();
        }
    }
    
    /**
     *通过PipedInputStream发送消息.
     */
    private void dispatchViaPipe(Message message) {
        final LocalPipeline conduit = this;
        final Exchange exchange = message.getExchange();

        if (transporter.getProcessor() == null) {
            throw new IllegalStateException("Local Pipeline does not have a Processor on address " 
                                            + getAddress());
        }
        
        AbstractWrappedOutputStream cout 
            = new LocalPipedOutputStream(conduit, exchange, message);
        message.setContent(OutputStream.class, cout);
        
    }
    @Override
    public void close(Message message) throws IOException {
        if(MessageUtils.isTrue(message.get(DIRECT_DISPATCH))
            && !message.isInbound()){
            if(transporter.getProcessor()==null){
                throw new IllegalArgumentException("transporter processor is null");
            }
            DefaultMessage copy = new DefaultMessage();
            copy.put(IN_PIPE, this);
            copy.put(Transporter.class, transporter);
            
            transportFactory.copy(message, copy);
            DefaultMessage.copyContent(message, copy);
            
            OutputStream out = message.getContent(OutputStream.class);
            out.flush();
            out.close();
            
            CachedOutputStream stream = message.get(CachedOutputStream.class);
            copy.setContent(InputStream.class, stream.getInputStream());
            copy.removeContent(CachedOutputStream.class);
            stream.releaseTempFileHold();
            
            // Create a new incoming exchange and store the original exchange for the response
            DefaultExchange ex = new DefaultExchange();
            ex.setIn(copy);
            ex.put(IN_EXCHANGE, message.getExchange());
            ex.put(LocalPipeline.DIRECT_DISPATCH, true);
            ex.put(Transporter.class, transporter);
            
            transporter.getProcessor().process(copy);
        }
        super.close(message);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
    //在一个线程中发送,另外的线程接收,避免阻塞.
    private final class LocalPipedOutputStream extends AbstractWrappedOutputStream {
        private final LocalPipeline conduit;
        private final Exchange exchange;
        private final Message message;

        private LocalPipedOutputStream(LocalPipeline conduit, Exchange exchange, Message message) {
            this.conduit = conduit;
            this.exchange = exchange;
            this.message = message;
        }

        @Override
        public void close() throws IOException {
            if (!written) {
                dispatchToService(true);
            }
            super.close();
        }

        @Override
        protected void onFirstWrite() throws IOException {
            dispatchToService(false);
        }
        
        protected void dispatchToService(boolean empty) throws IOException {
            final DefaultMessage inMsg = new DefaultMessage();
            transportFactory.copy(message, inMsg);
            
            if (!empty) {
                final PipedInputStream stream = new PipedInputStream();
                wrappedStream = new PipedOutputStream(stream);
                inMsg.setContent(InputStream.class, stream);
            }
            ByteBuf buffer = Unpooled.buffer();
            
            inMsg.put(Transporter.class, transporter);
            inMsg.put(IN_PIPE, conduit);

            final Runnable receiver = new Runnable() {
                @Override
                public void run() {                            
                    DefaultExchange ex = new DefaultExchange();
                    ex.put(Container.class, transporter.getContainer());
                    //设置返回的Message,message的interceptor在transporter.getProcessor().process(inMsg)中
                    //由ChainInitiationProcessor初始化.
                    ex.setIn(inMsg);
                    inMsg.setExchange(ex);
                    ex.put(IN_EXCHANGE, exchange);
                    try {
                        transporter.getProcessor().process(inMsg);
                    } catch (Throwable t) {
                        Message m = inMsg.getExchange().getOutFault();
                        if (m == null) {
                            m = inMsg.getExchange().getOut();
                        }
                        if (m != null) {
                            try {
                                m.put(Message.RESPONSE_CODE, 500);
                                m.put(Message.PROTOCOL_HEADERS, new HashMap<String, List<String>>());
                                m.getExchange().put(Message.RESPONSE_CODE, 500);
                                m.getContent(OutputStream.class).close();
                            } catch (IOException e) {
                                //ignore
                            }
                        }
                    }
                }
            };
            Executor ex = message.getExchange() != null
                ? message.getExchange().get(Executor.class) : null;
            if (ex == null ) {
                ex = transportFactory.getExecutor(transporter.getContainer());
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
