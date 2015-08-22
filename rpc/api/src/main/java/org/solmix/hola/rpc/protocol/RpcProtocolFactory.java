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

package org.solmix.hola.rpc.protocol;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.exchange.model.SerializationInfo;
import org.solmix.exchange.model.ServiceInfo;
import org.solmix.exchange.support.AbstractProtocolFactory;
import org.solmix.hola.common.Params;
import org.solmix.hola.rpc.support.ServiceProperties;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月19日
 */

public abstract class RpcProtocolFactory extends AbstractProtocolFactory {

    public RpcProtocolFactory(Container container) {
        super(container);
    }

    protected void setProperties(RpcProtocolInfo pi, Object configObject) {
         if (configObject instanceof ServiceProperties) {
            setMapProperties(pi,new DataTypeMap((ServiceProperties)configObject));
        } else {
            throw new IllegalArgumentException(
                "Unsuport protocol configure object "
                    + configObject.getClass().getName());
        }
        
    }
    
    protected void setMapProperties(RpcProtocolInfo pi,DataTypeMap map) {
        String  serialObject = map.getString(Params.SERIALIZATION_KEY, Params.DEFAULT_RPC_SERIALIZATION);
        if(serialObject!=null){
            pi.setSerializationInfo(new SerializationInfo(serialObject));
        }
    }

    public RpcProtocolInfo createRpcProtocolInfo(ServiceInfo info,
        String protocol) {
        return new RpcProtocolInfo(info, protocol);
    }
}
