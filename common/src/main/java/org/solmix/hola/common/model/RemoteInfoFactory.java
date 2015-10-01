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
package org.solmix.hola.common.model;

import java.util.Dictionary;

import org.solmix.commons.collections.DataTypeMap;
import org.solmix.exchange.EndpointInfoFactory;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.model.ServiceInfo;
import org.solmix.hola.common.HOLA;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月20日
 */

public class RemoteInfoFactory implements EndpointInfoFactory {

    private final boolean server;
    public RemoteInfoFactory(boolean server){
        this.server=server;
    }
    @Override
    public EndpointInfo createEndpointInfo(Container container,
        ServiceInfo serviceInfo, ProtocolInfo b,  Dictionary<String, ?>  configObject) {
        if(configObject instanceof ServiceProperties){
            DataTypeMap config = new DataTypeMap((ServiceProperties)configObject);
            RemoteServiceInfo rei = new RemoteServiceInfo(serviceInfo, getTransporter(config));
            setupTransporterInfo(rei,config);
            return rei;
        }else {
            throw new IllegalArgumentException("Rpc endpoint configuration object must be DataTypeMap");
        }
    }

    
    
    protected void setupTransporterInfo(RemoteServiceInfo rei, DataTypeMap config) {
        /*if(server){
            TransportServerInfo tsi = new TransportServerInfo();
            tsi.setBufferSize(config.getInt(HOLA.BUFFER_KEY, HOLA.DEFAULT_BUFFER_SIZE));
            tsi.setHost(config.getString(HOLA.HOST_KEY));
            tsi.setPort(config.getInt(HOLA.PORT_KEY));
            tsi.setThreadPoolSize(config.getInt(HOLA.THREADS_KEY, HOLA.DEFAULT_THREADS));
            tsi.setWaiteSuccess(config.getBoolean(HOLA.WAIT_KEY, HOLA.DEFAULT_WAIT));
            tsi.setPath(config.getString(HOLA.PATH_KEY));
            rei.addExtension(tsi);
        }else{
            TransportClientInfo tci = new TransportClientInfo();
            tci.setBufferSize(config.getInt(HOLA.BUFFER_KEY, HOLA.DEFAULT_BUFFER_SIZE));
            tci.setReceiveTimeout(config.getInt(HOLA.TIMEOUT_KEY, HOLA.DEFAULT_RECEIVE_TIMEOUT));
            tci.setConnectionTimeout(config.getInt(HOLA.CONNECT_TIMEOUT_KEY, HOLA.DEFAULT_CONNECT_TIMEOUT));
            rei.addExtension(tci);
        }*/
        
    }


    protected String getTransporter(DataTypeMap config) {
        return config.getString(HOLA.TRANSPORTER_KEY, HOLA.DEFAULT_RPC_TRANSPORTER);
    }

}
