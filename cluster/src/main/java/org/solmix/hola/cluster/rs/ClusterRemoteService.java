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
package org.solmix.hola.cluster.rs;

import java.util.concurrent.Future;

import org.solmix.hola.rs.RSRequest;
import org.solmix.hola.rs.RSRequestListener;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月21日
 */

public class ClusterRemoteService implements RemoteService{

	private RemoteService delegate;
	
	public ClusterRemoteService(RemoteService rs){
		delegate=rs;
	}
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#getInterfaces()
	 */
	@Override
	public String[] getInterfaces() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#sync(org.solmix.hola.rs.RSRequest)
	 */
	@Override
	public Object sync(RSRequest call) throws RemoteServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#async(org.solmix.hola.rs.RSRequest, org.solmix.hola.rs.RSRequestListener)
	 */
	@Override
	public void async(RSRequest call, RSRequestListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#async(org.solmix.hola.rs.RSRequest)
	 */
	@Override
	public Future<Object> async(RSRequest call) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#fireAsync(org.solmix.hola.rs.RSRequest)
	 */
	@Override
	public void fireAsync(RSRequest call) throws RemoteServiceException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#getProxy()
	 */
	@Override
	public Object getProxy() throws RemoteServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.solmix.hola.rs.RemoteService#getProxy(java.lang.ClassLoader, java.lang.Class[])
	 */
	@Override
	public Object getProxy(ClassLoader classLoader, Class<?>[] interfaceClasses)
			throws RemoteServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
