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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.model.RemoteInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月19日
 */

public class RemoteServiceRegistry implements Serializable
{

    private static final long serialVersionUID = 6523354816462309411L;

    public RemoteServiceRegistry()
    {
    }

    protected Map<ID, HolaRemoteServiceRegistration<?>>  publishedServices = 
                new ConcurrentHashMap<ID, HolaRemoteServiceRegistration<?>>(50);

    public void publishService(HolaRemoteServiceRegistration<?> reg) {
        publishedServices.put(reg.getID(), reg);
    }

    /**
     * @param nextServiceId2
     * @return
     */
    public HolaServiceID createRemoteServiceID(String serviceName,String version,String group,int port) {
        Namespace ns = IDFactory.getDefault().getNamespaceByName( HolaNamespace.NAME);
        return (HolaServiceID) IDFactory.getDefault().createID(ns,
            new Object[] { serviceName,version,group,new Integer(port) });
    }
    public HolaServiceID createRemoteServiceID(RemoteInfo info) {
        Namespace ns = IDFactory.getDefault().getNamespaceByName( HolaNamespace.NAME);
        return (HolaServiceID) IDFactory.getDefault().createID(ns,
            new Object[] { info.getPath(),info.getVersion(),info.getGroup(),info.getPort() });
    }
    /**
     * @param reg
     */
    public void unplublishService(HolaRemoteServiceRegistration<?> reg) {
        publishedServices.remove(reg.getID());
    }

    /**
     * 
     */
    public void destroy() {
        publishedServices.clear();
    }
}
