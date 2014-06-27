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

import java.util.Hashtable;
import java.util.Queue;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.shared.SharedChannel;
import org.solmix.hola.shared.SharedConnetor;
import org.solmix.hola.shared.SharedServiceEvent;
import org.solmix.runtime.event.Event;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年6月23日
 */

public class GenericSharedConnetor implements SharedConnetor
{

    private final ID senderID;

    private Hashtable<ID, Queue<Event>> queues;

    /**
     * @param sendId
     * @param queues
     */
    public GenericSharedConnetor(ID sendId, Hashtable<ID, Queue<Event>> queues)
    {
        this.senderID = sendId;
        this.queues = queues;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedConnetor#getChannels()
     */
    @Override
    public SharedChannel[] getChannels() {
        // TODO Auto-generated method stub
        return null;
    }

    public void enqueue(SharedServiceEvent event) throws Exception {
        for (Queue<Event> queue : queues.values()) {
            queue.offer(event);
        }
    }

    public void enqueue(SharedServiceEvent[] events) throws Exception {
        for (Queue<Event> queue : queues.values()) {
            for (SharedServiceEvent event : events) {
                queue.offer(event);
            }
        }
    }
    
    /**
     * @return the senderID
     */
    public ID getSenderID() {
        return senderID;
    }

    public void dispose() {
        if (queues != null) {
            queues.clear();
            queues = null;
        }
        queues = null;
    }
}
