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
package org.solmix.hola.rt.config;

import java.util.Arrays;
import java.util.List;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月9日
 */

public class AbstractServiceConfig extends AbstractClassConfig
{

    /**
     * 
     */
    private static final long serialVersionUID = 90513768239318002L;
    // 延迟暴露
    protected Integer              delay;

    // 是否暴露
    protected Boolean              export;

    // 权重
    protected Integer              weight;

    // 应用文档
    protected String               document;

    protected List<ProtocolConfig> protocols;
    /**
     * @return the delay
     */
    public Integer getDelay() {
        return delay;
    }

    
    /**
     * @param delay the delay to set
     */
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    
    /**
     * @return the export
     */
    public Boolean getExport() {
        return export;
    }

    
    /**
     * @param export the export to set
     */
    public void setExport(Boolean export) {
        this.export = export;
    }

    
    /**
     * @return the weight
     */
    public Integer getWeight() {
        return weight;
    }

    
    /**
     * @param weight the weight to set
     */
    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    
    /**
     * @return the document
     */
    public String getDocument() {
        return document;
    }
    
    /**
     * @param document the document to set
     */
    public void setDocument(String document) {
        this.document = document;
    }

    /**
     * @return the protocols
     */
    public List<ProtocolConfig> getProtocols() {
        return protocols;
    }
    public ProtocolConfig getProtocol() {
        return protocols==null||protocols.size()==0?null:protocols.get(0);
    }
    public void setProtocol(ProtocolConfig protocol) {
        this.protocols =  Arrays.asList(new ProtocolConfig[] {protocol});
    }
    /**
     * @param protocols the protocols to set
     */
    public void setProtocols(List<ProtocolConfig> protocols) {
        this.protocols = protocols;
    }
    
    
}