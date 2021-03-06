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
 * Netty 参数配置
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月15日
 */

public class NettyConfiguration
{

    public enum SSLMODE{
        CA("CA"),
        SCA("SCA");
        private final String value;
        SSLMODE(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static SSLMODE fromValue(String v) {
            for (SSLMODE c: SSLMODE.values()) {
                if (c.value.equalsIgnoreCase(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }
    // XXX通过netty -Dio.netty.allocator.chunkSize: 配置
    // private boolean chunking;private int chunkThreshold;
    /**SSL key别名*/
    private String keyAlias;
    /**SSL key文件路径*/
    private String keyFilePath;
    /**SSL key文件密码*/
    private String keyFilePassword;
    /**是否允许自动生成key*/
    private Boolean keyAuto;
    /**SSL CN 信息*/
    private String keyCN = "";
    /**SSL 认证方式,sacu saca*/
    private String keyMode;

    /** 连接超时毫秒数 */
    private int connectTimeout = HOLA.DEFAULT_CONNECT_TIMEOUT;

    /** 写超时,写完成后等待毫秒数 */
    private int writeTimeout = HOLA.DEFAULT_TIMEOUT;

    /** 超时时间,读等待超时毫秒 */
    private int timeout = HOLA.DEFAULT_TIMEOUT;

    private Integer idleTimeout;

    private Integer readIdleTimeout;

    private Integer writeIdleTimeout;

    /** 异步执行等待超时时间,如果线程已满无法立即执行,等待超时 */
    private final long asyncExecuteTimeout = -1;

    /** 编码/解码实现名称 */
    private String codec;

    /** 核心线程数,Netty中用于Child线程数,如果netty boss线程数小于该数字,boss也使用该线程数,默认boss线程数为处理器+1 */
    private int threads = 5;

    /** 心跳周期 */
    private Integer heartbeat;

    /** 心跳超时毫秒数 */
    private Integer heartbeatTimeout;

    /** 写动作后是否需要等待写超时,该参数配合writeTimeout使用 */
    private boolean waiteSuccess;

    /** 最大允许连接数 */
    private int accepts;

    /** 连接超时毫秒数 */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /** 连接超时毫秒数 */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** 超时时间,读等待超时毫秒 */
    public int getTimeout() {
        return timeout;
    }

    /** 超时时间,读等待超时毫秒 */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /** 编码/解码实现名称 */
    public String getCodec() {
        return codec;
    }

    /** 编码/解码实现名称 */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    /** 写超时,写完成后等待毫秒数 */
    public int getWriteTimeout() {
        return writeTimeout;
    }

    /** 写超时,写完成后等待毫秒数 */
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
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

    public Integer getReadIdleTimeout() {
        return readIdleTimeout;
    }

    public void setReadIdleTimeout(Integer readIdleTimeout) {
        this.readIdleTimeout = readIdleTimeout;
    }

    /** 核心线程数,Netty中用于Child线程数,如果netty boss线程数小于该数字,boss也使用该线程数,默认boss线程数为处理器+1 */
    public int getThreads() {
        return threads;
    }

    public void setThreads(int threadPoolSize) {
        this.threads = threadPoolSize;
    }

    /** 心跳周期 */
    public Integer getHeartbeat() {
        return heartbeat;
    }

    /** 心跳周期 */
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    /** 心跳超时毫秒数,默认为3个心跳周期 */
    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    /** 心跳超时毫秒数 */
    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    /** 写动作后是否需要等待写超时,该参数配合writeTimeout使用 */
    public boolean isWaiteSuccess() {
        return waiteSuccess;
    }

    public void setWaiteSuccess(boolean waiteSuccess) {
        this.waiteSuccess = waiteSuccess;
    }

    /** 最大允许连接数 */
    public int getAccepts() {
        return accepts;
    }

    /** 最大允许连接数 */
    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    /** 异步执行等待超时时间,如果线程已满无法立即执行,等待超时 */
    public long getAsyncExecuteTimeout() {
        return asyncExecuteTimeout;
    }

    /** 是否启动空闲检测 */
    public boolean enableIdleHeartbeat() {
        if (enableHeartbeat() && ((this.writeIdleTimeout != null && this.writeIdleTimeout > 0)
            || (this.readIdleTimeout != null && this.readIdleTimeout > 0) || (this.idleTimeout != null && this.idleTimeout > 0))) {
            return true;
        } else {
            return false;
        }

    }

    /** 是否启动心跳机制 */
    public boolean enableHeartbeat() {
        return heartbeat != null && heartbeat.intValue() > 0;
    }

    /**
     * @return
     */
    public boolean enableSSL() {
        return getKeyFilePath() != null 
            && getKeyFilePassword() != null 
            && getKeyMode() != null;
    }
    
    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
    
    public String getKeyFilePath() {
        return keyFilePath;
    }
    
    public void setKeyFilePath(String keyFilePath) {
        this.keyFilePath = keyFilePath;
    }

    public String getKeyFilePassword() {
        return keyFilePassword;
    }
    
    public void setKeyFilePassword(String keyFilePassword) {
        this.keyFilePassword = keyFilePassword;
    }
    
    public Boolean isKeyAuto() {
        return keyAuto;
    }

    public void setKeyAuto(Boolean keyAuto) {
        this.keyAuto = keyAuto;
    }

    
    public String getKeyCN() {
        return keyCN;
    }

    public void setKeyCN(String keyCN) {
        this.keyCN = keyCN;
    }
    
    public String getKeyMode() {
        return keyMode;
    }
    public SSLMODE getKEYMODE() {
        return SSLMODE.fromValue(keyMode);
    }
    
    public void setKeyMode(String keyMode) {
        this.keyMode = keyMode;
    }
    
}
