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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月13日
 */

public class ExecutorInfo extends EndpointInfo
{

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
    @Override
    public ExecutorInfo addProperty(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return this;
        if (value.equals(getProperty(key)))
            return this;
        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.put(key, value);
        return new ExecutorInfo(map);
    }
    @Override
    public ExecutorInfo addProperties(Map<String, Object> properties) {
        if (properties == null || properties.size() == 0) {
            return this;
        }
        boolean hasAndEqual = true;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = getProperty(entry.getKey());
            if (value == null && entry.getValue() != null
                || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if (hasAndEqual)
            return this;

        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.putAll(properties);
        return new ExecutorInfo(map);
    }

    @Override
    public ExecutorInfo addProperties(Properties properties) {
        if (properties == null || properties.size() == 0) {
            return this;
        }
        boolean hasAndEqual = true;
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Object value = getProperty(entry.getKey().toString());
            if (value == null && entry.getValue() != null
                || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if (hasAndEqual)
            return this;

        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return new ExecutorInfo(map);
    }

    @Override
    public ExecutorInfo addPropertyIfAbsent(String key, Object value) {
        if (key == null || key.length() == 0 || value == null)
            return this;
        if (hasProperty(key))
            return this;
        Map<String, Object> map = new HashMap<String, Object>(getProperties());
        map.put(key, value);
        return new ExecutorInfo(map);
    }

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
    public static Builder newBuilder(EndpointInfo info){
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
        public Builder(EndpointInfo info)
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
}
