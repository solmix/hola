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
    
    private int timeout= HOLA.DEFAULT_TIMEOUT;

    private int receiveTimeout= HOLA.DEFAULT_RECEIVE_TIMEOUT;

    private final long asyncExecuteTimeout = -1;

    private String codec;

    private int threadPoolSize = 5;

    private int bufferSize = HOLA.DEFAULT_BUFFER_SIZE;

    private int writeTimeout = HOLA.DEFAULT_WRITE_TIMEOUT;

    private boolean waiteSuccess;

    /**   */
    public String getCodec() {
        return codec;
    }

    /**   */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    
    public int getConnectTimeout() {
        return connectTimeout;
    }

    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**   */
    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    /**   */
    public void setReceiveTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**   */
    public boolean isChunking() {
        return chunking;
    }

    
    public int getTimeout() {
        return timeout;
    }

    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**   */
    public void setChunking(boolean chunking) {
        this.chunking = chunking;
    }

    /**   */
    public int getChunkThreshold() {
        return chunkThreshold;
    }

    /**   */
    public void setChunkThreshold(int chunkThreshold) {
        this.chunkThreshold = chunkThreshold;
    }

    /**   */
    public int getBufferSize() {
        return bufferSize;
    }

    /**   */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @return
     */
    public long getAsyncExecuteTimeout() {
        return asyncExecuteTimeout;
    }

    
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
    
    public int getWriteTimeout() {
        return writeTimeout;
    }

    
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    
    public boolean isWaiteSuccess() {
        return waiteSuccess;
    }

    
    public void setWaiteSuccess(boolean waiteSuccess) {
        this.waiteSuccess = waiteSuccess;
    }

}
