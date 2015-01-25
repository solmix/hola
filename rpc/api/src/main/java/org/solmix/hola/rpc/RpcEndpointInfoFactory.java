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
package org.solmix.hola.rpc;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.hola.common.Params;
import org.solmix.hola.rpc.support.ServiceProperties;
import org.solmix.hola.transport.TransportClientInfo;
import org.solmix.hola.transport.TransportServerInfo;
import org.solmix.runtime.Container;
import org.solmix.runtime.exchange.EndpointInfoFactory;
import org.solmix.runtime.exchange.model.EndpointInfo;
import org.solmix.runtime.exchange.model.ProtocolInfo;
import org.solmix.runtime.exchange.model.ServiceInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月20日
 */

public class RpcEndpointInfoFactory implements EndpointInfoFactory {

    private final boolean server;
    public RpcEndpointInfoFactory(boolean server){
        this.server=server;
    }
    @Override
    public EndpointInfo createEndpointInfo(Container container,
        ServiceInfo serviceInfo, ProtocolInfo b, Object configObject) {
        if(configObject instanceof ServiceProperties){
            DataTypeMap config = new DataTypeMap((ServiceProperties)configObject);
            RpcEndpointInfo rei = new RpcEndpointInfo(serviceInfo, getTransporter(config));
            setupTransporterInfo(rei,config);
            return rei;
        }else {
            throw new IllegalArgumentException("Rpc endpoint configuration object must be DataTypeMap");
        }
    }

    
    
    protected void setupTransporterInfo(RpcEndpointInfo rei, DataTypeMap config) {
        if(server){
            TransportServerInfo tsi = new TransportServerInfo();
            tsi.setBufferSize(config.getInt(Params.BUFFER_KEY, Params.DEFAULT_BUFFER_SIZE));
            tsi.setHost(config.getString(Params.HOST_KEY));
            tsi.setPort(config.getInt(Params.PORT_KEY));
            tsi.setThreadPoolSize(config.getInt(Params.THREADS_KEY, Params.DEFAULT_THREADS));
            tsi.setWaiteSuccess(config.getBoolean(Params.WAIT_KEY, Params.DEFAULT_WAIT));
            rei.addExtension(tsi);
        }else{
            TransportClientInfo tci = new TransportClientInfo();
            tci.setBufferSize(config.getInt(Params.BUFFER_KEY, Params.DEFAULT_BUFFER_SIZE));
            tci.setReceiveTimeout(config.getInt(Params.TIMEOUT_KEY, Params.DEFAULT_RECEIVE_TIMEOUT));
            tci.setConnectionTimeout(config.getInt(Params.CONNECT_TIMEOUT_KEY, Params.DEFAULT_CONNECT_TIMEOUT));
            rei.addExtension(tci);
        }
        
    }


    protected String getTransporter(DataTypeMap config) {
        return config.getString(Params.TRANSPORTER_KEY, Params.DEFAULT_RPC_TRANSPORTER);
    }

}
