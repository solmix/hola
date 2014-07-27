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

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.InvalidSyntaxException;
import org.solmix.commons.util.Assert;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.ConnectListener;
import org.solmix.hola.core.event.ConnectedEvent;
import org.solmix.hola.core.event.DisconnectedEvent;
import org.solmix.hola.core.event.EjectedConnectEvent;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.security.ConnectSecurityContext;
import org.solmix.hola.rs.RemoteConnectException;
import org.solmix.hola.rs.RemoteConstants;
import org.solmix.hola.rs.RemoteFilter;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceListener;
import org.solmix.hola.rs.RemoteServiceProvider;
import org.solmix.hola.rs.RemoteServiceReference;
import org.solmix.hola.rs.RemoteServiceRegistration;
import org.solmix.hola.rs.identity.RemoteServiceID;
import org.solmix.hola.shared.BaseSharedService;
import org.solmix.hola.shared.SharedMessage;
import org.solmix.runtime.event.Event;
import org.solmix.runtime.event.EventProcessor;

/**
 * 注册远程服务
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月19日
 */

public class RegistryService extends BaseSharedService implements
    RemoteServiceProvider
{
    private RemoteServiceRegistry registry;

    @Override
    public void initialize() {
        super.initialize();
        ID local = getLocalProviderID();
        registry = (local == null) ? new RemoteServiceRegistry()
            : new RemoteServiceRegistry(local);
        addEventProcessor(new EventProcessor() {

            @Override
            public boolean process(Event event) {
                if (event instanceof ConnectedEvent) {

                } else if (event instanceof DisconnectedEvent) {

                } else if (event instanceof EjectedConnectEvent) {

                }
                return false;
            }

        });

    }

    @Override
    public RemoteServiceRegistration<?> registerRemoteService(String[] clazzes,
        Object service, Map<String, ?> properties) {
        Assert.isNotNull(service);
        final int size = clazzes.length;
        if (size == 0) {
            throw new IllegalArgumentException(
                "Service classes list is empty");
        }
        final String[] copy = new String[clazzes.length];
        for (int i = 0; i < clazzes.length; i++) {
            copy[i] = new String(clazzes[i].getBytes());
        }
        clazzes = copy;
        final String invalidService = checkServiceClass(clazzes, service);
        if (invalidService != null) {
            throw new IllegalArgumentException("Service=" + invalidService
                + " is invalid");
        }
        final HolaRemoteServiceRegistration<Object> reg = new HolaRemoteServiceRegistration<Object>();
        synchronized (registry) {
            reg.publish(this, registry, clazzes, service, properties);
            if (isConnected()) {
                final ID[] targets = getTargetsFromProperties(properties);
                HolaRemoteServiceRegistration<?>[] regs = new HolaRemoteServiceRegistration[] { reg };
                if (targets == null)
                    sendAddRegistrations(null, null, regs);
                else
                    for (int i = 0; i < targets.length; i++) {
                        sendAddRegistrations(targets[i], null, regs);
                    }
            }
        }
        return reg;

    }
    private static final String ADD_REGISTRATIONS = "handleAddRegistrations";
    protected void sendAddRegistrations(ID receiver, Integer requestId,
        HolaRemoteServiceRegistration<?>[] regs) {
        try {
            sendSharedMessage(receiver, new SharedMessage(null,ADD_REGISTRATIONS,getLocalProviderID(),receiver,regs));
            if (receiver != null && requestId != null) {
                for (int i = 0; i < regs.length; i++)
                    addTargetForUnregister(regs[i], receiver);
            }
        } catch (IOException e) {
            LOG.error("Exception registration service",e);
        }
       
    }

    /**
     * @param holaRemoteServiceRegistration
     * @param receiver
     */
    private void addTargetForUnregister(
        HolaRemoteServiceRegistration<?> serviceRegistration, ID targetID) {
        List<ID> existingTargets = localRegistryUnregistrationTargets.get(serviceRegistration);
        if (existingTargets == null) {
            existingTargets = new ArrayList<ID>();
        }
        existingTargets.add(targetID);
        localRegistryUnregistrationTargets.put(serviceRegistration,
            existingTargets);

    }

    private final Map<HolaRemoteServiceRegistration<?>,List<ID>> localRegistryUnregistrationTargets = new HashMap<HolaRemoteServiceRegistration<?>,List<ID>>();

    /**
     * @param properties
     * @return
     */
    private ID[] getTargetsFromProperties(Dictionary<String, ?> properties) {
        if (properties == null)
            return null;
        List<ID> results = new ArrayList<ID>();
        Object o = properties.get(RemoteConstants.SERVICE_REGISTRATION_TARGETS);
        if (o != null) {
            if (o instanceof ID)
                results.add((ID) o);
            if (o instanceof ID[]) {
                ID[] targets = (ID[]) o;
                for (int i = 0; i < targets.length; i++)
                    results.add(targets[i]);
            }
        }
        if (results.size() == 0)
            return null;
        return results.toArray(new ID[] {});
    }
    /**
     * 检查服务是否为接口的实例
     * @param clazzes
     * @param service
     * @return
     */
    static String checkServiceClass(final String[] clazzes, final Object service) {
        final ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return service.getClass().getClassLoader();
            }
        });
        for (int i = 0; i < clazzes.length; i++) {
            try {
                final Class<?> serviceClazz = cl == null ? Class.forName(clazzes[i])
                    : cl.loadClass(clazzes[i]);
                if (!serviceClazz.isInstance(service)) {
                    return clazzes[i];
                }
            } catch (final ClassNotFoundException e) {
                // This check is rarely done
                if (extensiveCheckServiceClass(clazzes[i], service.getClass())) {
                    return clazzes[i];
                }
            }
        }
        return null;
    }
    /**
     * 检查父类和接口
     * @param clazz
     * @param serviceClazz
     * @return
     */
    static boolean extensiveCheckServiceClass(String clazz,
        Class<?> serviceClazz) {
        if (clazz.equals(serviceClazz.getName())) {
            return false;
        }
        final Class<?>[] interfaces = serviceClazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (!extensiveCheckServiceClass(clazz, interfaces[i])) {
                return false;
            }
        }
        final Class<?> superClazz = serviceClazz.getSuperclass();
        if (superClazz != null) {
            if (!extensiveCheckServiceClass(clazz, superClazz)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceReferences(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.core.identity.ID[], java.lang.String,
     *      java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getRemoteServiceReferences(ID target,
        ID[] idFilter, String clazz, String filter)
        throws InvalidSyntaxException, RemoteConnectException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceReferences(org.solmix.hola.core.identity.ID,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getRemoteServiceReferences(ID target,
        String clazz, String filter) throws InvalidSyntaxException,
        RemoteConnectException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getAllRemoteServiceReferences(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getAllRemoteServiceReferences(
        String clazz, String filter) throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceNamespace()
     */
    @Override
    public Namespace getRemoteServiceNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteServiceReference(org.solmix.hola.rs.identity.RemoteServiceID)
     */
    @Override
    public RemoteServiceReference<?> getRemoteServiceReference(
        RemoteServiceID serviceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#getRemoteService(org.solmix.hola.rs.RemoteServiceReference)
     */
    @Override
    public RemoteService getRemoteService(RemoteServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#ungetRemoteService(org.solmix.hola.rs.RemoteServiceReference)
     */
    @Override
    public boolean ungetRemoteService(RemoteServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#createRemoteFilter(java.lang.String)
     */
    @Override
    public RemoteFilter createRemoteFilter(String filter)
        throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#addRemoteServiceListener(org.solmix.hola.rs.RemoteServiceListener)
     */
    @Override
    public void addRemoteServiceListener(RemoteServiceListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProvider#removeRemoteServiceListener(org.solmix.hola.rs.RemoteServiceListener)
     */
    @Override
    public void removeRemoteServiceListener(RemoteServiceListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#connect(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.core.security.ConnectSecurityContext)
     */
    @Override
    public void connect(ID remoteID, ConnectSecurityContext securityContext)
        throws ConnectException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#getRemoteNamespace()
     */
    @Override
    public Namespace getRemoteNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#disconnect()
     */
    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#addListener(org.solmix.hola.core.ConnectListener)
     */
    @Override
    public void addListener(ConnectListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#removeListener(org.solmix.hola.core.ConnectListener)
     */
    @Override
    public void removeListener(ConnectListener listener) {
        // TODO Auto-generated method stub

    }

}
