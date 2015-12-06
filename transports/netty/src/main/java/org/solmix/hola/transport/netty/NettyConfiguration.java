/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.transport.netty;

import org.solmix.hola.common.HOLA;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */

public class NettyConfiguration
{

    private boolean chunking;

    private int chunkThreshold;

    private int connectTimeout= HOLA.DEFAULT_CONNECT_TIMEOUT;
    
    private Integer writeIdleTimeout ;
    private int writeTimeout =HOLA.DEFAULT_TIMEOUT;
    
    private Integer idleTimeout;
    
    private int timeout= HOLA.DEFAULT_TIMEOUT;

    private Integer readIdleTimeout;

    private final long asyncExecuteTimeout = -1;

    private String codec;

    private int threadPoolSize = 5;

    private int bufferSize = HOLA.DEFAULT_BUFFER_SIZE;
    private Integer heartbeat;
    
    private Integer heartbeatTimeout;


    private boolean waiteSuccess;


    
    public boolean isChunking() {
        return chunking;
    }


    
    public void setChunking(boolean chunking) {
        this.chunking = chunking;
    }


    
    public int getChunkThreshold() {
        return chunkThreshold;
    }


    
    public void setChunkThreshold(int chunkThreshold) {
        this.chunkThreshold = chunkThreshold;
    }


    
    public int getConnectTimeout() {
        return connectTimeout;
    }


    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }


    
    public Integer getWriteIdleTimeout() {
        return writeIdleTimeout;
    }


    
    public void setWriteIdleTimeout(Integer writeIdleTimeout) {
        this.writeIdleTimeout = writeIdleTimeout;
    }


    
    public Integer getIdleTimeout() {
        return idleTimeout;
    }


    
    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }


    
    public int getTimeout() {
        return timeout;
    }


    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    
    public Integer getReadIdleTimeout() {
        return readIdleTimeout;
    }


    
    public void setReadIdleTimeout(Integer readIdleTimeout) {
        this.readIdleTimeout = readIdleTimeout;
    }


    
    public String getCodec() {
        return codec;
    }


    
    public void setCodec(String codec) {
        this.codec = codec;
    }


    
    
    public int getWriteTimeout() {
        return writeTimeout;
    }



    
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }



    public int getThreadPoolSize() {
        return threadPoolSize;
    }


    
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }


    
    public int getBufferSize() {
        return bufferSize;
    }


    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    
    public Integer getHeartbeat() {
        return heartbeat;
    }


    
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }


    
    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }


    
    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }


    
    public boolean isWaiteSuccess() {
        return waiteSuccess;
    }


    
    public void setWaiteSuccess(boolean waiteSuccess) {
        this.waiteSuccess = waiteSuccess;
    }


    
    public long getAsyncExecuteTimeout() {
        return asyncExecuteTimeout;
    }

    public boolean enableIdleHeartbeat(){
        if(enableHeartbeat()
            &&(
                (this.writeIdleTimeout!=null&&this.writeIdleTimeout>0)
                ||(this.readIdleTimeout!=null&&this.readIdleTimeout>0)
                ||(this.idleTimeout!=null&&this.idleTimeout>0))){
            return true;
        }else{
            return false;
        }
        
    }
    
    public boolean enableHeartbeat(){
        return heartbeat!=null&&heartbeat.intValue()>0;
    }
}
