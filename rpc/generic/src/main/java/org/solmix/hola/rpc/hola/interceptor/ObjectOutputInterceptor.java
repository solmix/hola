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

import org.solmix.runtime.Container;
import org.solmix.runtime.exchange.Message;
import org.solmix.runtime.exchange.dataformat.ObjectOutput;
import org.solmix.runtime.exchange.dataformat.Serialization;
import org.solmix.runtime.exchange.dataformat.SerializationManager;
import org.solmix.runtime.exchange.model.SerializationInfo;
import org.solmix.runtime.interceptor.Fault;
import org.solmix.runtime.interceptor.phase.Phase;
import org.solmix.runtime.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.runtime.interceptor.support.AttachmentOutInterceptor;
import org.solmix.runtime.io.AbstractWrappedOutputStream;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月14日
 */

public class ObjectOutputInterceptor extends PhaseInterceptorSupport<Message> {
    public static final String OUTPUT_STREAM_HOLDER = ObjectOutputInterceptor.class.getName() + ".outputstream";

    private final SerializationInfo serializationInfo;
    private final Serialization serialization;
    
    public static final ObjectOutputEndingInterceptor ENDING = new ObjectOutputEndingInterceptor();
    
    public ObjectOutputInterceptor(SerializationInfo si,Container c) {
        this(Phase.PRE_STREAM,si,c);
    }

    public ObjectOutputInterceptor(String phase,SerializationInfo si,Container c) {
        super(phase);
        getAfter().add(AttachmentOutInterceptor.class.getName());
        SerializationManager sm = c.getExtension(SerializationManager.class);
        serialization = sm.getSerializationByName(si.getName());
        this.serializationInfo=si;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        OutputStream os = message.getContent(OutputStream.class);
        ObjectOutput oo = message.getContent(ObjectOutput.class);
        if(os==null && oo!=null){
            return;
        }
        os = setupOutputStream(os);
        try {
            oo =  serialization.createObjectOutput(serializationInfo, os);
            message.put(Message.CONTENT_TYPE, serialization.getContentType());
            message.removeContent(OutputStream.class);
            message.put(OUTPUT_STREAM_HOLDER, os);
        } catch (IOException e) {
            throw new Fault("Exception create ObjectOutput Stream", e);
        }
        message.put(ObjectOutput.class, oo);
        
        message.getInterceptorChain().add(ENDING);
    }

    private OutputStream setupOutputStream(OutputStream os) {
       if(!(os instanceof AbstractWrappedOutputStream)){
           os = new AbstractWrappedOutputStream(os){};
       }
       ((AbstractWrappedOutputStream)os).allowFlush(false);
       return os;
    }
}
