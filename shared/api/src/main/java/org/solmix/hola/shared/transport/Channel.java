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

package org.solmix.hola.shared.transport;

import java.io.IOException;

import org.solmix.hola.core.HolaException;
import org.solmix.hola.core.identity.ID;

/**
 * 传输层链接语义.
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年6月24日
 */

public interface Channel
{

    /**
     * 连接远程进程
     * 
     * @param targetId 远程连接ID,非空.
     * @param authData 连接是附带的数据,通常用于发送密码等一些验证信息
     * @param timeout 超时等待毫秒数
     * @return 返回对象,具体内容视不同实现方式而不同
     * @throws HolaException
     */
    Object connect(ID targetId, Object authData, int timeout)
        throws HolaException;
    
    /**
     * 中断连接
     */
    void disconnect();
    
    /**
     * 返回当前链接是否为连接状态
     * @return
     */
    boolean isConnected();
    
    /**
     * 本地Id
     * @return
     */
    ID getLocalID();
    
    /**
     * Start connection
     */
    public void start();

    /**
     * Stop connection
     */
    public void stop();

    /**
     * 
     * @return true if connection is started, false otherwise
     */
    public boolean isStarted();

    /**
     * Get properties for this connection
     * 
     * @return Map the properties associated with this connection. May be null.
     */
    /**
     * get attribute.
     * 
     * @param key key.
     * @return value.
     */
    Object getAttribute(String key);

    /**
     * set attribute.
     * 
     * @param key key.
     * @param value value.
     */
    void setAttribute(String key,Object value);
    
    /**
     * remove attribute.
     * 
     * @param key key.
     */
    void removeAttribute(String key);
    
//    public void addListener(IConnectionListener listener);
//    public void removeListener(IConnectionListener listener);

    /**
     * 异步发送数据,发送数据后应该立即返回,不应该被阻塞.
     * 
     * @param receiver
     * @param data
     * @throws IOException
     */
    void send(ID receiver, byte[] data) throws IOException;

    /**
     * 同步发送数据,发送数据后阻塞至道返回结果.
     * 
     * @param receiver
     * @param data
     * @return
     * @throws IOException
     */
//    Object send(ID receiver, byte[] data) throws IOException;

}
