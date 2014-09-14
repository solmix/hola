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
package org.solmix.hola.core.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月14日
 */

public class InfoUtils
{
    private static final Logger LOG= LoggerFactory.getLogger(InfoUtils.class);
    public static RemoteInfo merge(RemoteInfo target,RemoteInfo src){
        try {
            if(src==null)
                throw new IllegalArgumentException("src is null");
            if(target==null)
                throw new IllegalArgumentException("target is null");
            Map<String,Object> properties= new HashMap<String,Object>();
            properties.putAll(src.getProperties());
            properties.putAll(target.getProperties());
          return new RemoteInfo(properties);
        } catch (Exception e) {
           LOG.error("Merge configuration info failed:",e);
           throw new IllegalStateException(e);
        }
    }

}
