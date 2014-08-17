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

import java.net.InetSocketAddress;

import org.solmix.commons.util.NetUtils;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月13日
 */

public class ChannelInfo
{
    /**
     * 心跳周期
     */
    private volatile Integer heartbeat;
    /**
     * 心跳超时时间
     */
    private volatile Integer heartbeatTimeout;
    /**
     * 序列化实现名称:java/json/avro
     */
    private volatile String serialName;
    
    /**
     * 交换层实现
     */
    private volatile String exchangeName;
    
    /**
     * 传输层实现
     */
    private volatile String transport;
    /**
     * 线程名称
     */
    private volatile String threadName;
    /**
     * 超时时间(ms)
     */
    private volatile Integer timeout;
    /**
     * 关闭等待时间
     */
    private volatile Long shutdownTimeout;
    
    /**
     * 连接超时时间(ms)
     */
    private volatile Integer connectTimeout;
    
    /**
     * 通道空闲超时(ms)
     */
    private volatile Integer idleTimeout;
    /**
     * 线程池实现
     */
    private volatile String threadPool;
    
    /**
     * 编码/解码实现
     */
    private volatile String codec;
    
    /**
     * 分发实现
     */
    private volatile String dispather;

    /**
     * 连接数
     */
    private volatile Integer connections;
    
    /**
     * 最大允许建立信道数
     */
    private volatile Integer accepts;
    
    /**
     * 信道buffer大小
     */
    private volatile Integer buffer;
    
    /**
     * 信道字符集
     */
    private volatile String charset;
    
    /**
     * IO线程数
     */
    private volatile Integer ioThreads;
    
    /**
     * 每次通信负载
     */
    private volatile Integer payload;
    
    /**
     * 是否为服务端
     */
    private volatile Boolean server;
    
    /**
     * 是否启用等待超时策略
     */
    private volatile Boolean await;
    
    /**
     * 是否启用只读模式
     */
    private volatile Boolean readOnly;
    
    /**
     * 是否启用重连
     */
    private volatile Boolean reconnect;
    
    
    /**
     * 重连周期
     */
    private volatile Integer reconnectPeriod;
    /**
     *发送事项是否启用重连
     */
    private volatile Boolean sendReconnect;
    
    private  volatile String host;

    private  volatile Integer port;
    
    /**
     * 线程池配置信息
     */
    private volatile ExecutorInfo executor;
    
    
    /**
     * 重连警告周期,多次重连失败后发出警告信息
     */
    private volatile Integer reconnectWarningPeriod;
    
    /**
     * @return the reconnectPeriod
     */
    public Integer getReconnectPeriod() {
        return reconnectPeriod;
    }

    public Integer getReconnectPeriod(Integer defaultValue) {
        return reconnectPeriod==null?defaultValue:reconnectPeriod;
    }


    
    /**
     * @param reconnectPeriod the reconnectPeriod to set
     */
    public void setReconnectPeriod(Integer reconnectPeriod) {
        this.reconnectPeriod = reconnectPeriod;
    }



    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    
    
    
    /**
     * @return the transport
     */
    public String getTransport() {
        return transport;
    }

    
    /**
     * @param transport the transport to set
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /**
     * @return the threadName
     */
    public String getThreadName() {
        return threadName;
    }
    public String getThreadName(String defaultValue) {
        return threadName==null?defaultValue:threadName;
    }

    
    /**
     * @param threadName the threadName to set
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }


    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    
    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    
    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return the heartbeat
     */
    public Integer getHeartbeat() {
        return heartbeat;
    }
    public Integer getHeartbeat(int defaultValue) {
        return heartbeat==null?defaultValue:heartbeat;
    }
    /**
     * @param heartbeat the heartbeat to set
     */
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    
    /**
     * @return the serialName
     */
    public String getSerialName() {
        return serialName;
    }
    public String getSerialName(String name) {
        return serialName==null?name:serialName;
    }
    
    /**
     * @param serialName the serialName to set
     */
    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    
    /**
     * @return the exchangeName
     */
    public String getExchangeName() {
        return exchangeName;
    }

    
    /**
     * @param exchangeName the exchangeName to set
     */
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    
    /**
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    public Integer getTimeout(Integer defaultValue) {
        return timeout==null?defaultValue:timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    
    /**
     * @return the threadPool
     */
    public String getThreadPool() {
        return threadPool;
    }
    public String getThreadPool(String df) {
        return threadPool==null?df:threadPool;
    }
    
    /**
     * @param threadPool the threadPool to set
     */
    public void setThreadPool(String threadPool) {
        this.threadPool = threadPool;
    }

    
    /**
     * @return the codec
     */
    public String getCodec() {
        return codec;
    }
    public String getCodec(String defaultName) {
        return codec==null?defaultName:codec;
    }
    
    /**
     * @param codec the codec to set
     */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    
    /**
     * @return the dispather
     */
    public String getDispather() {
        return dispather;
    }
    public String getDispather(String df) {
        return dispather==null?df:dispather;
    }
    
    /**
     * @param dispather the dispather to set
     */
    public void setDispather(String dispather) {
        this.dispather = dispather;
    }

    
    /**
     * @return the connections
     */
    public Integer getConnections() {
        return connections;
    }

    
    /**
     * @param connections the connections to set
     */
    public void setConnections(Integer connections) {
        this.connections = connections;
    }

    
    /**
     * @return the ioThreads
     */
    public Integer getIoThreads() {
        return ioThreads;
    }
    public Integer getIoThreads(int defaultValue) {
        return ioThreads==null?defaultValue:ioThreads;
    }
    
    /**
     * @param ioThreads the ioThreads to set
     */
    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    
    /**
     * @return the server
     */
    public Boolean getServer() {
        return server;
    }

    
    /**
     * @param server the server to set
     */
    public void setServer(Boolean server) {
        this.server = server;
    }

    
    /**
     * @return the executor
     */
    public ExecutorInfo getExecutor() {
        return executor;
    }

    
    /**
     * @param executor the executor to set
     */
    public void setExecutor(ExecutorInfo executor) {
        this.executor = executor;
    }

    
    /**
     * @return the heartbeatTimeout
     */
    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }
    public Integer getHeartbeatTimeout(int defaultValue) {
        return heartbeatTimeout==null?defaultValue:heartbeatTimeout;
    }
    
    /**
     * @param heartbeatTimeout the heartbeatTimeout to set
     */
    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    
    /**
     * @return the shutdownTimeout
     */
    public Long getShutdownTimeout() {
        return shutdownTimeout;
    }
    
    public long getShutdownTimeout(Long defaultValue) {
        return shutdownTimeout==null?defaultValue:shutdownTimeout;
    }
    
    /**
     * @param shutdownTimeout the shutdownTimeout to set
     */
    public void setShutdownTimeout(Long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    
    /**
     * @return the connectTimeout
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getConnectTimeout(Integer deafultValue) {
        return connectTimeout==null?deafultValue:connectTimeout;
    }
    /**
     * @param connectTimeout the connectTimeout to set
     */
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    
    /**
     * @return the idleTimeout
     */
    public Integer getIdleTimeout() {
        return idleTimeout;
    }
    public Integer getIdleTimeout(Integer defaultValue) {
        return idleTimeout==null?defaultValue:idleTimeout;
    }

    
    /**
     * @param idleTimeout the idleTimeout to set
     */
    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    
    /**
     * @return the accepts
     */
    public Integer getAccepts() {
        return accepts;
    }
    
    public Integer getAccepts(Integer defaultValue) {
        return accepts==null?defaultValue:accepts;
    }
    
    /**
     * @param accepts the accepts to set
     */
    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    
    /**
     * @return the buffer
     */
    public Integer getBuffer() {
        return buffer;
    }

    public Integer getBuffer(int defaultValue) {
        return buffer==null?defaultValue:buffer;
    }
    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
    }

    
    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    
    /**
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    
    /**
     * @return the payload
     */
    public Integer getPayload() {
        return payload;
    }
    public Integer getPayload(int deafultValue) {
        return payload==null?deafultValue:payload;
    }
    
    /**
     * @param payload the payload to set
     */
    public void setPayload(Integer payload) {
        this.payload = payload;
    }

    
    /**
     * @return the await
     */
    public Boolean getAwait() {
        return await;
    }
    
    public Boolean getAwait(Boolean defaultValue) {
        return await==null?defaultValue:await;
    }

    
    /**
     * @param await the await to set
     */
    public void setAwait(Boolean await) {
        this.await = await;
    }

    
    /**
     * @return the readOnly
     */
    public Boolean getReadOnly() {
        return readOnly;
    }

    
    /**
     * @param readOnly the readOnly to set
     */
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    
    /**
     * @return the reconnect
     */
    public Boolean getReconnect() {
        return reconnect;
    }
    public Boolean getReconnect(boolean defaultValue) {
        return reconnect==null?defaultValue:reconnect;
    }
    
    /**
     * @param reconnect the reconnect to set
     */
    public void setReconnect(Boolean reconnect) {
        this.reconnect = reconnect;
    }

    
    /**
     * @return the sendReconnect
     */
    public Boolean getSendReconnect() {
        return sendReconnect;
    }
    
    /**
     * @return the reconnectWarningPeriod
     */
    public Integer getReconnectWarningPeriod() {
        return reconnectWarningPeriod;
    }
    
    /**
     * @param defaultValue
     * @return
     */
    public Integer getReconnectWarningPeriod(Integer defaultValue) {
        return reconnectWarningPeriod==null?defaultValue:reconnectWarningPeriod;
    }

    
    /**
     * @param reconnectWarningPeriod the reconnectWarningPeriod to set
     */
    public void setReconnectWarningPeriod(Integer reconnectWarningPeriod) {
        this.reconnectWarningPeriod = reconnectWarningPeriod;
    }


    /**
     * @param sendReconnect the sendReconnect to set
     */
    public void setSendReconnect(Boolean sendReconnect) {
        this.sendReconnect = sendReconnect;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }
    
    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }
    public String getIp() {
        return NetUtils.getIpByHost(host);
    }
    
    public static Builder newBuilder(){
        return new Builder();
    }
    public static Builder newBuilder(ChannelInfo info){
        return new Builder(info);
    }
    public static class Builder{
        private  Integer heartbeat;
        private  Integer heartbeatTimeout;
        private  String serialName;
        private  String exchangeName;
        private  String transport;
        private  String threadName;
        private  Integer timeout;
        private  Long shutdownTimeout;
        private  Integer connectTimeout;
        private  Integer idleTimeout;
        private  String threadPool;
        private  String codec;
        private  String dispather;
        private  Integer connections;
        private  Integer accepts;
        private  Integer buffer;
        private  String charset;
        private  Integer ioThreads;
        private  Integer payload;
        private  Boolean server;
        private  Boolean await;
        private  Boolean readOnly;
        private  Boolean reconnect;
        private  Integer reconnectPeriod;
        private  Boolean sendReconnect;
        private   String host;
        private   Integer port;
        private  ExecutorInfo executor;
        private  Integer reconnectWarningPeriod;
        private Builder()
        {
        }
        /**
         * @param info
         */
        public Builder(ChannelInfo info)
        {
            this.accepts=info.accepts;
            this.await=info.await;
            this.buffer=info.buffer;
            this.charset=info.charset;
            this.codec=info.codec;
            this.connections=info.connections;
            this.connectTimeout=info.connectTimeout;
            this.dispather=info.dispather;
            this.exchangeName=info.exchangeName;
            this.transport=info.transport;
            this.executor=info.executor;
            this.heartbeat=info.heartbeat;
            this.heartbeatTimeout=info.heartbeatTimeout;
            this.host=info.host;
            this.idleTimeout=info.idleTimeout;
            this.ioThreads=info.ioThreads;
            this.payload=info.payload;
            this.port=info.port;
            this.readOnly=info.readOnly;
            this.reconnect=info.reconnect;
            this.reconnectPeriod=info.reconnectPeriod;
            this.reconnectWarningPeriod=info.reconnectWarningPeriod;
            this.sendReconnect=info.sendReconnect;
            this.serialName=info.serialName;
            this.server=info.server;
            this.shutdownTimeout=info.shutdownTimeout;
            this.threadName=info.threadName;
            this.threadPool=info.threadPool;
            this.timeout=info.timeout;
        }
        /**
         * @param heartbeat the heartbeat to set
         */
        public Builder setHeartbeat(Integer heartbeat) {
            this.heartbeat = heartbeat;
            return this;
        }
        
        
        /**
         * @param transport the transport to set
         */
        public void setTransport(String transport) {
            this.transport = transport;
        }
        /**
         * @param heartbeatTimeout the heartbeatTimeout to set
         */
        public Builder setHeartbeatTimeout(Integer heartbeatTimeout) {
            this.heartbeatTimeout = heartbeatTimeout;
            return this;
        }
        /**
         * @param serialName the serialName to set
         */
        public Builder setSerialName(String serialName) {
            this.serialName = serialName;
            return this;
        }
        /**
         * @param exchangeName the exchangeName to set
         */
        public Builder setExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }
        /**
         * @param threadName the threadName to set
         */
        public Builder setThreadName(String threadName) {
            this.threadName = threadName;
            return this;
        }
        /**
         * @param timeout the timeout to set
         */
        public Builder setTimeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }
        /**
         * @param shutdownTimeout the shutdownTimeout to set
         */
        public Builder setShutdownTimeout(Long shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }
        /**
         * @param connectTimeout the connectTimeout to set
         */
        public Builder setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        /**
         * @param idleTimeout the idleTimeout to set
         */
        public Builder setIdleTimeout(Integer idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }
        /**
         * @param threadPool the threadPool to set
         */
        public Builder setThreadPool(String threadPool) {
            this.threadPool = threadPool;
            return this;
        }
        /**
         * @param codec the codec to set
         */
        public Builder setCodec(String codec) {
            this.codec = codec;
            return this;
        }
        
        /**
         * @param dispather the dispather to set
         */
        public Builder setDispather(String dispather) {
            this.dispather = dispather;
            return this;
        }
        /**
         * @param connections the connections to set
         */
        public Builder setConnections(Integer connections) {
            this.connections = connections;
            return this;
        }
        /**
         * @param accepts the accepts to set
         */
        public Builder setAccepts(Integer accepts) {
            this.accepts = accepts;
            return this;
        }
        /**
         * @param buffer the buffer to set
         */
        public Builder setBuffer(Integer buffer) {
            this.buffer = buffer;
            return this;
        }
        /**
         * @param charset the charset to set
         */
        public Builder setCharset(String charset) {
            this.charset = charset;
            return this;
        }
        /**
         * @param ioThreads the ioThreads to set
         */
        public Builder setIoThreads(Integer ioThreads) {
            this.ioThreads = ioThreads;
            return this;
        }
        /**
         * @param payload the payload to set
         */
        public Builder setPayload(Integer payload) {
            this.payload = payload;
            return this;
        }
        /**
         * @param server the server to set
         */
        public Builder setServer(Boolean server) {
            this.server = server;
            return this;
        }
        /**
         * @param await the await to set
         */
        public Builder setAwait(Boolean await) {
            this.await = await;
            return this;
        }
        /**
         * @param readOnly the readOnly to set
         */
        public Builder setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }
        /**
         * @param reconnect the reconnect to set
         */
        public Builder setReconnect(Boolean reconnect) {
            this.reconnect = reconnect;
            return this;
        }
        /**
         * @param reconnectPeriod the reconnectPeriod to set
         */
        public Builder setReconnectPeriod(Integer reconnectPeriod) {
            this.reconnectPeriod = reconnectPeriod;
            return this;
        }
        /**
         * @param sendReconnect the sendReconnect to set
         */
        public Builder setSendReconnect(Boolean sendReconnect) {
            this.sendReconnect = sendReconnect;
            return this;
        }
        /**
         * @param host the host to set
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }
        /**
         * @param port the port to set
         */
        public Builder setPort(Integer port) {
            this.port = port;
            return this;
        }
        /**
         * @param executor the executor to set
         */
        public Builder setExecutor(ExecutorInfo executor) {
            this.executor = executor;
            return this;
        }
        /**
         * @param reconnectWarningPeriod the reconnectWarningPeriod to set
         */
        public Builder setReconnectWarningPeriod(Integer reconnectWarningPeriod) {
            this.reconnectWarningPeriod = reconnectWarningPeriod;
            return this;
        }



        public ChannelInfo build(){
            ChannelInfo info=   new ChannelInfo();
            info.accepts=this.accepts;
            info.await=this.await;
            info.buffer=this.buffer;
            info.charset=this.charset;
            info.codec=this.codec;
            info.connections=this.connections;
            info.connectTimeout=this.connectTimeout;
            info.dispather=this.dispather;
            info.exchangeName=this.exchangeName;
            info.transport=this.transport;
            info.executor=this.executor;
            info.heartbeat=this.heartbeat;
            info.heartbeatTimeout=this.heartbeatTimeout;
            info.host=this.host;
            info.idleTimeout=this.idleTimeout;
            info.ioThreads=this.ioThreads;
            info.payload=this.payload;
            info.port=this.port;
            info.readOnly=this.readOnly;
            info.reconnect=this.reconnect;
            info.reconnectPeriod=this.reconnectPeriod;
            info.reconnectWarningPeriod=this.reconnectWarningPeriod;
            info.sendReconnect=this.sendReconnect;
            info.serialName=this.serialName;
            info.server=this.server;
            info.shutdownTimeout=this.shutdownTimeout;
            info.threadName=this.threadName;
            info.threadPool=this.threadPool;
            info.timeout=this.timeout;
            return info;
        }
    }
}
