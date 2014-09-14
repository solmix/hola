/*
 * Copyright 2013 The Solmix Project
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
package org.solmix.hola.transport.netty;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.AbstractTransporter;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.TransporterProvider;
import org.solmix.hola.transport.channel.ChannelHandler;
import org.solmix.hola.transport.channel.Client;
import org.solmix.hola.transport.channel.Server;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年7月15日
 */
@Extension(name=NettyTransporter.NAME)
public class NettyTransporter extends AbstractTransporter implements TransporterProvider
{

    public static final String NAME="netty";
    
    private final Container container;
    
    public NettyTransporter(Container container){
        this.container=container;
    }
  
    @Override
    protected Server newServer(RemoteInfo info,ChannelHandler handler)
        throws TransportException {
        return new NettyServer(info, handler,container);
    }

   
    @Override
    protected Client newClient(RemoteInfo info,ChannelHandler handler)
        throws TransportException {
        return new NettyClient(info, handler,container);
    }
    
    public Container getContainer(){
        return container;
    }

}
