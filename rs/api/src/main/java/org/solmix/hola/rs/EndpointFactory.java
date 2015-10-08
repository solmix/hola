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

package org.solmix.hola.rs;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.ArrayUtils;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.exchange.Endpoint;
import org.solmix.exchange.EndpointException;
import org.solmix.exchange.EndpointInfoFactory;
import org.solmix.exchange.ProtocolFactoryManager;
import org.solmix.exchange.Service;
import org.solmix.exchange.TransporterFactory;
import org.solmix.exchange.TransporterFactoryManager;
import org.solmix.exchange.data.DataProcessor;
import org.solmix.exchange.event.ServiceFactoryEvent;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.model.NamedID;
import org.solmix.exchange.model.ProtocolInfo;
import org.solmix.exchange.model.ServiceInfo;
import org.solmix.exchange.support.AbstractEndpointFactory;
import org.solmix.exchange.support.ReflectServiceFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.ConfigSupportedReference;
import org.solmix.hola.common.util.ServicePropertiesUtils;
import org.solmix.hola.transport.RemoteAddress;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年11月21日
 */

public abstract class EndpointFactory extends AbstractEndpointFactory {

    private static final long serialVersionUID = 121982796534012439L;
    
    private static final Logger LOG = LoggerFactory.getLogger(EndpointFactory.class);

    private Class<?> serviceClass;

    private ReflectServiceFactory serviceFactory;
    
    private DataProcessor dataProcessor;

    protected EndpointFactory(ReflectServiceFactory factory) {
        this.serviceFactory = factory;
        this.serviceClass = factory.getServiceClass();
    }

    protected EndpointFactory() {

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Endpoint createEndpoint() throws EndpointException {

        if (getServiceName() != null) {
            serviceFactory.setServiceName(getServiceName());
        }
        if (getEndpointName() != null) {
            serviceFactory.setEndpointName(getEndpointName());
        }
        Service service = serviceFactory.getService();
        // 初始化ServiceFactory,创建service
        if (service == null) {
            initializeServiceFactory();
            service = serviceFactory.create();
        }
        if (getEndpointName() == null) {
            setEndpointName(serviceFactory.getEndpointName());
        }

        EndpointInfo info = service.getEndpointInfo(getEndpointName());
        if (info != null) {
            //传输不一致
            if (transporter != null
                && !transporter.equals(info.getTransporter())) {
                info = null;
            } else {
                ProtocolFactoryManager pfm = getContainer().getExtension(
                    ProtocolFactoryManager.class);
                protocolFactory = pfm.getProtocolFactory(info.getProtocol().getProtocolId());
            }
        }
        if (info == null) {
            //service中查找
           List<ServiceInfo> sinfos= service.getServiceInfos();
           for(ServiceInfo si:sinfos){
               for(EndpointInfo ei:si.getEndpoints()){
                   if(ei.getInterface().getName().equals(service.getServiceName())){
                       info=ei;
                   }
               }
           }
           if(info!=null){
               ProtocolFactoryManager pfm = getContainer().getExtension(
                   ProtocolFactoryManager.class);
               protocolFactory = pfm.getProtocolFactory(info.getProtocol().getProtocolId());
           }
           //创建一个
           if(info==null){
               info=createEndpointInfo(null);
               //传输层变了.
           }else  if (transporter != null
               && !transporter.equals(info.getTransporter())) {
               ProtocolInfo ptl = info.getProtocol();
               info = createEndpointInfo(ptl);
           }
        }
        
        Endpoint ep = service.getEndpoints().get(info.getName());
        
        if(ep==null){
            //创建endpoint
            ep = serviceFactory.createEndpoint(info);
        }else{
            serviceFactory.setEndpointName(info.getName());
        }
        if (properties != null) {
            Enumeration<String> keys=properties.keys();
            while(keys.hasMoreElements()){
                String key = keys.nextElement();
                Object value = properties.get(key);
                Dictionary dic= getServiceFactory().getProperties();
                dic.put(key, value);
            }
        }
        //建立关联
        service.getEndpoints().put(ep.getEndpointInfo().getName(), ep);

        if (getInInterceptors() != null) {
            ep.getInInterceptors().addAll(getInInterceptors());
        }
        if (getOutInterceptors() != null) {
            ep.getOutInterceptors().addAll(getOutInterceptors());
        }
        if (getInFaultInterceptors() != null) {
            ep.getInFaultInterceptors().addAll(getInFaultInterceptors());
        }
        if (getOutFaultInterceptors() != null) {
            ep.getOutFaultInterceptors().addAll(getOutFaultInterceptors());
        }
        serviceFactory.pulishEvent(ServiceFactoryEvent.ENDPOINT_SELECTED, info, ep,
                                 serviceFactory.getServiceClass(), getServiceClass());
        return ep;
    }

    
    /**
     * @return
     */
    protected EndpointInfo createEndpointInfo(ProtocolInfo ptl) {
        Service service = serviceFactory.getService();
        // 创建protocolInfo
        if (ptl == null) {
            ptl = createProtocolInfo();
            service.getServiceInfo().addProtocol(ptl);
        }

        if (transporter == null) {
            if (transporter == null && getAddress() != null && getAddress().contains("://")) {
                transporter = getTransportTypeForAddress(getAddress());
            }
            if (transporter == null) {
                transporter = HOLA.DEFAULT_TRANSPORTER;
            }
        }
        if (transporterFactory == null) {
            transporterFactory = getTransporterFactory(transporter);
        }

        EndpointInfoFactory eif = getEndpointInfoFactory();
        EndpointInfo endpointInfo;
        if (eif != null) {
            endpointInfo = eif.createEndpointInfo(getContainer(), service.getServiceInfo(), ptl, getProperties());
            endpointInfo.setTransportor(transporter);
        } else {
            endpointInfo = new EndpointInfo(service.getServiceInfo(), transporter);
        }
        int count = 1;
        while (service.getEndpointInfo(endpointName) != null) {
            endpointName = new NamedID(endpointName.getServiceNamespace(), endpointName.getName() + count);
            count++;
        }
        endpointInfo.setName(endpointName);
        endpointInfo.setProtocol(ptl);

        service.getServiceInfo().addEndpoint(endpointInfo);
        // 根据transporerFactry的supportConfigs创建configedBean并加入
        // XXX
        makeupConfigReference(endpointInfo, transporterFactory);

        RemoteAddress ra  = new RemoteAddress(getProperties());
        if(getAddress()==null){
            setAddress(ra.getAddress());
        }
        endpointInfo.setAddress(getAddress());
        endpointInfo.addExtension(ra);
        serviceFactory.pulishEvent(ServiceFactoryEvent.ENDPOINTINFO_CREATED, endpointInfo);
        return endpointInfo;
    }
  

    protected void makeupConfigReference(EndpointInfo endpointInfo,Object... factorys){
        if(factorys!=null&&factorys.length>0){
            for(Object factory:factorys){
                if(factory instanceof ConfigSupportedReference){
                    makeConfigAsEndpointInfoExtension((ConfigSupportedReference)factory,endpointInfo,getProperties());
                }
            } 
        }
    }

    /**把ConfigSupportedReference的配置放入extension中*/
    protected  void makeConfigAsEndpointInfoExtension(
        ConfigSupportedReference config, 
        EndpointInfo endpointInfo,
        Dictionary<String, ?> properties) {
        String[] supported = config.getSupportedConfigs(properties);
        Class<?> clazz = config.getSupportedConfigClass();
        if(!ArrayUtils.isEmptyArray(supported)&&clazz!=null){
            try {
                Object bean = Reflection.newInstance(clazz);
                Map<String,Object> copyed = new HashMap<String,Object>();
                for(String key:supported){
                    Object value = properties.get(key);
                    if(value!=null){
                        copyed.put(key, properties.get(key));
                    }
                }
                DataUtils.setProperties(copyed, bean, false);
                endpointInfo.addExtension(bean);
            } catch (Exception e) {
               LOG.warn("Make ConfigSupportedReference into EndpointInfo extensions",e);
            }
        }
    }

    protected TransporterFactory getTransporterFactory(String transporter) {
        TransporterFactory transporterFactory = getContainer().getExtension(
            TransporterFactoryManager.class).getFactory(transporter);
        return transporterFactory;
    }

    /**
     * 用于生成EndpointInfo的工厂.
     * 
     * @return
     */
    protected   EndpointInfoFactory getEndpointInfoFactory(){
        final TransporterFactory tf = transporterFactory;
        if(tf instanceof EndpointInfoFactory){
            return (EndpointInfoFactory)tf;
        }
        return defaultEndpointInfoFactory();
    }

    protected EndpointInfoFactory defaultEndpointInfoFactory() {
        return null;
    }

    /**
     * 根据配置的服务发布地址,检查使用协议类型.
     * 
     * @param address
     * @return
     */
    protected abstract String getTransportTypeForAddress(String address) ;

    /**
     * @return
     */
    protected ProtocolInfo createProtocolInfo() {

        String ptl = getProtocol();
        if (ptl == null) {
            ptl = HOLA.DEFAULT_PROTOCOL;
        }
        ProtocolFactoryManager pfm = getContainer().getExtension(
            ProtocolFactoryManager.class);

        protocolFactory = pfm.getProtocolFactory(ptl);

        ProtocolInfo pi = protocolFactory.createProtocolInfo(
            serviceFactory.getService(), ptl,getProperties());
        serviceFactory.pulishEvent(ServiceFactoryEvent.PROTOCOL_CREATED, pi);
        return pi;
    }

    /**
     * 
     */
    protected void initializeServiceFactory() {
        Class<?> cls = getServiceClass();
        serviceFactory.setServiceClass(cls);
        serviceFactory.setContainer(getContainer());
        if (dataProcessor != null) {
            serviceFactory.setDataProcessor(dataProcessor);
        }
    }

    /**   */
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    
    /**   */
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    
    /**   */
    public ReflectServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    
    /**   */
    public void setServiceFactory(ReflectServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    
    /**   */
    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    
    /**   */
    public void setDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }
    
    @Override
    public String getAddress() {
        //通过配置设置address
        if(address==null&&getProperties()!=null){
           address=ServicePropertiesUtils.toAddress(getProperties());
        }
        return address;
    }
}
