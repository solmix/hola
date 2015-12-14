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
 * @version $Id$ 2014年10月30日
 */

public class ConsumerDefinition extends AbstractReferenceDefinition {

    /**    */
    private static final long serialVersionUID = -2030026232826658223L;

    private Boolean isDefault;
    
    

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
     * <li>连接数
     */
    private Integer pipelines;


    /**
     * <li>字符集
     */
    private String charset;





    /**
     * 信息交换方式
     */
    private String exchanger;

    /**
     * 信息线程模型派发方式
     */
    private String dispatcher;


    /**
     * 组网方式
     */
    private String networker;
    @Override
    public void setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        String rmiTimeout = System.getProperty("sun.rmi.transport.tcp.responseTimeout");
        if (timeout != null && timeout > 0
            && (rmiTimeout == null || rmiTimeout.length() == 0)) {
            System.setProperty("sun.rmi.transport.tcp.responseTimeout",
                String.valueOf(timeout));
        }
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

  

    public Boolean getIsDefault() {
        return isDefault;
    }

    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    
    
    public String getExecutor() {
        return executor;
    }

    
    public void setExecutor(String executor) {
        this.executor = executor;
    }

    
    public Integer getThreads() {
        return threads;
    }

    
    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    
    public Integer getIothreads() {
        return iothreads;
    }

    
    public void setIothreads(Integer iothreads) {
        this.iothreads = iothreads;
    }

    
    public Integer getQueues() {
        return queues;
    }

    
    public void setQueues(Integer queues) {
        this.queues = queues;
    }
    
    public Integer getPipelines() {
        return pipelines;
    }

    
    public void setPipelines(Integer pipelines) {
        this.pipelines = pipelines;
    }

    
    public String getCharset() {
        return charset;
    }

    
    public void setCharset(String charset) {
        this.charset = charset;
    }

    
    public String getExchanger() {
        return exchanger;
    }

    
    public void setExchanger(String exchanger) {
        this.exchanger = exchanger;
    }

    
    public String getDispatcher() {
        return dispatcher;
    }

    
    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    
    public String getNetworker() {
        return networker;
    }

    
    public void setNetworker(String networker) {
        this.networker = networker;
    }

    
}
