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

package org.solmix.hola.rs.generic;

import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.exchange.Server;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.hola.rs.call.DefaultRemoteRequest;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteRequestListener;
import org.solmix.hola.rs.call.RemoteResponse;
import org.solmix.hola.rs.support.RemoteRegistrationImpl;
import org.solmix.hola.transport.netty.NettyTransporter;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.monitor.MonitorInfo;
import org.solmix.runtime.monitor.support.MonitorServiceImpl;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月17日
 */

public class HolaRemoteServiceFactoryTest extends Assert
{

    public static final int PORT =  3333/*NetUtils.getAvailablePort()*/;
    static int i=0;
    @Test
    public void test() throws RemoteException, InterruptedException, ExecutionException {
        RemoteServiceManager rm = container.getExtension(RemoteServiceManager.class);
        assertNotNull(rm);
        RemoteServiceFactory rsf = rm.getRemoteServiceFactory("hola");
        assertNotNull(rsf);
        HelloServiceImpl hs = new HelloServiceImpl();
        //注册
        RemoteRegistration<HelloService> reg=  rsf.register(HelloService.class, hs, mockConfig());
        RemoteRegistrationImpl<HelloService> regImpl =(RemoteRegistrationImpl<HelloService>)reg;
        Server server = regImpl.getServer();
        NettyTransporter nettyTransporter = (NettyTransporter)server.getTransporter();
        ChannelGroup group=nettyTransporter.getNettyEngine().getAllChannels();
        
        
        HelloService hello=rsf.getService(reg.getReference());
        assertSame(hs, hello);
        Dictionary<String, Object> properties=mockConfig();
       
        RemoteReference<HelloService> reference=rsf.getReference(HelloService.class, properties);
        assertNotNull(reference);
        HelloService remote = rsf.getService(reference);
        assertNotNull(remote);
        long start =System.currentTimeMillis();
        String str= getString();
        final String mock="hello "+str;
        ResourceLeakDetector.setLevel(Level.PARANOID);
        
        Thread t = new Thread() {

            @Override
            public void run() {
                long last=0;
                do {
                    MonitorInfo old = new MonitorServiceImpl().getMonitorInfo();
                    long now=old.getUsedMemory();
                    System.out.println("count:" + i + ",Thread:" + old.getTotalThread() + ",mem:" + (now-last) / (1000 * 1000));
                    last=now;
                    try {
                        Thread.sleep(10*100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        };
        
//        t.start();
        //测试正常同步请求
        for( i=0;i<1;i++){
        try {
            String returnString=remote.say(str);
            assertEquals(mock,returnString);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }
        group=nettyTransporter.getNettyEngine().getAllChannels();
        for(Channel ch:group) {
        	SocketAddress address=ch.remoteAddress();
        	boolean active=ch.isActive();
        	ch.writeAndFlush("aaaaa");
        }
        //使用RemoteService
        RemoteService<HelloService> rs = rsf.getRemoteService(reference);
        assertNotNull(rs);
        RemoteRequest request = new DefaultRemoteRequest("say", str);
        RemoteResponse res = rs.sync(request);
        assertNotNull(res); 
        assertEquals(mock,res.getValue());
        //异步请求
        final CountDownLatch count= new CountDownLatch(1);
        rs.async(request, new RemoteRequestListener() {
            
            @Override
            public void handleResponse(Object res, Map<String, Object> ctx) {
                assertEquals(mock,res);
                count.countDown();
            }
            
            @Override
            public void handleException(Throwable ex, Map<String, Object> ctx) {
                count.countDown();
                
            }
        });
        count.await();
        //无返回请求
        RemoteRequest send = new DefaultRemoteRequest("send", str);
        rs.fireAsync(send);
        Future<RemoteResponse> future=rs.async(request);
       String returnValue= (String) future.get().getValue();
       assertEquals(mock, returnValue);
       
        reference.destroy();
        reg.unregister();

    }
    /**
     * @return
     */
    private String getString() {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<1;i++){
            sb.append("abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*(");
        }
        return sb.toString();
    }
    private Dictionary<String, Object> mockConfig(){
        Hashtable<String, Object> table = new Hashtable<String, Object>();
        table.put(HOLA.PATH_KEY, "/hola");
        table.put(HOLA.PORT_KEY,PORT);
        table.put(HOLA.TIMEOUT_KEY,1000*600);
//        table.put(HOLA.TRANSPORTER_KEY, "local");
        table.put(HOLA.HOST_KEY, HOLA.LOCALHOST_VALUE);
        //ipv6
//        table.put(HOLA.HOST_KEY, "111::2");
        return table;
    }

    static Container container;

    @BeforeClass
    public static void setup() {
        ContainerFactory.setThreadDefaultContainer(null);
        container = ContainerFactory.getThreadDefaultContainer(true);
    }

    @AfterClass
    public static void tearDown() {
        if (container != null) {
            container.close();
        }
    }
}
