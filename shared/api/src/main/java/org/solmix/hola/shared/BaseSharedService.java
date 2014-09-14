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

package org.solmix.hola.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Identifiable;
import org.solmix.runtime.SystemContext;
import org.solmix.runtime.SystemContextFactory;
import org.solmix.runtime.adapter.AdapterManager;
import org.solmix.runtime.event.Event;
import org.solmix.runtime.event.EventProcessor;

/**
 * 基础服务类,可通过继承该类来扩展.
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月17日
 */

public class BaseSharedService implements SharedService, Identifiable
{

    private SharedServiceConfig config;

    private final List<EventProcessor> eventProcessors = new Vector<EventProcessor>();

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public void initialize() {

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.adapter.Adaptable#adaptTo(java.lang.Class)
     */
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type.isInstance(this))
            return type.cast(this);
        SystemContext sc = SystemContextFactory.getThreadDefaultSystemContext();
        if (sc != null) {
            AdapterManager am = sc.getExtension(AdapterManager.class);
            if (am == null)
                return null;
            return am.getAdapter(this, type);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedService#init(org.solmix.hola.shared.SharedServiceConfig)
     */
    @Override
    public void init(SharedServiceConfig serviceConfig) {
        this.config = serviceConfig;
        initialize();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedService#handleEvent(org.solmix.runtime.event.Event)
     */
    @Override
    public void handleEvent(Event event) {
        if (LOG.isTraceEnabled())
            LOG.trace("method->handleEvent");
        fireEventProcessors(event);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedService#handleEvents(org.solmix.runtime.event.Event[])
     */
    @Override
    public void handleEvents(Event[] events) {
        if (LOG.isTraceEnabled())
            LOG.trace("method->handleEvent");
        if (events == null)
            return;
        for (Event event : events) {
            handleEvent(event);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Identifiable#getID()
     */
    @Override
    public ID getID() {
        return getConfig().getSharedServiceID();
    }

    protected final SharedServiceContext getContext() {
        return getConfig().getContext();
    }

    /**
     * @return the config
     */
    public final SharedServiceConfig getConfig() {
        return config;
    }

    protected final boolean isPrimary() {
        ID home = getHomeProviderID();
        ID local = getLocalProviderID();
        if (local == null || home == null)
            return false;
        return home.equals(local);
    }

    protected final ID getHomeProviderID() {
        return getConfig().getHomeProviderID();
    }

    protected final ID getLocalProviderID() {
        SharedServiceContext context = getContext();
        return context == null ? null : context.getLocalProviderID();
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedService#destroy()
     */
    @Override
    public void destroy() {
        eventProcessors.clear();

    }

    protected void fireEventProcessors(Event event) {
        if (event == null)
            return;
        Event evt = event;
        List<EventProcessor> notify = null;
        synchronized (eventProcessors) {
            notify = new ArrayList<EventProcessor>(eventProcessors);
        }
        if (notify.size() == 0) {
            handleUnhandledEvent(evt);
            return;
        }
        for (Iterator<EventProcessor> i = notify.iterator(); i.hasNext();) {
            EventProcessor ep = i.next();
            if (ep.process(evt))
                break;
        }
    }

    /**
     * @param event
     */
    protected void handleUnhandledEvent(Event evt) {
        LOG.warn("Unhandled Event ->" + evt);
    }

    public void clearEventProcessor() {
        synchronized (eventProcessors) {
            eventProcessors.clear();
        }
    }

    public boolean removeEventProcessor(EventProcessor proc) {
        Assert.isNotNull(proc);
        synchronized (eventProcessors) {
            return eventProcessors.remove(proc);
        }
    }

    public boolean addEventProcessor(EventProcessor proc) {
        Assert.isNotNull(proc);
        synchronized (eventProcessors) {
            return eventProcessors.add(proc);
        }
    }
    protected final boolean isConnected() {
        return (getTargetID() != null);
  }
    public ID getTargetID() {
        SharedServiceContext context = getContext();
        return context == null ? null : context.getTargetID();
  }
    /**
     * @param receiver
     * @param create
     * @throws IOException 
     */
    protected void sendSharedMessage(ID target, SharedMessage message) throws IOException {
        SharedServiceContext context= getContext();
        if(context!=null){
            Assert.isNotNull(message,"message must be not null");
            context.sendMessage(target,message);
        }else{
            LOG.trace("No SharedServiceContext no message send");
        }
    }
}
