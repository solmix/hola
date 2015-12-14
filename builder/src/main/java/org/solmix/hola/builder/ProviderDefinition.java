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

public class ProviderDefinition extends AbstractServiceDefinition {

    private static final long serialVersionUID = 7508462139160267039L;
   

    /**
     * <li>上下文路径
     */
    private String contextpath;

    /**
     * 线程池名称
     */
    private String executor;

    /**
     * 线程池大小
     */
    private Integer threads;

    /**
     * IO线程池大小
     */
    private Integer iothreads;

    /**
     * 线程池队列大小
     */
    private Integer queues;

   
    /**
     * 字符集
     */
    private String charset;

 


    /**
     * 信息线程模型派发方式
     */
    private String dispatcher;


    /**
     * 组网方式
     */
    private String networker;

    /**
     * 是否为默认设置
     */
    private Boolean isDefault;
    

    /**   */
    public String getContextpath() {
        return contextpath;
    }

    /**   */
    public void setContextpath(String contextpath) {
        checkPathName("contextpath", contextpath);
        this.contextpath = contextpath;
    }

    /**   */
    public String getExecutor() {
        return executor;
    }

    /**   */
    public void setExecutor(String executor) {
        this.executor = executor;
    }

    /**   */
    public Integer getThreads() {
        return threads;
    }

    /**   */
    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    /**   */
    public Integer getIothreads() {
        return iothreads;
    }

    /**   */
    public void setIothreads(Integer iothreads) {
        this.iothreads = iothreads;
    }

    /**   */
    public Integer getQueues() {
        return queues;
    }

    /**   */
    public void setQueues(Integer queues) {
        this.queues = queues;
    }


    /**   */
    public String getCharset() {
        return charset;
    }

    /**   */
    public void setCharset(String charset) {
        this.charset = charset;
    }


    /**   */
    public String getDispatcher() {
        return dispatcher;
    }

    /**   */
    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**   */
    public String getNetworker() {
        return networker;
    }

    /**   */
    public void setNetworker(String networker) {
        this.networker = networker;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }


    
}
