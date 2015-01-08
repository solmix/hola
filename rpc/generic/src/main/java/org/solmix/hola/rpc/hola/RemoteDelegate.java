/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.rpc.hola;

import org.solmix.hola.common.config.ReferenceConfig;
import org.solmix.runtime.exchange.model.NamedID;


/**
 * 需要调用远程服务时,可以委派该服务类生成RemoteService或者远程服务的本地代理.
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月7日
 */

public interface RemoteDelegate {
    
    <T> T getProxy(NamedID id,Class<T> type,ReferenceConfig config);
    
    <T> RemoteService<T> createService(NamedID id,Class<T> type);

}
