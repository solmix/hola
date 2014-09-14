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

package org.solmix.hola.transport.exchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.hola.transport.TransportException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月17日
 */

public class ReplierDispatcher implements Replier<Object>
{

    private final Replier<?> defaultReplier;

    private final Map<Class<?>, Replier<?>> repliers = new ConcurrentHashMap<Class<?>, Replier<?>>();

    public ReplierDispatcher()
    {
        this(null, null);
    }

    public ReplierDispatcher(Replier<?> defaultReplier)
    {
        this(defaultReplier, null);
    }

    public ReplierDispatcher(Replier<?> defaultReplier,
        Map<Class<?>, Replier<?>> repliers)
    {
        this.defaultReplier = defaultReplier;
        if (repliers != null && repliers.size() > 0) {
            this.repliers.putAll(repliers);
        }
    }

    public <T> ReplierDispatcher addReplier(Class<T> type, Replier<T> replier) {
        repliers.put(type, replier);
        return this;
    }

    public <T> ReplierDispatcher removeReplier(Class<T> type) {
        repliers.remove(type);
        return this;
    }

    private Replier<?> getReplier(Class<?> type) {
        for (Map.Entry<Class<?>, Replier<?>> entry : repliers.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }
        if (defaultReplier != null) {
            return defaultReplier;
        }
        throw new IllegalStateException(
            "Replier not found, Unsupported message object: " + type);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object reply(ExchangeChannel channel, Object request)
        throws TransportException {
        return ((Replier) getReplier(request.getClass())).reply(channel,
            request);
    }

}
