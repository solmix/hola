/*
 * Copyright 2015 The Solmix Project
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
package org.solmix.hola.rs;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.solmix.commons.util.ArrayUtils;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.support.AbstractProtocolFactory;
import org.solmix.hola.common.model.ConfigSupportedReference;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月28日
 */

public abstract class ProtocolFactorySupport extends AbstractProtocolFactory
{

    /**把ConfigSupportedReference的配置放入extension中*/
    protected  void makeConfigAsEndpointInfoExtension(
        ConfigSupportedReference config, 
        ProtocolInfo protocolInfo,
        Dictionary<String, ?> properties) {
        String[] supported = config.getSupportedConfigs(properties);
        Class<?> clazz = config.getSupportedConfigClass();
        if(!ArrayUtils.isEmptyArray(supported)&&clazz!=null){
            try {
                Object bean = Reflection.newInstance(clazz);
                Map<String,Object> copyed = new HashMap<String,Object>();
                for(String key:supported){
                    Object value = properties.get(key);
                    if(value!=null){
                        copyed.put(key, properties.get(key));
                    }
                }
                DataUtils.setProperties(copyed, bean, false);
                protocolInfo.addExtension(bean);
            } catch (Exception e) {
                getLogger().warn("Make ConfigSupportedReference into EndpointInfo extensions",e);
            }
        }
    }
    
    public abstract Logger getLogger();

}
