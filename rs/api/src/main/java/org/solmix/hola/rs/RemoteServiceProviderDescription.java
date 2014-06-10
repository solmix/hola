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
package org.solmix.hola.rs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月28日
 */

public class RemoteServiceProviderDescription
{
    
    private static final Logger LOG= LoggerFactory.getLogger(RemoteServiceProviderDescription.class.getName());
    private boolean server;
    
    private String description;
    
    private String factoryClass;
    private String name;
    
    private RemoteServiceProviderFactory factory;

    /**
     * @return
     */
    public boolean isServer() {
        return server;
    }
    
    protected RemoteServiceProviderFactory getFactory()  {
        synchronized (this) {
            if (factory == null)
                try {
                    initializeFactory();
                } catch (Exception e) {
                    LOG.error("Can't initialize RemoteServiceProviderFactory,className="
                        + factoryClass);
                }
            return factory;
        }
    }

    private void initializeFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        Class<?> clz=Class.forName(factoryClass);
        Object o=clz.newInstance();
        if(o instanceof RemoteServiceProviderFactory){
            factory=(RemoteServiceProviderFactory)o;
        }else{
            throw new java.lang.IllegalArgumentException("the factory is not RemoteServiceProviderFactory instance");
        }
    }

    /**
     * @return
     */
    public String[] getSupportedConfigs() {
        RemoteServiceProviderFactory factory=  getFactory();
        return factory.getSupportedConfigs(this);
    }

    /**
     * @return
     */
    public String[] getSupportedIntents() {
        RemoteServiceProviderFactory factory=  getFactory();
        return factory.getSupportedIntents(this);
    }


    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    
    /**
     * @param server the server to set
     */
    public void setServer(boolean server) {
        this.server = server;
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param remoteSupportedConfigs
     * @return
     */
    public String[] getImportedConfigs(String[] remoteSupportedConfigs) {
       if(remoteSupportedConfigs==null)
           return null;
        return  getFactory().getImportedConfigs(this,remoteSupportedConfigs);
    }

    

}
