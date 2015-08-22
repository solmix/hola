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

import org.solmix.exchange.Message;
import org.solmix.exchange.dataformat.ObjectInput;
import org.solmix.runtime.interceptor.Fault;
import org.solmix.runtime.interceptor.phase.Phase;
import org.solmix.runtime.interceptor.phase.PhaseInterceptorSupport;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月14日
 */

public class ObjectInputEndingInterceptor extends
    PhaseInterceptorSupport<Message> {

    public static final String REUSE_OBJECT_INPUT = ObjectInputEndingInterceptor.class.getName()+".REUSE_OBJECT_INPUT";
    public final static ObjectInputEndingInterceptor INSTANCE = new ObjectInputEndingInterceptor();
    public ObjectInputEndingInterceptor(String phase) {
        super(phase);
    }
    public ObjectInputEndingInterceptor() {
        super(Phase.POST_STREAM_ENDING);
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.interceptor.Interceptor#handleMessage(org.solmix.exchange.Message)
     */
    @Override
    public void handleMessage(Message message) throws Fault {
        ObjectInput oo = message.getContent(ObjectInput.class);
        if (oo != null) {
            message.removeContent(ObjectInput.class);
        }
    }

}
