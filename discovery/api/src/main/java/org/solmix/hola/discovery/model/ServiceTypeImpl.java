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

package org.solmix.hola.discovery.model;

import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.runtime.identity.IDCreateException;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public class ServiceTypeImpl implements ServiceType
{

    private static final long serialVersionUID = 6751472077455908271L;

    private String typeName = "";

    private String group;

    private final String serviceInterface;

    private final String category;

    public ServiceTypeImpl(String serviceInterface, String group,String category)
    {
        this.group = group;
        this.serviceInterface=serviceInterface;
        this.category = category;
        encodeType();
        Assert.isNotNull(typeName);
    }

    public ServiceTypeImpl( ServiceType type)
    {
        this(type.getServiceInterface(), type.getGroup(), type.getCategory());
    }
    
    public static ServiceTypeImpl fromAddress(String address){
        if (address == null)
            throw new IDCreateException("Service Type Name is null");
        try {
            address = address.trim();
            
            if (address.endsWith(HOLA.PATH_SEPARATOR)) {
                address = address.substring(0, address.length() - 1);
            }
            if (address.startsWith(HOLA.PATH_SEPARATOR)){
                address = address.substring(1);
            }
            int last = address.lastIndexOf('/');
            int first=address.indexOf(HOLA.PATH_SEPARATOR);
            String category=address.substring(last, address.length());
            String group=address.substring(0,first);
            String serviceInterface=address.substring(first,last);
        
        ServiceTypeImpl  st = new ServiceTypeImpl(serviceInterface,group,category);
        st.encodeType();
        return st;
        }catch (Exception e) {
            throw new IDCreateException("service type not parseable", e);
        }
    }

    public ServiceTypeImpl(String serviceInterface)
    {
      this(serviceInterface,null,HOLA.PROVIDER_CATEGORY);

    }

    /**
     * 
     */
    private void encodeType() {
        final StringBuffer buf = new StringBuffer();
        if (!DataUtils.isEmpty(group)) {
            buf.append(group).append(HOLA.PATH_SEPARATOR);
        }
        if (!DataUtils.isEmpty(serviceInterface)) {
           buf .append(serviceInterface);
        }
        if(!DataUtils.isEmpty(category)){
            buf.append(HOLA.PATH_SEPARATOR).append(category);
        }
        
        typeName = buf.toString();

    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ServiceType){
            return getIdentityName().equals(((ServiceType)obj).getIdentityName());
        }
        return false;
    }
    
    @Override
    public String toString(){
        return getIdentityName();
    }
    
    @Override
    public int hashCode(){
        return getIdentityName().hashCode();
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getServiceInterface() {
        return serviceInterface;
    }

    @Override
    public String getServiceName() {
        final StringBuffer buf = new StringBuffer();
        if (!DataUtils.isEmpty(group)) {
            buf.append(group).append(HOLA.PATH_SEPARATOR);
        }
        if (!DataUtils.isEmpty(serviceInterface)) {
           buf .append(serviceInterface);
        }
        return buf.toString();
    }

    @Override
    public String getIdentityName() {
        return typeName;
    }

}
