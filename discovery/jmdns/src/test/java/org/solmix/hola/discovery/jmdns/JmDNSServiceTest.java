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

package org.solmix.hola.discovery.jmdns;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.core.ConnectException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月10日
 */

public class JmDNSServiceTest
{

    private JmDNSProvider service;

    @Test
    public void mock(){
        
    }
//    @Before
    public void setup() {
        service = new JmDNSProvider();
    }
//    @After
    public void tearDown() throws Exception {
        service.disconnect();
        service.destroy();
  }
//    @Test
    public void testConnection() {
        Assert.assertNull(service.getTargetID());
        try {
            service.connect(null, null);
        } catch (ConnectException e) {
            fail("connect may not fail the first time");
        }
        Assert.assertNotNull(service.getTargetID());
    }

//    @Test
    public void testConnectiontwice() {
        testConnection();
        try {
            service.connect(null, null);
        } catch (ConnectException e) {
            return;
        }
        fail("succeeding connects should fail");
    }

//    @Test
    public void testDisconnection() {
        testConnection();
        service.disconnect();
        Assert.assertNull(service.getTargetID());
    }

//    @Test
    public void testReconnection() {
        testDisconnection();
        testConnection();
    }

//    @Test
    public void testDestroy() {
        testConnection();
        service.destroy();
        Assert.assertNull(service.getTargetID());
    }

//    @Test
    public void testGetServiceNull() {
        try {
            service.getService(null);
        } catch (Exception e) {
            return;
        }
        fail();
    }

//    @Test
    public void testAddServiceListenerNull() {
        try {
            service.addServiceListener(null);
        } catch (Exception e) {
            return;
        }
        fail();
    }

//    @Test
    public void testGetServicesNull() {
        try {
            service.getServices(null);
        } catch (Exception e) {
            return;
        }
        fail();
    }
    
   
}