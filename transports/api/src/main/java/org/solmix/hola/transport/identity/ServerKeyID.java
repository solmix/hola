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
package org.solmix.hola.transport.identity;

import org.solmix.runtime.identity.BaseID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年9月28日
 */

public class ServerKeyID extends BaseID
{

    private static final long serialVersionUID = -3557032805902095380L;

    private String protocol;
    private String host;
    private int port;
   
    public ServerKeyID(ServerKeyNamespace ns, String protocol, String host, Integer port)
    {
        super(ns);
        this.protocol=protocol;
        this.host=host;
        this.port=port;
    }


    @Override
    protected int namespaceCompareTo(BaseID o) {
        if(o==null ||!(o instanceof ServerKeyID)){
            return Integer.MIN_VALUE;
        }
        final ServerKeyID other = (ServerKeyID)o;
        int compare = port-other.port;
        if (compare == 0) {
            if(protocol!=null){
                compare = protocol.compareTo(other.protocol);
                if(compare==0){
                    return host.compareTo(other.host);
                }
            }else{
                if(other.protocol!=null){
                    return -1;
                }else{
                    return host.compareTo(other.host);
                }
            }
        }
        return -1;
    }

   
    @Override
    protected boolean namespaceEquals(BaseID o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof ServerKeyID)) {
            return false;
        }
        ServerKeyID other = (ServerKeyID) o;
        if (port==other.port) {
            if(protocol!=null){
                if(protocol.equals(other.protocol)){
                    return host.equals(other.host);
                }
            }else{
               if(other.protocol!=null){
                   return false;
               }else{
                   return host.equals(other.host);
               }
            }
        }
        return false;
    }

    
    @Override
    protected String namespaceGetName() {
        StringBuilder sb = new StringBuilder();
        if(protocol!=null){
            sb.append(protocol).append("://");
        }
        if(host!=null){
            sb.append(host);
        }else{
            sb.append("localhost");
        }
        sb.append(":").append(port);
        return sb.toString();
    }

   
    @Override
    protected int namespaceHashCode() {
        int iTotal=17,iConstant=37;
        iTotal = iTotal * iConstant + port;
        iTotal = iTotal * iConstant + protocol==null?0:protocol.hashCode();
        iTotal = iTotal * iConstant + host==null?0:host.hashCode();
        return iTotal;
    }

}
