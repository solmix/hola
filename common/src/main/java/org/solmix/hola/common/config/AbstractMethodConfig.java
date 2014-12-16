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

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月28日
 */

public class AbstractMethodConfig extends AbstractHolaConfig {

    private static final long serialVersionUID = -6697447901643164112L;

    /**
     * 远程调用超时时间(毫秒)
     */
    protected Integer timeout;

    /**
     * 重试次数
     */
    protected Integer retries;

    /**
     * 最大并发调用
     */
    protected Integer actives;

    /**
     * 负载均衡算法
     */
    protected String loadbalance;

    /**
     * 异步标志
     */
    protected Boolean async;

    protected Boolean asyncwait;

    /** 服务连接数,0表示共享连接 */
    protected Integer connections;

    /**
     * @return the asyncwait
     */
    public Boolean isAsyncwait() {
        return asyncwait;
    }

    /**
     * @param asyncwait the asyncwait to set
     */
    public void setAsyncwait(Boolean asyncwait) {
        this.asyncwait = asyncwait;
    }

    /**
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the retries
     */
    public Integer getRetries() {
        return retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    /**
     * @return the actives
     */
    public Integer getActives() {
        return actives;
    }

    /**
     * @param actives the actives to set
     */
    public void setActives(Integer actives) {
        this.actives = actives;
    }

    /**
     * @return the loadbalance
     */
    public String getLoadbalance() {
        return loadbalance;
    }

    /**
     * @param loadbalance the loadbalance to set
     */
    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    /**
     * @return the async
     */
    public Boolean isAsync() {
        return async;
    }

    /**
     * @param async the async to set
     */
    public void setAsync(Boolean async) {
        this.async = async;
    }

    /**   */
    public Integer getConnections() {
        return connections;
    }

    /**   */
    public void setConnections(Integer connections) {
        this.connections = connections;
    }
}
