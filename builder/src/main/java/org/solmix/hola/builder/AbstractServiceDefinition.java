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

package org.solmix.hola.builder;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月28日
 */

public class AbstractServiceDefinition extends AbstractInterfaceDefinition {

    private static final long serialVersionUID = -3045082427456103750L;

    /**
     * 服务版本
     */
    protected String version;

    /**
     * 服务分组
     */
    protected String group;

    /**
     * 服务权重
     */
    protected Integer weight;

    /**
     * 文档地址
     */
    protected String document;

    /**
     * 是否为动态服务
     */
    protected Boolean dynamic;

    /**
     * 允许的请求次数
     */
    private Integer executes;

    /**
     * <li>服务是否允许暴露
     */
    private Boolean publish;

    /**
     * <li>延迟发布时间(ms)
     */
    protected Integer delay;
    
    /**
     * 最大接收连接数
     */
    private Integer accepts;
    

    /**   */
    public Integer getAccepts() {
        return accepts;
    }

    /**   */
    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }
    /**   */
    public Boolean isPublish() {
        return publish;
    }

    /**   */
    public void setPublish(Boolean publish) {
        this.publish = publish;
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

    /**   */
    public Boolean isDynamic() {
        return dynamic;
    }

    /**   */
    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
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
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return the executes
     */
    public Integer getExecutes() {
        return executes;
    }

    /**
     * @param executes the executes to set
     */
    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    
    public Integer getDelay() {
        return delay;
    }

    
    public void setDelay(Integer delay) {
        this.delay = delay;
    }
    
}
