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

import org.solmix.hola.transport.RemoteProtocol;



/**
 * Netty引擎
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月18日
 */

public interface NettyEngine {
    
    /**
     * 启动服务并指定传输协议。<br>
     * <b>同一个端口不能解析不同协议</b>
     * 
     * @param protocol
     */
    void start(RemoteProtocol protocol);

    /**
     * 添加消息处理回调
     * @param path
     * @param handler
     */
    void addHandler(String path, NettyMessageHandler handler);

    /**
     * 移除消息处理回调
     * @param path
     */
    void removeHandler(String path);
    
    /**
     * 取得path对应的handler
     * 
     * @param path
     * @return
     */
    NettyMessageHandler getHandler(String path);

    /**
     * 关闭引擎
     */
    void shutdown();
}
