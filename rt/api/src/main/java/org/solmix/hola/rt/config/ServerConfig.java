/*
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

package org.solmix.hola.rt.config;

import org.solmix.hola.core.executor.ExecutorProvider;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月6日
 */

public class ServerConfig extends AbstractServiceConfig
{
    private static final long serialVersionUID = -4040109969678654602L;

    /**
     * 主机
     */
    private String host;

    /**
     * 服务端口
     */
    private Integer port;
    /**
     * 上下文路径
     */
    private String contextpath;
  
    /**
     * 线程池名称
     */
    private String threadpool;
    
    /**
     * 线程池大小
     */
    private Integer   threads;
    
    /**
     * IO线程池大小
     */
    private Integer     iothreads;

    /**
     * 线程池队列大小
     */
    private Integer queues;

    /**
     * 最大接收连接数
     */
    private Integer accepts;
    /**
     * 编码实现名称
     */
    private String codec;

    /**
     * 序列化方法
     */
    private String serial;

    /**
     * 字符集
     */
    private String charset;
    /**
     * 最大请求数据长度
     */
    private Integer      payload;
    
    /**
     * 心跳间隔
     */
    private Integer    heartbeat;
    
    /**
     * 网络传输方式
     */
    private String    transporter;
    /**
     * 缓存区大小
     */
    private Integer buffer;
    /**
     * 信息交换方式
     */
    private String    exchanger;
    
    /**
     * 信息线程模型派发方式
     */
    private String   dispatcher;

    /**
     * 是否公告
     */
    private Boolean advertise;
    /**
     * 组网方式
     */
    private String      networker;
    
    /**
     * 是否为默认设置
     */
    private Boolean isDefault;
     ServerConfig(Container container)
    {
        super(container);
    }

    public ServerConfig(Container container, String protocol)
    {
        super(container);
        setProtocol(protocol);
    }

    /**
     * @return the host
     */
    @Property(excluded=true)
    public String getHost() {
        return host;
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
    @Property(excluded=true)
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
     * @return the codec
     */
    public String getCodec() {
        return codec;
    }

    /**
     * @param codec the codec to set
     */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    /**
     * @return the queues
     */
    public Integer getQueues() {
        return queues;
    }

    /**
     * @param queues the queues to set
     */
    public void setQueues(Integer queues) {
        this.queues = queues;
    }

    /**
     * @return the accepts
     */
    public Integer getAccepts() {
        return accepts;
    }

    /**
     * @param accepts the accepts to set
     */
    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    /**
     * @return the contextpath
     */
    @Property(excluded=true)
    public String getContextpath() {
        return contextpath;
    }

    /**
     * @param contextpath the contextpath to set
     */
    public void setContextpath(String contextpath) {
        this.contextpath = contextpath;
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
     * @return the buffer
     */
    public Integer getBuffer() {
        return buffer;
    }

    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
    }

    /**
     * @return the executor
     */
    public String getThreadpool() {
        return threadpool;
    }

    /**
     * @param executor the executor to set
     */
    public void setThreadpool(String threadpool) {
        this.threadpool = threadpool;
        checkExtension(ExecutorProvider.class, "threadpool", threadpool);
    }

    /**
     * @return the advertise
     */
    public Boolean getAdvertise() {
        return advertise;
    }

    /**
     * @param advertise the advertise to set
     */
    public void setAdvertise(Boolean advertise) {
        this.advertise = advertise;
    }

    
    /**
     * @return the threads
     */
    public Integer getThreads() {
        return threads;
    }

    
    /**
     * @param threads the threads to set
     */
    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    
    /**
     * @return the iothreads
     */
    public Integer getIothreads() {
        return iothreads;
    }

    
    /**
     * @param iothreads the iothreads to set
     */
    public void setIothreads(Integer iothreads) {
        this.iothreads = iothreads;
    }

    
    /**
     * @return the serial
     */
    public String getSerial() {
        return serial;
    }

    
    /**
     * @param serial the serial to set
     */
    public void setSerial(String serial) {
        this.serial = serial;
    }

    
    /**
     * @return the payload
     */
    public Integer getPayload() {
        return payload;
    }

    
    /**
     * @param payload the payload to set
     */
    public void setPayload(Integer payload) {
        this.payload = payload;
    }

    
    /**
     * @return the heartbeat
     */
    public Integer getHeartbeat() {
        return heartbeat;
    }

    
    /**
     * @param heartbeat the heartbeat to set
     */
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    
    /**
     * @return the transporter
     */
    public String getTransporter() {
        return transporter;
    }

    
    /**
     * @param transporter the transporter to set
     */
    public void setTransporter(String transporter) {
        this.transporter = transporter;
    }

    
    /**
     * @return the exchanger
     */
    public String getExchanger() {
        return exchanger;
    }

    
    /**
     * @param exchanger the exchanger to set
     */
    public void setExchanger(String exchanger) {
        this.exchanger = exchanger;
    }

    
    /**
     * @return the dispatcher
     */
    public String getDispatcher() {
        return dispatcher;
    }

    
    /**
     * @param dispatcher the dispatcher to set
     */
    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    
    /**
     * @return the networker
     */
    public String getNetworker() {
        return networker;
    }

    
    /**
     * @param networker the networker to set
     */
    public void setNetworker(String networker) {
        this.networker = networker;
    }

    
    /**
     * @return the isDefault
     */
    public Boolean getIsDefault() {
        return isDefault;
    }

    
    /**
     * @param isDefault the isDefault to set
     */
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

}
