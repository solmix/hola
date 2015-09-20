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

import org.solmix.hola.common.Constants;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */

public class NettyConfiguration
{

    private boolean chunking;

    private int chunkThreshold;

    private int connectionTimeout;

    private int receiveTimeout;

    private final long asyncExecuteTimeout = -1;

    private String codec;

    private int threadPoolSize = 5;

    private String host;

    private int port;

    private String path;

    private int bufferSize = Constants.DEFAULT_BUFFER_SIZE;

    private int writeTimeout = Constants.DEFAULT_TIMEOUT;

    private boolean waiteSuccess;

    /**   */
    public String getCodec() {
        return codec;
    }

    /**   */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    /**   */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**   */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
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

    
    public String getHost() {
        return host;
    }

    
    public void setHost(String host) {
        this.host = host;
    }

    
    public int getPort() {
        return port;
    }

    
    public void setPort(int port) {
        this.port = port;
    }

    
    public String getPath() {
        return path;
    }

    
    public void setPath(String path) {
        this.path = path;
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
