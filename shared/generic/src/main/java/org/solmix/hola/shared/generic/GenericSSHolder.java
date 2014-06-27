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

package org.solmix.hola.shared.generic;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.event.ConnectedEvent;
import org.solmix.hola.core.event.DisconnectedEvent;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.shared.SharedService;
import org.solmix.hola.shared.event.SharedServiceActivatedEvent;
import org.solmix.hola.shared.event.SharedServiceDeactivatedEvent;
import org.solmix.hola.shared.generic.member.Member;
import org.solmix.runtime.event.Event;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月18日
 */

public class GenericSSHolder
{

    protected final SharedService sharedService;

    private final GenericSSConfig serviceConfig;

    private final ID serviceID;

    private final ID providerID;

    private final Queue<Event> queue;

    private Thread thread;

    private GenericSSProvider provider;

    private static final Logger LOG = LoggerFactory.getLogger(GenericSSHolder.class.getName());

    /**
     * @param createServiceConfig
     * @param sharedService
     * @param genericProvider
     */
    public GenericSSHolder(GenericSSConfig serviceConfig,
        SharedService sharedService, GenericSSProvider provider)
    {
        this.sharedService = sharedService;
        this.serviceConfig = serviceConfig;
        this.serviceID = serviceConfig.getSharedServiceID();
        this.providerID = provider.getID();
        queue = new LinkedBlockingQueue<Event>();
    }

    /**
     * 
     */
    public void init() throws Exception {
        serviceConfig.active(queue);
        sharedService.init(serviceConfig);

    }

    /**
     * @param m
     * @param add
     */
    public void memberChanged(Member m, boolean add) {
        //already activate
       if(thread!=null){
           if(add){
               send(new ConnectedEvent(this, providerID, m.getID()));
           }else{
               send(new DisconnectedEvent(this, providerID, m.getID()));
           }
       }
    }

    /**
     * @return
     */
    public ID getServiceID() {
        return serviceConfig.getSharedServiceID();
    }

    /**
     * @return
     */
    public Object getLocalProviderID() {
        return providerID;
    }
    
    /**
     * @return the queue
     */
    public Queue<Event> getQueue() {
        return queue;
    }

    /**
     * 
     */
    public void activated() {
        thread = AccessController.doPrivileged(new PrivilegedAction<Thread>() {

            @Override
            public Thread run() {
                Thread aThread = getThread();
                return aThread;
            }
        });
        provider.notifyServiceActivated(serviceID);
        thread.start();
    }

    /**
     * @return
     */
    protected Thread getThread() {
        return provider.getNewServiceThread(serviceID, new Runnable() {

            @Override
            public void run() {
                Event evt = null;
                for (;;) {
                    if (Thread.currentThread().isInterrupted())
                        break;
                    evt = queue.peek();
                    if (Thread.currentThread().isInterrupted() || evt == null)
                        break;
                    if (evt instanceof DestroyEvent) {
                        GenericSSHolder.this.destroy();
                    } else {
                        GenericSSHolder.this.handleEvent(evt);
                    }
                }
                if (LOG.isTraceEnabled()) {
                    if (Thread.currentThread().isInterrupted()) {
                        LOG.trace("runner(" + serviceID
                            + ") terminating interrupted");
                    } else {
                        LOG.trace("runner(" + serviceID
                            + ") terminating normally");
                    }
                }
            }
        });
    }

    /**
     * @param evt
     */
    protected void handleEvent(Event evt) {
        this.sharedService.handleEvent(evt);
    }

    protected void sendEvent(Event event) {
        this.queue.offer(event);
    }

    /**
     * 
     */
    protected void destroy() {

        this.serviceConfig.inactive();

    }
    public void otherChanged(ID serviceID, boolean activated) {
        if (activated && thread != null) {
            send(new SharedServiceActivatedEvent(providerID, serviceID));
      } else {
            send(new SharedServiceDeactivatedEvent(providerID, serviceID));
      }
}
    private void send(Event evt) {
        queue.offer(evt);
  }
    protected static class DestroyEvent implements Event
    {

        DestroyEvent()
        {
            //
        }
    }

    public void deactivated() {
        provider.notifyServiceDeactivated(serviceID);
        // XXX fire SharedServiceDeactiveEvent
    }
}
