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

package org.solmix.hola.osgi.rsa.support;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.common.identity.Namespace;
import org.solmix.hola.common.identity.support.DefaultIDFactory;
import org.solmix.hola.discovery.DiscoveryAdvertiser;
import org.solmix.hola.discovery.ServiceInfo;
import org.solmix.hola.discovery.ServiceProperties;
import org.solmix.hola.discovery.identity.DefaultServiceTypeFactory;
import org.solmix.hola.discovery.identity.ServiceType;
import org.solmix.hola.discovery.support.ServiceInfoImpl;
import org.solmix.hola.discovery.support.ServicePropertiesImpl;
import org.solmix.hola.osgi.rsa.AbstractMetadataFactory;
import org.solmix.hola.osgi.rsa.HolaRemoteConstants;
import org.solmix.hola.osgi.rsa.PropertiesUtil;
import org.solmix.hola.osgi.rsa.ServiceMetadataFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月12日
 */

public class ServiceMetadataFactoryImpl extends AbstractMetadataFactory
    implements ServiceMetadataFactory
{

    private static final Logger LOG= LoggerFactory.getLogger(ServiceMetadataFactoryImpl.class.getName());
    @Override
    public ServiceInfo create(DiscoveryAdvertiser advertiser,
        EndpointDescription des) {
        try {
            ServiceType type = createServiceType(des, advertiser);
            String serviceName = createServiceName(des, advertiser, type);
            URI uri = createURI(des, advertiser, type, serviceName);
            ServiceProperties serviceProperties = createServiceProperties(des,
                advertiser, type, serviceName, uri);
            ServiceInfo newServiceInfo = createMetadata(uri, serviceName,
                type, serviceProperties);
            return newServiceInfo;
        } catch (Exception e) {
            LOG.error("create service metadata faild,EndpointDescription="
                + des, e);
            return null;
        }

    }

    private ServiceInfo createMetadata(URI uri, String serviceName,
        ServiceType type, ServiceProperties serviceProperties) {
        return new ServiceInfoImpl(uri, serviceName, type,
            serviceProperties);
    }


    private ServiceProperties createServiceProperties(EndpointDescription des,
        DiscoveryAdvertiser advertiser, ServiceType type, String serviceName,
        URI uri) {
        ServiceProperties result = new ServicePropertiesImpl();
        encodeServiceProperties(des,result);
        return result;
    }


    private URI createURI(EndpointDescription des,
        DiscoveryAdvertiser advertiser, ServiceType type, String serviceName) throws URISyntaxException{
        String path=new StringBuilder().append("/").append(serviceName).toString();
        String str = des.getId();
        URI uri = null;
        while (true) {
              try {
                    uri = new URI(str);
                    if (uri.getHost() != null) {
                          break;
                    } else {
                          final String rawSchemeSpecificPart = uri
                                      .getRawSchemeSpecificPart();
                          // make sure we break eventually
                          if (str.equals(rawSchemeSpecificPart)) {
                                uri = null;
                                break;
                          } else {
                                str = rawSchemeSpecificPart;
                          }
                    }
              } catch (URISyntaxException e) {
                    uri = null;
                    break;
              }
        }
        String scheme = HolaRemoteConstants.DISCOVERY_SERVICE_TYPE;
        int port = 32565;
        if (uri != null) {
              port = uri.getPort();
              if (port == -1)
                    port = 13131;
        }
        String host = null;
        if (uri != null) {
              host = uri.getHost();
        } else {
              try {
                    host = InetAddress.getLocalHost().getHostAddress();
              } catch (Exception e) {
                    LOG.warn("failed to get local host adress, falling back to \'localhost\'.", e); 
                    host = "localhost"; 
              }
        }
        return new URI(scheme, null, host, port, path, null, null);
    }

    /**
     * @param des
     * @param advertiser
     * @param type
     * @return
     */
    private String createServiceName(EndpointDescription des,
        DiscoveryAdvertiser advertiser, ServiceType type) {
        Map<String, Object> edProp=des.getProperties();
        String serviceName=null;
        Object _sName=edProp.get(HolaRemoteConstants.DISCOVERY_SERVICE_NAME);
        if(_sName==null||!(_sName instanceof String)){
            //default Servicename
            serviceName=createDefaultServiceName(des, advertiser,type);
        }else
            serviceName=_sName.toString(); 
        return serviceName;
    }

    private String createDefaultServiceName(EndpointDescription des,
        DiscoveryAdvertiser advertiser, ServiceType type) {
        return HolaRemoteConstants.DISCOVERY_SERVICE_NAME_PREFIX+DefaultIDFactory.getDefault().createGUID();
    }

    /**
     * @param des
     * @param advertiser
     * @return
     */
    private ServiceType createServiceType(EndpointDescription des,
        DiscoveryAdvertiser advertiser) {
        Namespace ns = advertiser.getServicesNamespace();
        Map<String, Object> props = des.getProperties();
        String[] scopes = PropertiesUtil.getStringArrayWithDefault(props,
            HolaRemoteConstants.DISCOVERY_SCOPE, ServiceType.DEFAULT_SCOPE);
        String[] protocols = PropertiesUtil.getStringArrayWithDefault(props,
            HolaRemoteConstants.DISCOVERY_PROTOCOL, ServiceType.DEFAULT_SCOPE);
        String namingAuthority = PropertiesUtil.getStringWithDefault(props,
            HolaRemoteConstants.DISCOVERY_AUTH, ServiceType.DEFAULT_NA);
        return DefaultServiceTypeFactory.getDefault().create(ns,
            new String[] { HolaRemoteConstants.DISCOVERY_SERVICE_TYPE },
            scopes, protocols, namingAuthority);
    }

}
