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

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.RemoteServiceReference;
import org.solmix.hola.rs.identity.RemoteServiceID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月21日
 */

public class HolaRemoteServiceReference<S> implements RemoteServiceReference<S>
{

    private final String clazz;
    private final RemoteInfo info;
    private final HolaRemoteServiceManager manager;
    private RemoteServiceID id;
    private boolean active;
    public HolaRemoteServiceReference(String clazz, RemoteInfo info,
        HolaRemoteServiceManager manager)
    {
        this.clazz=clazz;
        this.manager=manager;
        this.info=info;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceReference#getID()
     */
    @Override
    public synchronized RemoteServiceID getID() {
        if(id==null){
            id=manager.createRemoteServiceID(info);
        }
        return id;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceReference#getInterfaces()
     */
    @Override
    public String[] getInterfaces() {
        return new String[]{clazz};
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceReference#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        return info.getProperty(key);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceReference#getPropertyKeys()
     */
    @Override
    public String[] getPropertyKeys() {
        return  info.getProperties().keySet().toArray(new String[]{});
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceReference#isActive()
     */
    @Override
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active){
        this.active=active;
    }

}
