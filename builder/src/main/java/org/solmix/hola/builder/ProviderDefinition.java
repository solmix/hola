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
 * @version $Id$ 2014年10月28日
 */

public class ProviderDefinition extends AbstractServiceDefinition {

    private static final long serialVersionUID = 7508462139160267039L;

    private String protocol;
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
    private Integer payload;

    /**
     * 心跳间隔
     */
    private Integer heartbeat;

    /**
     * 网络传输方式
     */
    private String transporter;

    /**
     * 缓存区大小
     */
    private Integer buffer;

    /**
     * 信息交换方式
     */
    private String exchanger;

    /**
     * 信息线程模型派发方式
     */
    private String dispatcher;

    /**
     * 是否公告
     */
    private Boolean advertise;

    /**
     * 组网方式
     */
    private String networker;

    /**
     * 是否为默认设置
     */
    private Boolean isDefault;
    

    
    public String getProtocol() {
        return protocol;
    }

    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**   */
    public String getHost() {
        return host;
    }

    /**   */
    public void setHost(String host) {
        this.host = host;
    }

    /**   */
    public Integer getPort() {
        return port;
    }

    /**   */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**   */
    public String getContextpath() {
        return contextpath;
    }

    /**   */
    public void setContextpath(String contextpath) {
        checkPathName("contextpath", contextpath);
        this.contextpath = contextpath;
    }

    /**   */
    public String getExecutor() {
        return executor;
    }

    /**   */
    public void setExecutor(String executor) {
        this.executor = executor;
    }

    /**   */
    public Integer getThreads() {
        return threads;
    }

    /**   */
    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    /**   */
    public Integer getIothreads() {
        return iothreads;
    }

    /**   */
    public void setIothreads(Integer iothreads) {
        this.iothreads = iothreads;
    }

    /**   */
    public Integer getQueues() {
        return queues;
    }

    /**   */
    public void setQueues(Integer queues) {
        this.queues = queues;
    }

    /**   */
    public Integer getAccepts() {
        return accepts;
    }

    /**   */
    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    /**   */
    public String getCodec() {
        return codec;
    }

    /**   */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    /**   */
    public String getSerial() {
        return serial;
    }

    /**   */
    public void setSerial(String serial) {
        this.serial = serial;
    }

    /**   */
    public String getCharset() {
        return charset;
    }

    /**   */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**   */
    public Integer getPayload() {
        return payload;
    }

    /**   */
    public void setPayload(Integer payload) {
        this.payload = payload;
    }

    /**   */
    public Integer getHeartbeat() {
        return heartbeat;
    }

    /**   */
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    /**   */
    public String getTransporter() {
        return transporter;
    }

    /**   */
    public void setTransporter(String transporter) {
        this.transporter = transporter;
    }

    /**   */
    public Integer getBuffer() {
        return buffer;
    }

    /**   */
    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
    }

    /**   */
    public String getExchanger() {
        return exchanger;
    }

    /**   */
    public void setExchanger(String exchanger) {
        this.exchanger = exchanger;
    }

    /**   */
    public String getDispatcher() {
        return dispatcher;
    }

    /**   */
    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**   */
    public Boolean isAdvertise() {
        return advertise;
    }

    /**   */
    public void setAdvertise(Boolean advertise) {
        this.advertise = advertise;
    }

    /**   */
    public String getNetworker() {
        return networker;
    }

    /**   */
    public void setNetworker(String networker) {
        this.networker = networker;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
