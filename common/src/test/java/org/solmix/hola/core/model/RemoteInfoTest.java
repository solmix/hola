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
import org.solmix.hola.common.config.ExecutorInfo;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;
import org.solmix.runtime.monitor.MonitorInfo;
import org.solmix.runtime.monitor.MonitorService;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月14日
 */

public class RemoteInfoTest extends Assert
{

    @Test
    public void testMermey(){
        Container c = Containers.get();
        MonitorService ms=  c.getExtension(MonitorService.class);
        MonitorInfo info = ms.getMonitorInfo();
        List<RemoteInfo> lists= new ArrayList<RemoteInfo>();
       long b=System.currentTimeMillis();
        for(int i=0;i<1000;i++){
            RemoteInfo cc=RemoteInfo.newBuilder().setAccepts(10).setBuffer(1024).setCharset("UTF-9").setAwait(true)
                .setCodec("hola").setConnections(100).setConnectTimeout(10000).setDispather("default").setExchanger("exchange")
                .setHeartbeat(10000).setHost("127.0.0.1").build();
            lists.add(cc);
        }
        MonitorInfo last = ms.getMonitorInfo();
        System.out.println(System.currentTimeMillis()-b);
        System.out.println(last.getUsedMemory()-info.getUsedMemory());
    }
    @Test
    public void testcopy(){
        RemoteInfo cc=RemoteInfo.newBuilder().setAccepts(10).setBuffer(1024).setCharset("UTF-9").setAwait(true)
            .setCodec("hola").setConnections(100).setConnectTimeout(10000).setDispather("default").setExchanger("exchange")
            .setHeartbeat(10000).setHost("127.0.0.1").build();
        RemoteInfo dd=RemoteInfo.newBuilder(cc)
            .setCodec("hoa1")
            .setHeartbeat(null)
            .build();
        Assert.assertTrue(!dd.getCodec().equals(cc.getCodec()));
        Assert.assertTrue(dd.getHost().equals(cc.getHost()));
        Assert.assertNotSame(dd.getCodec(), cc.getCodec());
        Assert.assertNull(dd.getHeartbeat());
    }
    @Test
    public void testexecutorInfo(){
        ExecutorInfo.Builder b=    ExecutorInfo.newBuilder();
        b.setAlive(400);
        ExecutorInfo e=  b.build();
        
        RemoteInfo.Builder r=  RemoteInfo.newBuilder();
        r.setProperties(e.getProperties());
        r.setAccepts(100);
        RemoteInfo ri=r.build();
        RemoteInfo a=  ri.addProperty("aaa", "bb");
        Assert.assertNotSame(a, ri);
        ExecutorInfo exe= ExecutorInfo.newBuilder(ri).build();
        Assert.assertEquals(exe.getAlive(), new Integer(400));
        
    }
}
