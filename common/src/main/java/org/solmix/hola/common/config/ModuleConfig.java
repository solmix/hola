/**
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
package org.solmix.hola.common.config;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年10月24日
 */

public class ModuleConfig extends AbstractHolaConfig
{

    private static final long serialVersionUID = 8499637514191699612L;
    
    private String name;

    private String version;

    private String owner;

    private String organization;

    private List<DiscoveryConfig> discoveries;

    private MonitorConfig monitor;

    private Boolean  isDefault;
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
        checkName("name", name);
        this.name = name;
        if (id == null || id.length() == 0) {
            id = name;
        }
    }

    
    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    
    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        checkMultiName("owner", owner);
        this.owner = owner;
    }

    
    /**
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }

    
    /**
     * @param organization the organization to set
     */
    public void setOrganization(String organization) {
        checkName("organization", organization);
        this.organization = organization;
    }


    
    /**
     * @return the discoveries
     */
    public List<DiscoveryConfig> getDiscoveries() {
        return discoveries;
    }

    
    /**
     * @param discoveries the discoveries to set
     */
    public void setDiscoveries(List<DiscoveryConfig> discoveries) {
        this.discoveries = discoveries;
    }
    
    public DiscoveryConfig getDiscovery(){
        return discoveries == null || discoveries.size() == 0 ? null
            : discoveries.get(0);
    }
    
    public void setDiscovery(DiscoveryConfig discovery){
        List<DiscoveryConfig> discoveries= new ArrayList<DiscoveryConfig>(1);
        discoveries.add(discovery);
        this.discoveries=discoveries;
    }

    
    /**
     * @return the monitor
     */
    public MonitorConfig getMonitor() {
        return monitor;
    }

    
    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }
    
    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
