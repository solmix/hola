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
package org.solmix.hola.core.model;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月13日
 */

public class ExecutorInfo
{
    
    private String threadName;
    private Integer coreThreads;
    private Integer threads;
    private Integer queues;
    private Integer alive;
    
    /**
     * @return the threadName
     */
    public String getThreadName() {
        return threadName;
    }
    public String getThreadName(String df) {
        return threadName==null?df:threadName;
    }
    /**
     * @param threadName the threadName to set
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    
    /**
     * @return the coreThreads
     */
    public Integer getCoreThreads() {
        return coreThreads;
    }
    public Integer getCoreThreads(int defaultValue) {
        return coreThreads==null?defaultValue:coreThreads;
    }
    /**
     * @param coreThreads the coreThreads to set
     */
    public void setCoreThreads(Integer coreThreads) {
        this.coreThreads = coreThreads;
    }
    
    /**
     * @return the threads
     */
    public Integer getThreads() {
        return threads;
    }
    public Integer getThreads(int defaultValue) {
        return threads==null?defaultValue:threads;
    }
    /**
     * @param threads the threads to set
     */
    public void setThreads(Integer threads) {
        this.threads = threads;
    }
    
    /**
     * @return the queues
     */
    public Integer getQueues() {
        return queues;
    }
    public Integer getQueues(int defaultValue) {
        return queues==null?defaultValue:queues;
    }
    /**
     * @param queues the queues to set
     */
    public void setQueues(Integer queues) {
        this.queues = queues;
    }
    
    /**
     * @return the alive
     */
    public Integer getAlive() {
        return alive;
    }
    public Integer getAlive(int defaultValue) {
        return alive==null?defaultValue:alive;
    }
    /**
     * @param alive the alive to set
     */
    public void setAlive(Integer alive) {
        this.alive = alive;
    }
    
}
