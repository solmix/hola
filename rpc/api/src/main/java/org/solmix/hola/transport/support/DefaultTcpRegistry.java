/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.transport.support;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.solmix.hola.transport.AbstractTCPTransporter;
import org.solmix.hola.transport.TransporterRegistry;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月18日
 */

public class DefaultTcpRegistry implements TransporterRegistry {

    private final ConcurrentMap<String, AbstractTCPTransporter> transporters = new ConcurrentHashMap<String, AbstractTCPTransporter>();

    @Override
    public void add(AbstractTCPTransporter transporter) {
        String path = getRealPath(transporter.getAddress());
        AbstractTCPTransporter t = transporters.putIfAbsent(path, transporter);
        if (t != null && t != transporter) {
            throw new RuntimeException("Already a Transporter on " + path);
        }
    }

    private String getRealPath(String address) {
        if (address == null) {
            return "/";
        }
        String path = address;
        if (address.indexOf("://") != -1) {
            path = address.substring(address.indexOf("://") + 1);
        }
        if (path.startsWith("localhost/")) {
            path = path.substring("localhost/".length());
        }
        if (!path.contains("://") && !path.startsWith("/")) {
            path = "/" + path;

        }
        return path;
    }

    @Override
    public void remove(String path) {
        transporters.remove(path);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.TransporterRegistry#getTransporterForPath(java.lang.String)
     */
    @Override
    public AbstractTCPTransporter getTransporterForPath(String path) {
        path = getRealPath(path);
        return transporters.get(path);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.TransporterRegistry#getTransporters()
     */
    @Override
    public Collection<AbstractTCPTransporter> getTransporters() {
        return Collections.unmodifiableCollection(transporters.values());
    }

}
