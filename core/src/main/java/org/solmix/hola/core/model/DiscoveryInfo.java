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
package org.solmix.hola.core.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.HolaConstants;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月14日
 */

public class DiscoveryInfo extends EndpointInfo
{
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryInfo.class);
    /**
     * @param properties
     */
    public DiscoveryInfo(Map<String, Object> properties)
    {
        super(properties);
    }
    
    public DiscoveryInfo()
    {
        this(null);
    }
    
    public URI getURI(){
        String address=getAddress();
        try {
            URI uri= new URI(address);
            return uri;
        } catch (URISyntaxException e) {
           LOG.error("Can't parse uri string:"+address+"to URI",e);
        }
        return null;
    }
    public String getAddress(){
        return getString("address");
    }

    /**
     * @return
     */
    public String getBackupAddress() {
       StringBuilder sb = new StringBuilder();
       sb.append(getAddress());
       String backup= getString("backup", null);
       if(backup!=null&&backup.length()>0){
         String[] backs=  HolaConstants.SPLIT_COMMA_PATTERN.split(backup);
         for(String back:backs){
             sb.append(",");
             sb.append(back);
         }
       }
        return sb.toString();
    }
    
    public String getAuthority() {
        String username=getString("username");
        String password=getString("password");
        if ((username == null || username.length() == 0)
                && (password == null || password.length() == 0)) {
            return null;
        }
        return (username == null ? "" : username) 
                + ":" + (password == null ? "" : password);
    }
    
    public String getGroup(String df){
        return getString("group", df);
    }

    /**
     * @param retryPeriod
     * @return
     */
    public int getRetyPeriod(int retryPeriod) {
        return getInt("retry.peroid", retryPeriod);
    }

    /**
     * 启动时是否检查错误
     * 
     * @param b
     */
    public boolean getCheck(boolean b) {
        return getBoolean("check", b);
        
    }
}
