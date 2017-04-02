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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.transport.RemoteAddress;
import org.solmix.hola.transport.identity.ServerKeyID;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerEvent;
import org.solmix.runtime.ContainerListener;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class NettyServerEngineFactory implements ContainerListener {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerEngineFactory.class);
    private Container container;

    private static ConcurrentHashMap<ServerKeyID, NettyServerEngine> engines = new ConcurrentHashMap<ServerKeyID, NettyServerEngine>();

    private List<ServerKeyID> localCache=new ArrayList<ServerKeyID>(4);
    public NettyServerEngineFactory() {
    }
    
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

 
    @Override
    public void handleEvent(ContainerEvent event) {
        switch (event.getType()) {
        case ContainerEvent.POSTCLOSE:
        	for(ServerKeyID key:localCache){
        		NettyServerEngine engine=	engines.get(key);
        		if(engine!=null){
        			engines.remove(key);
        			engine.shutdown();
        		}
        	}
        	/*
            NettyServerEngine[] ens = engines.values().toArray( new NettyServerEngine[engines.values().size()]);
            for (NettyServerEngine en : ens) {
                en.shutdown();
            }
            engines.clear();*/
            break;
        default:
            break;
        }

    }
  
    public synchronized NettyServerEngine retrieveEngine(RemoteAddress remoteAddress) {
        ServerKeyID serverKey = remoteAddress.getServerKey();
        return engines.get(serverKey);
    }

    public synchronized NettyServerEngine createEngine(RemoteAddress remoteAddress) {
        ServerKeyID serverKey = remoteAddress.getServerKey();
        NettyServerEngine engine=engines.get(serverKey);
        if(engine==null){
            engine = new NettyServerEngine(remoteAddress.getHost(),remoteAddress.getPort());
            engine.setContainer(container);
            engine.finalizeConfig();
            NettyServerEngine e = engines.putIfAbsent(serverKey, engine);
            synchronized (localCache) {
            	if(e!=engine){
            		localCache.add(serverKey);
            	}
			}
            if(e!=null){
                engine =e;
            }
        }
        return engine;
    }

    public static String getServerKey(String host, int port) {
        StringBuilder builder= new StringBuilder();
        if(!StringUtils.isEmpty(host)){
            builder.append(host).append(":");
        }
        return builder.append(port).toString();
    }
    
    public static synchronized void destroy(int port) {
        destroy(null,null, port);
    }
    
    public static synchronized void destroy(String protocol,String host, int port) {
        ServerKeyID serverKey = new ServerKeyID(protocol,host, port);
        NettyServerEngine ref = engines.remove(serverKey);
        if (ref != null) {
            LOG.debug("Stop netty server engine on:{}", serverKey);
            try {
                ref.shutdown();
            } catch (Exception e) {
               //IGNORE
            }
        }
    }
   
}
