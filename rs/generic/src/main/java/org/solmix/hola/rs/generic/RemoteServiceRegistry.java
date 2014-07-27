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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.rs.identity.RemoteServiceID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月19日
 */

public class RemoteServiceRegistry implements Serializable
{

    private static long nextServiceId = 1;

    private static final long serialVersionUID = 6523354816462309411L;

    private ID providerID;

    public RemoteServiceRegistry()
    {
    }

    /**
     * @param local
     */
    public RemoteServiceRegistry(ID local)
    {
        providerID = local;
    }

    protected long getNextServiceId() {
        return nextServiceId++;
    }

    /**
     * 
     */
    public ID getProviderID() {
        return providerID;

    }

    protected HashMap<String, List<HolaRemoteServiceRegistration<?>>> publishedServicesByClass = new HashMap<String, List<HolaRemoteServiceRegistration<?>>>(
        50);

    protected ArrayList<HolaRemoteServiceRegistration<?>> allPublishedServices = new ArrayList<HolaRemoteServiceRegistration<?>>(
        50);

    public void publishService(HolaRemoteServiceRegistration<?> reg) {
        final String[] clazzes = (String[]) reg.getReference().getProperty(
            HolaConstants.REMOTE_OBJECTCLASS);
        final int size = clazzes.length;

        for (int i = 0; i < size; i++) {
            final String clazz = clazzes[i];
            List<HolaRemoteServiceRegistration<?>> services = publishedServicesByClass.get(clazz);
            if (services == null) {
                services = new ArrayList<HolaRemoteServiceRegistration<?>>(10);
                publishedServicesByClass.put(clazz, services);
            }

            services.add(reg);
        }
        allPublishedServices.add(reg);
    }

    /**
     * @param nextServiceId2
     * @return
     */
    public RemoteServiceID createRemoteServiceID(long seq) {
        Namespace ns = DefaultIDFactory.getDefault().getNamespaceByName(
            HolaNamespace.NAME);
        return (RemoteServiceID) DefaultIDFactory.getDefault().createID(ns,
            new Object[] { getProviderID(), new Long(seq) });
    }

    /**
     * @param reg
     */
    public void unplublishService(HolaRemoteServiceRegistration<?> reg) {
     // Remove the ServiceRegistration from the list of Services published by
        // Class Name.
        final String[] clazzes = (String[]) reg.getReference().getProperty(HolaConstants.REMOTE_OBJECTCLASS);
        final int size = clazzes.length;

        for (int i = 0; i < size; i++) {
              final String clazz = clazzes[i];
              final List<HolaRemoteServiceRegistration<?>>  services =  publishedServicesByClass.get(clazz);
              // Fix for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=329161
              if (services != null)
                    services.remove(reg);
        }
        // Remove the ServiceRegistration from the list of all published
        // Services.
        allPublishedServices.remove(reg);
    }

    /**
     * 
     */
    public void destroy() {
        publishedServicesByClass.clear();
        allPublishedServices.clear();
    }
}
