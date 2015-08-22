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
import java.io.InputStream;

import org.solmix.runtime.Container;
import org.solmix.exchange.Message;
import org.solmix.exchange.dataformat.ObjectInput;
import org.solmix.exchange.dataformat.Serialization;
import org.solmix.exchange.dataformat.SerializationManager;
import org.solmix.exchange.model.SerializationInfo;
import org.solmix.runtime.interceptor.Fault;
import org.solmix.runtime.interceptor.phase.Phase;
import org.solmix.runtime.interceptor.phase.PhaseInterceptorSupport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月14日
 */

public class ObjectInputInterceptor extends PhaseInterceptorSupport<Message> {

    private SerializationInfo si;

    private SerializationManager serializationManager;

    public ObjectInputInterceptor(SerializationInfo si, Container c) {
        this(Phase.POST_STREAM);
        serializationManager = c.getExtension(SerializationManager.class);
    }

    public ObjectInputInterceptor(String phase) {
        super(phase);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (message.getContent(ObjectInput.class) != null) {
            return;
        }
        InputStream is = message.getContent(InputStream.class);
        if (is == null) {
            return;
        }
        Byte sid = (Byte) message.get(Serialization.SERIALIZATION_ID);
        Serialization s = serializationManager.getSerializationById(sid);
        ObjectInput oi = null;
        try {
            oi = s.createObjectInput(si, is);

        } catch (IOException e) {
            throw new Fault("Exception create ObjectInput Stream", e);
        }
        message.setContent(ObjectInput.class, oi);
        message.getInterceptorChain().add(ObjectInputEndingInterceptor.INSTANCE);
    }

}
