/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.rs.generic.interceptor;

import static org.solmix.exchange.MessageUtils.getString;

import org.solmix.exchange.Message;
import org.solmix.exchange.MessageList;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.data.ObjectOutput;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.exchange.model.MessageInfo;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.interceptor.SerializationOutInterceptor;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月24日
 */

public class HolaInInterceptor extends PhaseInterceptorSupport<Message>
{

    /**
     * @param phase
     */
    public HolaInInterceptor()
    {
        super(Phase.ENCODE);
        addAfter(SerializationOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            ObjectOutput out = message.getContent(ObjectOutput.class);
            if (MessageUtils.getBoolean(message, Message.EVENT_MESSAGE)) {
                Object msg= message.getContent(Object.class);
                out.writeObject(msg);
            }else{
                out.writeUTF(getString(message, HOLA.HOLA_VERSION_KEY, HOLA.DEFAULT_HOLA_VERSION));
                out.writeUTF(getString(message, Message.PATH_INFO));
                out.writeUTF(getString(message, HOLA.VERSION));
                // operation
                MessageInfo msi = message.get(MessageInfo.class);
                out.writeUTF(msi.getOperationInfo().getName().toIdentityString());
                // argument desc
                // out.writeUTF(ClassDescUtils.getTypeDesc(msi.geta));
                MessageList msgs= message.getContent(MessageList.class);
                if(msgs!=null){
                    for(Object msg:msgs){
                        out.writeObject(msg);
                    }
                }
            }
           

        } catch (Throwable ex) {
            throw new Fault(ex);
        }

    }

}
