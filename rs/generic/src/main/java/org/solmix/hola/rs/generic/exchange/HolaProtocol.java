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
package org.solmix.hola.rs.generic.exchange;

import org.solmix.exchange.Message;
import org.solmix.exchange.Protocol;
import org.solmix.exchange.data.SerializationManager;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.support.AbstractProtocol;
import org.solmix.hola.rs.RemoteException;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月21日
 */

public class HolaProtocol extends AbstractProtocol implements Protocol
{

    private static final long serialVersionUID = -1687881624306758836L;

    private ProtocolInfo protocolInfo;

    private SerializationManager serializationManager;
    public HolaProtocol(ProtocolInfo protocolInfo,Container container){
        super(container);
        this.protocolInfo=protocolInfo;
    }
    @Override
    public Message createMessage() {
        return new HolaMessage();
    }

    @Override
    public Message createMessage(Message m) {
        //允许decode
        if(m instanceof HolaMessage&&!((HolaMessage)m).isDecoded()){
            try {
                HolaMessage hm = (HolaMessage)m;
                hm.setSerializationManager(this.getSerializationManager());
                hm.decode();
            } catch (Exception e) {
               throw new RemoteException("Error decode message",e);
            }
        }
        return m;
    }

    @Override
    public ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }
   
    public void setSerializationManager(SerializationManager sm) {
        this.serializationManager=sm;
    }
    
    public SerializationManager getSerializationManager() {
        return serializationManager;
    }

}
