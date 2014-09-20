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
package org.solmix.hola.discovery.zk.server;

import java.net.InetSocketAddress;

import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月19日
 */

public class ZKServer
{

    
    public static ZooKeeperServer startStandalone(Configuration conf) throws Exception{
        conf=conf.configure();
        ZooKeeperServer zooKeeperServer= new ZooKeeperServer();
        FileTxnSnapLog fileTxnSnapLog = new FileTxnSnapLog(conf.getZookeeperDataFile(), conf.getZookeeperDataFile());
        zooKeeperServer.setTxnLogFactory(fileTxnSnapLog);
        zooKeeperServer.setTickTime(conf.getTickTime());
        NIOServerCnxnFactory cnxnFactory = new NIOServerCnxnFactory();
        cnxnFactory.configure((new InetSocketAddress(conf.getClientPort())), 2);
        cnxnFactory.startup(zooKeeperServer);
        return zooKeeperServer;
    }
}
