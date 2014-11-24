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

import java.util.List;
import java.util.Map;

import org.solmix.commons.annotation.ThreadSafe;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月22日
 */
@ThreadSafe
public class CopyOfEndpointInfo extends  ExtensionInfo<CopyOfEndpointInfo>
{

    private static final long serialVersionUID = 1630305446485770146L;
    public static final String SCOPE_KEY = "scope";
    
    public static final String SCOPE_NONE = "none";
    
    public static final String SCOPE_REMOTE = "remote";
    
    public static final String SCOPE_LOCAL = "local";
    
    /**
     * 是否公告服务
     */
    public static final String ADVERTISE_KEY = "advertise";
    public static final String INTERFACE_KEY = "interface";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String VERSION="hola";
    
    private List<DiscoveryInfo> discoveryInfos;

    private RemoteInfo remoteInfo;
    
    /**
     * @return the remoteInfo
     */
    public RemoteInfo getRemoteInfo() {
        return remoteInfo;
    }

    /**
     * @param remoteInfo the remoteInfo to set
     */
    public void setRemoteInfo(RemoteInfo remoteInfo) {
        this.remoteInfo = remoteInfo;
    }

    /**
     * @return the discoveryInfos
     */
    public List<DiscoveryInfo> getDiscoveryInfos() {
        return discoveryInfos;
    }

    /**
     * @param discoveryInfos the discoveryInfos to set
     */
    public void setDiscoveryInfos(List<DiscoveryInfo> discoveryInfos) {
        this.discoveryInfos = discoveryInfos;
    }

    public CopyOfEndpointInfo(Map<String, Object> properties)
    {
        super(properties);
    }

    public CopyOfEndpointInfo(){
        super(null);
    }
    /**
     * @param remoteInfo2
     */
    public CopyOfEndpointInfo(RemoteInfo remoteInfo)
    {
        this();
       this.remoteInfo=remoteInfo;
    }

    public Boolean getAdvertise(boolean dfValue){
        
       return remoteInfo==null?dfValue: remoteInfo.getBoolean(ADVERTISE_KEY,dfValue);
    }
    public String getScope(){
        return remoteInfo==null?null: remoteInfo.getString(SCOPE_KEY);
    }
    
    public String getRemoteProtocol(){
        return remoteInfo==null?null: remoteInfo.getString(RemoteInfo.PROTOCOL);
    }
    @Override
    protected CopyOfEndpointInfo getSelf() {
        return this;
    }

    @Override
    protected CopyOfEndpointInfo makeSelf(Map<String, Object> map) {
        return new CopyOfEndpointInfo(map);
    }
}
