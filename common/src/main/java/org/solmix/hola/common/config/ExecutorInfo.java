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
package org.solmix.hola.common.config;

import java.util.HashMap;
import java.util.Map;

import org.solmix.commons.annotation.ThreadSafe;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月13日
 */
@ThreadSafe
public class ExecutorInfo extends ExtensionInfo<ExecutorInfo>
{

    private static final long serialVersionUID = -4873010981768016604L;

    public ExecutorInfo(Map<String, Object> properties)
    {
        super(properties);
    }
    /**
     * 线程名称
     */
    public static  final  String EXECUTOR_THREAD_NAME="executor.threadName";
    /**
     * 核心线程
     */
    public static  final  String EXECUTOR_CORE_THREADS="executor.coreThreads";
    /**
     * 线程
     */
    public static  final  String EXECUTOR_THREADS="executor.threads";
    /**
     * 队列
     */
    public static  final  String EXECUTOR_QUEUES="executor.queues";
    /**
     * 活动时间
     */
    public static  final  String EXECUTOR_ALIVE="executor.alive";
  
    /**
     * @return the threadName
     */
    public String getThreadName() {
        return getString(EXECUTOR_THREAD_NAME);
    }
    public String getThreadName(String df) {
        return getString(EXECUTOR_THREAD_NAME,df);
    }
    
    /**
     * @return the coreThreads
     */
    public Integer getCoreThreads() {
        return getInt(EXECUTOR_CORE_THREADS);
    }
    public Integer getCoreThreads(int defaultValue) {
        return getInt(EXECUTOR_CORE_THREADS,defaultValue);
    }
  
    /**
     * @return the threads
     */
    public Integer getThreads() {
        return getInt(EXECUTOR_THREADS);
    }
    public Integer getThreads(int defaultValue) {
        return getInt(EXECUTOR_THREADS,defaultValue);
    }
 
    /**
     * @return the queues
     */
    public Integer getQueues() {
        return getInt(EXECUTOR_QUEUES);
    }
    public Integer getQueues(int defaultValue) {
        return getInt(EXECUTOR_QUEUES,defaultValue);
    }
    
    /**
     * @return the alive
     */
    public Integer getAlive() {
        return getInt(EXECUTOR_ALIVE);
    }
    public Integer getAlive(int defaultValue) {
        return getInt(EXECUTOR_ALIVE,defaultValue);
    }
  
    
    public static Builder newBuilder(){
        return new Builder();
    }
    public static Builder newBuilder(ExtensionInfo<?> info){
        return new Builder(info);
    }
    public static class Builder{
        private final  Map<String,Object> properties = new HashMap<String,Object>();
        private Builder()
        {
        }

        
        /**
         * @param info
         */
        public Builder(ExtensionInfo<?> info)
        {
            properties.putAll(info.getProperties());
        }


        /**
         * @param threadName the threadName to set
         */
        public Builder setThreadName(String threadName) {
            properties.put(EXECUTOR_THREAD_NAME, threadName);
            return this;
        }
        
        /**
         * @param coreThreads the coreThreads to set
         */
        public Builder setCoreThreads(Integer coreThreads) {
            properties.put(EXECUTOR_CORE_THREADS, coreThreads);
            return this;
        }
        
        /**
         * @param threads the threads to set
         */
        public Builder setThreads(Integer threads) {
            properties.put(EXECUTOR_THREADS, threads);
            return this;
        }
        
        /**
         * @param queues the queues to set
         */
        public Builder setQueues(Integer queues) {
            properties.put(EXECUTOR_QUEUES, queues);
            return this;
        }
        /**
         * @param alive the alive to set
         */
        public Builder setAlive(Integer alive) {
            properties.put(EXECUTOR_ALIVE, alive);
            return this;
        }
        public ExecutorInfo build(){
            ExecutorInfo info = new ExecutorInfo(properties);
            //TODO validate properties.
            return info;
        }
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.config.ExtensionInfo#getSelf()
     */
    @Override
    protected ExecutorInfo getSelf() {
        return this;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.config.ExtensionInfo#makeSelf(java.util.Map)
     */
    @Override
    protected ExecutorInfo makeSelf(Map<String, Object> map) {
        return new ExecutorInfo(map);
    }
}
