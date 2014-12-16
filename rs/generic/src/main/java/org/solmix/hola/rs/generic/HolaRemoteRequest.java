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
import java.io.ObjectInput;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ClassDescUtils;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.rs.RemoteRequest;
import org.solmix.hola.rs.support.RemoteRequestImpl;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.codec.Decodeable;
import org.solmix.hola.transport.exchange.Request;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月25日
 */

public class HolaRemoteRequest extends RemoteRequestImpl implements RemoteRequest,
    Decodeable
{

    private static final Logger log = LoggerFactory.getLogger(HolaRemoteRequest.class);

    private final Channel channel;

    private final Request request;

    private final ObjectInput in;

    private volatile boolean decoded;

    public HolaRemoteRequest(Channel channel, Request request, ObjectInput in)
    {
        this.channel = channel;
        this.request = request;
        this.in = in;
    }

    private static final long serialVersionUID = 6259515257091947280L;

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Decodeable#decode()
     */
    @Override
    public void decode() throws Exception {
        if (!HolaRemoteRequest.this.decoded && channel != null && in != null) {
            try {
                setProperty(RemoteInfo.PATH, in.readUTF());
                setProperty(RemoteInfo.VERSION, in.readUTF());
                setMethod(in.readUTF());
                Object[] args;
                Class<?>[] pts;
                String desc = in.readUTF();
                if (desc.length() == 0) {
                    pts = HolaCodec.EMPTY_CLASS_ARRAY;
                    args = HolaCodec.EMPTY_OBJECT_ARRAY;
                } else {
                    pts = ClassDescUtils.getType(desc);
                    args = new Object[pts.length];
                    for (int i = 0; i < args.length; i++) {
                        try {
                            args[i] = in.readObject();
                        } catch (Exception e) {
                            if (log.isWarnEnabled()) {
                                log.warn(
                                    "Decode argument failed: " + e.getMessage(),
                                    e);
                            }
                        }
                    }
                }
                setParameterTypes(pts);
                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>) in.readObject();
                if (map != null) {
                    setProperties(map);
                }
                setParameters(args);
            } catch (Exception e) {
                throw new IOException("Read invocation data failed.", e);
            } finally {
                HolaRemoteRequest.this.decoded = true;
            }
        }

    }

}
