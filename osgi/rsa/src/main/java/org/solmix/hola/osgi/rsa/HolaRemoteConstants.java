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
package org.solmix.hola.osgi.rsa;

import org.solmix.hola.discovery.identity.ServiceType;



/**
 * Hola remote service Admin,指定了Hola RSA中元数据的参数,关于OSGI中的元数据参考{@link org.osgi.service.remoteserviceadmin.RemoteConstants}
 * 或者<a href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGI 4.2 Remote
 * Service Admin specification (chap 122)</a>.
 * @author solmix.f@gmail.com
 * @version $Id$  2014年4月2日
 */

public class HolaRemoteConstants
{

    public static final String ENDPOINT_ID = "hola.endpoint.id";
    public static final String ENDPOINT_NAMESPACE = "hola.endpoint.id.ns";
    public static final String ENDPOINT_CONNECTTARGET_ID = "hola.endpoint.connecttarget.id";
    public static final String ENDPOINT_REMOTESERVICE_FILTER ="hola.endpoint.rsfilter";
    public static final String ENDPOINT_IDFILTER_IDS ="hola.endpoint.idfilter.ids";
    public static final String SERVICE_EXPORTED_ASYNC_INTERFACES = "hola.exported.asyn.interfaces";
    public static final String SERVICE_PREVENT_ASYNCPROXY = null;
    public static final String ENDPOINT_TIMESTAMP="hola.endpoint.ts";
    public static final String DISCOVERY_SCOPE="hola.endpoint.discovery.scope";
    public static final String DISCOVERY_PROTOCOL="hola.endpoint.discovery.protocol";
    public static final String DISCOVERY_AUTH="hola.endpoint.discovery.namingauth";
    /**
     *Hola Discovery-service 服务类型,所有Hola Remoting-service发布的远程服务都必须在
     *{@link ServiceType#getServices()}中包含该配置.
     */
    public static final String DISCOVERY_SERVICE_TYPE="osgirsa";
    public static final String DISCOVERY_SERVICE_NAME="hola.endpoint.discovery.servicename";
    public static final String DISCOVERY_SERVICE_NAME_PREFIX="osgirsa_";
    public static final Object SERVICE_EXPORTED_PROVIDER_ID = "service.exported.providerID";
    public static final Object SERVICE_EXPORTED_SECURITY_CONTEXT =  "service.exported.security";
    public static final Object SERVICE_IMPORTED_VALUETYPE = null;;
    

}
