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
package org.solmix.hola.rs.generic;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteManagerProtocol;
import org.solmix.hola.rs.RemoteService;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月21日
 */

public class RemoteServiceTest extends TestCase
{
    private  RemoteManagerProtocol provider;
    private Container container;
    @Override
    public  void setUp(){
        container=Containers.get();
        provider=container.getExtensionLoader(RemoteManagerProtocol.class).getExtension("hola");
    }

    @Test
    public void test() throws InterruptedException {
        HolaRemoteManager manager=(HolaRemoteManager) provider.createManager();
        assertNotNull(provider);
        SimpleServiceImpl ss= new SimpleServiceImpl();
        RemoteInfo ei=RemoteInfo.valueOf("hola://localhost:1314/simple?codec=hola&heartbeat=60000");
        manager.registerRemoteService(new String[]{SimpleService.class.getName()}, ss, ei);
        RemoteInfo ec=RemoteInfo.valueOf("hola://localhost:1314/simple?codec=hola&heartbeat=60000");
        RemoteService rs=  manager.getRemoteService(SimpleService.class.getName(), ec);
    
        try {
            URI uri = new URI("hola://localhost:1314/simple?codec=hola&heartbeat=60000");
            System.out.println(uri.toString());
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
      
      try {
        Object o=  rs.getProxy();
        Assert.assertTrue(o instanceof SimpleService);
        SimpleService service=(SimpleService)o;
        String res=service.syaHello();
     Assert.assertNotNull(res);
    } catch ( RemoteException e) {
        e.printStackTrace();
    }
    }

}
