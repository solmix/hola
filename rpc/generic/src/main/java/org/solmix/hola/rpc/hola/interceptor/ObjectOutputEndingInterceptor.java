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

package org.solmix.hola.rpc.hola.interceptor;

import java.io.IOException;
import java.io.OutputStream;

import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.dataformat.ObjectOutput;
import org.solmix.runtime.interceptor.Fault;
import org.solmix.runtime.interceptor.phase.Phase;
import org.solmix.runtime.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.runtime.interceptor.support.AttachmentOutInterceptor;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月14日
 */

public class ObjectOutputEndingInterceptor extends
    PhaseInterceptorSupport<Message> {

    public ObjectOutputEndingInterceptor() {
        this(Phase.PRE_STREAM_ENDING);
    }

    public ObjectOutputEndingInterceptor(String phase) {
        super(phase);
        getAfter().add(AttachmentOutInterceptor.AttachmentOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        ObjectOutput oo = message.getContent(ObjectOutput.class);
        if(oo!=null){
            try {
                oo.flushBuffer();
            } catch (IOException e) {
                //IGNORE
            }
            OutputStream os = (OutputStream)message.get(ObjectOutputInterceptor.OUTPUT_STREAM_HOLDER);
            //outputstream inputstream 的关闭在pipeline.close();
            if(os != null) {
                message.setContent(OutputStream.class, os);
            }
            message.removeContent(ObjectOutput.class);
        }
    }

}
