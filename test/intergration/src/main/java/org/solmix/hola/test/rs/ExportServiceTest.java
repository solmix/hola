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
package org.solmix.hola.test.rs;

import junit.framework.TestCase;

import org.junit.Test;
import org.solmix.hola.common.identity.IDFactory;
import org.solmix.hola.rt.ServicePublisher;
import org.solmix.hola.rt.ServiceManager;
import org.solmix.hola.rt.config.DiscoveryConfig;
import org.solmix.hola.rt.config.ServiceConfig;
import org.solmix.hola.test.services.HelloWorldService;
import org.solmix.hola.test.services.HelloWorldServiceImpl;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月23日
 */

public class ExportServiceTest extends TestCase {
	
	private static Container container;
	
	@Override
	public  void setUp(){
		container= Containers.get();
	}
	@Test
	public void testExport(){
	    ServiceManager manager =container.getExtension(ServiceManager.class);
	  
		assertNotNull(manager);
		ServiceConfig<HelloWorldService> cf=new ServiceConfig<HelloWorldService>(container);
		cf.setInterface(HelloWorldService.class.getName());
		cf.setRef(new HelloWorldServiceImpl());
		cf.setProtocol("hola");
		DiscoveryConfig dicovery=cf.createDiscovery();
		
		dicovery.setAddress(DiscoveryConfig.NO_AVAILABLE);
		cf.setDiscovery(dicovery);
		//服务配置
		ServicePublisher	export= manager.createExporter(cf);
		assertNotNull(container.getExtension(IDFactory.class));
		//发布
		export.publish();
		
	}
	
	@Override
	public void tearDown(){
	    //colse by jvm shutdown hook
	    //container.close();
	}
}
