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

package org.solmix.hola.transport.netty;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerEvent;
import org.solmix.runtime.ContainerListener;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerEngineFactory implements ContainerListener {

    private Container container;

    private static ConcurrentHashMap<String, NettyServerEngine> engines = new ConcurrentHashMap<String, NettyServerEngine>();

    public NettyServerEngineFactory() {

    }

    /**   */
    public Container getContainer() {
        return container;
    }

    @Resource
    public void setContainer(Container container) {
        this.container = container;
        if (container != null) {
            container.setExtension(this, NettyServerEngineFactory.class);
            container.addListener(this);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.ContainerListener#handleEvent(org.solmix.runtime.ContainerEvent)
     */
    @Override
    public void handleEvent(ContainerEvent event) {
        switch (event.getType()) {
        case ContainerEvent.POSTCLOSE:
            NettyServerEngine[] ens = engines.values().toArray(
                new NettyServerEngine[engines.values().size()]);
            for (NettyServerEngine en : ens) {
                en.shutdown();
            }
            engines.clear();
            break;
        default:
            break;
        }

    }

    public synchronized NettyServerEngine retrieveEngine(String host, int port) {
        String serverKey = getServerKey(host, port);
        return engines.get(serverKey);
    }

    public synchronized NettyServerEngine createEngine(String host, int port) {
        String serverKey = getServerKey(host, port);
        NettyServerEngine engine=engines.get(serverKey);
        if(engine==null){
            engine = new NettyServerEngine(host,port);
            engine.finalizeConfig();
            NettyServerEngine e = engines.putIfAbsent(serverKey, engine);
            if(e!=null){
                engine =e;
            }
        }
        return engine;
    }

    public String getServerKey(String host, int port) {
        return new StringBuilder().append(host).append(":").append(port).toString();
    }
}
