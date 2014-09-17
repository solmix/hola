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

package org.solmix.hola.discovery.event;

import java.util.EventObject;

import org.solmix.hola.discovery.ServiceListener;
import org.solmix.hola.discovery.ServiceInfo;

/**
 * 服务监听事项,在{@link ServiceListener}中使用
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月5日
 */

public class ServiceEvent extends EventObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 5274548617761559601L;
    private final ServiceInfo metadata;

    public ServiceEvent(Object source,ServiceInfo metadata)
    {
        super(source);
        this.metadata = metadata;
    }

    /**
     * 返回服务元数据标识
     * 
     * @return
     */
    public ServiceInfo getServiceMetadata() {
        return metadata;
    }
}
