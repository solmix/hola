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

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年9月6日
 */

public class ServerConfig extends AbstractServiceConfig {
	/**
     * 
     */
	private static final long serialVersionUID = -4040109969678654602L;

	/**
	 * 主机
	 */
	private String host;

	/**
	 * 服务端口
	 */
	private Integer port;
	 // 延迟暴露
	    protected Integer              delay;
	/**
     * 
     */
	private String codec;

	private String  executor;
	   
	private Integer queues;
	
	private Integer accepts;
	
	private String contextpath;
	
	private String serialization;
	
	private String charset;
	
	private Integer buffer;

	
	public ServerConfig(String protocol){
	    setProtocol(protocol);
	}
    /**
     * @return the delay
     */
    public Integer getDelay() {
        return delay;
    }

    
    /**
     * @param delay the delay to set
     */
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    /**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
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
	 * @param port
	 *            the port to set
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
	 * @param codec
	 *            the codec to set
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
	 * @param queues
	 *            the queues to set
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
	 * @param accepts
	 *            the accepts to set
	 */
	public void setAccepts(Integer accepts) {
		this.accepts = accepts;
	}

	/**
	 * @return the contextpath
	 */
	public String getContextpath() {
		return contextpath;
	}

	/**
	 * @param contextpath
	 *            the contextpath to set
	 */
	public void setContextpath(String contextpath) {
		this.contextpath = contextpath;
	}

	/**
	 * @return the serialization
	 */
	public String getSerialization() {
		return serialization;
	}

	/**
	 * @param serialization
	 *            the serialization to set
	 */
	public void setSerialization(String serialization) {
		this.serialization = serialization;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            the charset to set
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
	 * @param buffer
	 *            the buffer to set
	 */
	public void setBuffer(Integer buffer) {
		this.buffer = buffer;
	}

	/**
	 * @return the executor
	 */
	public String getExecutor() {
		return executor;
	}

	/**
	 * @param executor the executor to set
	 */
	public void setExecutor(String executor) {
		this.executor = executor;
		checkExtension(ExecutorProvider.class, "executor", executor);
	}
}
