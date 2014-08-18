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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.transport.TransporterProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月18日
 */

public class NettyExtensionTest
{

    @Test(expected=IllegalArgumentException.class)
    public void testNull() {
        Container c=   Containers.get();
        TransporterProvider tt=   c.getExtensionLoader(TransporterProvider.class).getExtension("ooxx");
        Assert.assertNull(tt);
        c.getExtensionLoader(TransporterProvider.class).getExtension(null);
    }
    /**
     * netty为默认的传输层实现.
     */
    @Test
    public void testDefault() {
        Container c=   Containers.get();
        TransporterProvider tt=   c.getExtensionLoader(TransporterProvider.class).getExtension(NettyTransporter.NAME);
        Assert.assertNotNull(tt);
        TransporterProvider ttt=c.getExtension(TransporterProvider.class);
        Assert.assertSame(ttt, ttt);
    }
    
    @Test
    public void testInstance() {
        Container c=   Containers.get();
        TransporterProvider tt=   c.getExtensionLoader(TransporterProvider.class).getExtension(NettyTransporter.NAME);
        Assert.assertEquals(NettyTransporter.class, tt.getClass());
    }
    
    /**
     * 测试container注入是否成功
     */
    @Test
    public void testInject() {
        Container c=   Containers.get();
        NettyTransporter tt=   (NettyTransporter)c.getExtensionLoader(TransporterProvider.class).getExtension(NettyTransporter.NAME);
       Assert.assertNotNull(tt.getContainer());
       Assert.assertSame(c, tt.getContainer());
    }
    
    @After
    public void after(){
        Containers.set(null);
    }

}
