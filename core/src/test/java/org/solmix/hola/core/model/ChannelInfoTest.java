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
package org.solmix.hola.core.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;
import org.solmix.runtime.monitor.MonitorInfo;
import org.solmix.runtime.monitor.MonitorService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月14日
 */

public class ChannelInfoTest extends Assert
{

    @Test
    public void mergeTest() throws Exception{
        ChannelInfo c1= new ChannelInfo();
        c1.setHost("127.0.0.1");
        c1.setPort(8080);
        c1.setAccepts(100);
        c1.setBuffer(1024);
        c1.setHeartbeat(1000);
        c1.setCodec("hola");
        c1.setAwait(true);
        ExecutorInfo e= new ExecutorInfo();
        e.setThreadName("Thread1");
        c1.setExecutor(e);
        ChannelInfo c2= new ChannelInfo();
        c2.setHost("127.0.0.1");
        c2.setPort(8081);
        c2.setAccepts(100);
        c2.setBuffer(1024);
        c2.setHeartbeat(1000);
        c2.setCodec("hola2");
        c2.setAwait(false);
        ExecutorInfo e2= new ExecutorInfo();
        e2.setThreadName("Thread1");
        c2.setExecutor(e2);
        ChannelInfo c3= InfoUtils.merge(c2, c1);
        assertEquals(c2.getPort(), c3.getPort());
        assertEquals(c2.getCodec(), c3.getCodec());
        assertEquals(c2.getAwait(), c3.getAwait());
        assertEquals(c2.getExecutor().getThreadName(), c3.getExecutor().getThreadName());
    }
    @Test
    public void testMermey(){
        Container c = Containers.get();
        MonitorService ms=  c.getExtension(MonitorService.class);
        MonitorInfo info = ms.getMonitorInfo();
        List<ChannelInfo> lists= new ArrayList<ChannelInfo>();
       long b=System.currentTimeMillis();
        for(int i=0;i<100000;i++){
            ChannelInfo cc=ChannelInfo.newBuilder().setAccepts(10).setBuffer(1024).setCharset("UTF-9").setAwait(true)
                .setCodec("hola").setConnections(100).setConnectTimeout(10000).setDispather("default").setExchangeName("exchange")
                .setExecutor(null).setHeartbeat(10000).setHost("127.0.0.1").build();
            lists.add(cc);
        }
        MonitorInfo last = ms.getMonitorInfo();
        System.out.println(System.currentTimeMillis()-b);
        System.out.println(last.getUsedMemory()-info.getUsedMemory());
    }
    
    
}
