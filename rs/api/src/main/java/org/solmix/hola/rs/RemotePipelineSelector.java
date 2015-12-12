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

package org.solmix.hola.rs;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Pipeline;
import org.solmix.exchange.PipelineFactory;
import org.solmix.exchange.PipelineFactoryManager;
import org.solmix.exchange.PipelineSelector;
import org.solmix.exchange.Processor;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.AbstractPipelineSelector;
import org.solmix.hola.common.util.AtomicPositiveInteger;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月27日
 */

public class RemotePipelineSelector implements PipelineSelector, Closeable
{

    private static final Logger LOG = LoggerFactory.getLogger(RemotePipelineSelector.class);

    private final AtomicPositiveInteger index = new AtomicPositiveInteger();

    protected Endpoint endpoint;

    protected Pipeline[] pipelines;

    private final boolean shared;

    private Container container;

    private volatile boolean init;

    private int limits;

    public RemotePipelineSelector(Container container, boolean shared, int pipelines)
    {
        this.shared = shared;
        this.limits = pipelines;
        setContainer(container);

    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    protected void createPreexitPipelines(int pls) {
        pipelines = new Pipeline[pls];

        EndpointInfo info = endpoint.getEndpointInfo();
        final String transportID = info.getTransporter();
        try {
            PipelineFactoryManager pfm = container.getExtension(PipelineFactoryManager.class);

            if (pfm != null) {
                PipelineFactory factory = pfm.getFactory(transportID);
                for (int i = 0; i < pipelines.length; i++) {
                    Pipeline newpipe = factory.getPipeline(info, container);
                    newpipe.setProtocol(endpoint.getProtocol());
                    pipelines[i] = newpipe;
                }
            } else {
                LOG.warn("PipelineFactoryManager not found");
            }
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    @Override
    public void prepare(Message message) {
        if (init) {
            return;
        } else {
            synchronized (endpoint) {
                createPreexitPipelines(limits);
                init = true;
            }
        }

    }

    @Override
    public Pipeline select(Message message) {
        Pipeline pl = getPipeline(message);
        if (pl == null) {
            if (pipelines.length == 1) {
                pl = pipelines[0];
            } else {
                pl = pipelines[index.getAndIncrement() % pipelines.length];
            }
            Processor p = message.getExchange().get(Processor.class);
            if (p != null) {
                pl.setProcessor(p);
            } else {
                LOG.warn("Message Processor not found!");
            }
            message.put(Pipeline.class, pl);
        }
        return pl;
    }

    @Override
    public void complete(Exchange exchange) {
        if (MessageUtils.isTrue(exchange.get(AbstractPipelineSelector.KEEP_PIPELINE_ALIVE))) {
            return;
        }
        try {
            final Message inMsg = exchange.getIn();
            if (inMsg != null) {
                Pipeline pl = exchange.getOut().get(Pipeline.class);
                if (pl == null) {
                    getPipeline(inMsg).close(inMsg);
                } else {
                    pl.close(exchange.getIn());
                }
            }
        } catch (IOException e) {
            // IGNORE
        }
    }

    private Pipeline getPipeline(Message msg) {
        Pipeline pl = msg.get(Pipeline.class);
        // found in out.
        if (pl == null && msg.getExchange() != null && msg.getExchange().getOut() != null && msg.getExchange().getOut() != msg) {
            pl = msg.getExchange().getOut().get(Pipeline.class);
        }
        return pl;
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;

    }

    @Override
    public void close() throws IOException {
        for (Pipeline p : pipelines) {
            p.close();
        }
    }
}
