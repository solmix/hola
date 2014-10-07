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

package org.solmix.hola.rs;

import java.util.Map;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月29日
 */

public interface RemoteRequest
{

    /**
     * 远程调用超时时间默认值,可通过hola.remotecall.timeout在System中配置,单位为毫秒.
     */
    public static final int DEFAULT_TIMEOUT = new Integer(System.getProperty(
        "hola.remote.timeout", "30000")).intValue();

    /**
     * 远程方法名,非空.
     * 
     * @return
     */
    public String getMethod();

    /**
     * 方法调用所需要的参数,数组中的参数可序列化并参数到远程服务调用端
     * 
     * @return 返回非空对象数组
     */
    public Object[] getParameters();

    /**
     * 调用超时时间，单位：毫秒．
     * 
     * @return
     */
    public int getTimeout();
    
    Map<String, String> getProperties();
   
    String getProperty(String key);
    
    String getProperty(String key, String defaultValue);
    
   
    
}
