/*
 * Copyright 2014 The Solmix Project
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

package org.solmix.hola.discovery.jmdns;

import java.util.Dictionary;

import javax.annotation.Resource;

import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年9月14日
 */
@Extension(name = JmDNSProvider.NAME)
public class JmDNSProvider implements DiscoveryProvider
{

    public static final String NAME = "jmdns";

    @Resource
    private Container container;

    @Override
    public Discovery createDiscovery(Dictionary<String, ?> info) throws DiscoveryException {
        return new JmDNSDiscovery(info, container);
    }

}
