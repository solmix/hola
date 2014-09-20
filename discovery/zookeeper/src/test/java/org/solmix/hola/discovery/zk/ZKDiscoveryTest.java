/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.hola.discovery.zk;

import static org.junit.Assert.fail;

import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.discovery.zk.server.Configuration;
import org.solmix.hola.discovery.zk.server.Configurator;
import org.solmix.hola.discovery.zk.server.ZKServer;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月17日
 */

public class ZKDiscoveryTest
{

    private static ZooKeeperServer zkServer;
    @BeforeClass
    public static void setup(){
       Configurator configurator= new Configurator();
       Configuration conf= configurator.createConfig("zoodiscovery.flavor.standalone=true;tickTime=2000;initLimit=10;syncLimit=5;clientPort=2181");
        try {
            zkServer=    ZKServer.startStandalone(conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 测试注册
     */
    @Test
    public void testRegister() {
        Assert.assertNotNull(zkServer);
        fail("Not yet implemented");
    }
    /**
     * 测试取消注册
     */
    @Test
    public void testUnRegister() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testAddListener() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testRemoveListener() {
        fail("Not yet implemented");
    }
    /**
     * 测试接口为多个
     */
    @Test
    public void testMutiServices() {
        fail("Not yet implemented");
    }
    /**
     * 测试失败重新发送
     */
    @Test
    public void testFailedback() {
        fail("Not yet implemented");
    }
}
