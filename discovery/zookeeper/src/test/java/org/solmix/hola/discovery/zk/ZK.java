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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.solmix.hola.core.model.DiscoveryInfo;
import org.solmix.hola.discovery.zk.support.CuratorZKClient;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月16日
 */

public class ZK
{

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        Map<String,Object> prop= new HashMap<String,Object>();
        prop.put("address", "localhost:2181,localhost:2182,localhost:2183");
       DiscoveryInfo info= new DiscoveryInfo(prop);
       ZKClient client = new CuratorZKClient(info);
       client.create("/test/1", false);
       client.create("/test/2", false);
       client.create("/test/3", false);
      System.out.println( client.getChildren("/test"));
       client.close();

    }

}
