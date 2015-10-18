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

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.monitor.MonitorInfo;
import org.solmix.runtime.monitor.support.MonitorServiceImpl;

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
    public void test() throws RemoteException {
        RemoteServiceManager rm = container.getExtension(RemoteServiceManager.class);
        assertNotNull(rm);
        RemoteServiceFactory rsf = rm.getRemoteServiceFactory("hola");
        assertNotNull(rsf);
        HelloServiceImpl hs = new HelloServiceImpl();
        RemoteRegistration<HelloService> reg=  rsf.register(HelloService.class, hs, mockConfig());
        HelloService hello=rsf.getService(reg.getReference());
        assertSame(hs, hello);
        Dictionary<String, Object> properties=mockConfig();
        properties.put(HOLA.HOST_KEY, HOLA.LOCALHOST_VALUE);
        RemoteReference<HelloService> reference=rsf.getReference(HelloService.class, properties);
        assertNotNull(reference);
        HelloService remote = rsf.getService(reference);
        assertNotNull(remote);
        long start =System.currentTimeMillis();
        String str= getString();
        String mock="hello "+str;
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
                        Thread.sleep(10*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        };
        
        t.start();
        for( i=0;i<1000;i++){
           
            
        try {
            System.out.println("===========================");
            System.out.println("@@@@,"+System.nanoTime());
            String returnString=remote.say(str);
            assertEquals(mock,returnString);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }
        System.out.println("============="+(System.currentTimeMillis()-start));

    }
    /**
     * @return
     */
    private String getString() {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<1;i++){
//            sb.append("abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*(");
            sb.append("abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*(");
        }
        return sb.toString();
    }
    private Dictionary<String, Object> mockConfig(){
        Hashtable<String, Object> table = new Hashtable<String, Object>();
        table.put(HOLA.PATH_KEY, "/hola");
        table.put(HOLA.PORT_KEY,PORT);
        table.put(HOLA.TIMEOUT_KEY,1000*600);
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
