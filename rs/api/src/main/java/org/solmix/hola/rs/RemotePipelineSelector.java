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
import org.solmix.exchange.PipelineSelector;
import org.solmix.exchange.support.AbstractPipelineSelector;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月27日
 */

public class RemotePipelineSelector extends AbstractPipelineSelector implements PipelineSelector,Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(RemotePipelineSelector.class);
    protected static final String KEEP_PIPELINE_ALIVE = "KeepPipelineAlive";
    protected Endpoint endpoint;
    protected Pipeline[] pipelines ;
    private final boolean shared;
    public RemotePipelineSelector(boolean shared, int pipelines) {
        this.shared=shared;
        createPreexitPipelines(pipelines);
    }

    protected void createPreexitPipelines(int pls) {
        pipelines = new Pipeline[pls];
        
    }

    @Override
    public void prepare(Message message) {
        Pipeline pl = message.get(Pipeline.class);
        if (pl != null) {
            pl = getSelectedPipeline(message);
            message.put(Pipeline.class, pl);
        }
    }

    @Override
    public Pipeline select(Message message) {
        Pipeline pl = message.get(Pipeline.class);
        if (pl != null) {
            pl = getSelectedPipeline(message);
            message.put(Pipeline.class, pl);
        }
        return pl;
    }

    @Override
    public void complete(Exchange exchange) {
        if (MessageUtils.isTrue(exchange.get(KEEP_PIPELINE_ALIVE))) {
            return;
        }
        try {
            final Message inMsg = exchange.getIn();
            if (inMsg != null) {
                Pipeline pl = exchange.getOut().get(Pipeline.class);
                if (pl == null) {
                    getSelectedPipeline(inMsg).close(inMsg);
                } else {
                    pl.close(exchange.getIn());
                }
            }
        } catch (IOException e) {
            // IGNORE
        }
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

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
