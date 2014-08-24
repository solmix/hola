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
package org.solmix.hola.rs.generic;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.framework.InvalidSyntaxException;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.Assert;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.rs.RemoteConnectException;
import org.solmix.hola.rs.RemoteFilter;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceListener;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.hola.rs.RemoteServiceReference;
import org.solmix.hola.rs.RemoteServiceRegistration;
import org.solmix.hola.rs.RemoteServiceException;
import org.solmix.hola.rs.event.RemoteServiceEvent;
import org.solmix.hola.rs.event.RemoteServiceRegisteredEvent;
import org.solmix.hola.rs.event.RemoteServiceUnregisteredEvent;
import org.solmix.hola.transport.TransportException;
import org.solmix.hola.transport.exchange.ExchangeChannel;
import org.solmix.hola.transport.exchange.ExchangeClient;
import org.solmix.hola.transport.exchange.ExchangeHandler;
import org.solmix.hola.transport.exchange.ExchangeHandlerAdaptor;
import org.solmix.hola.transport.exchange.ExchangeServer;
import org.solmix.hola.transport.exchange.ExchangerProvider;
import org.solmix.runtime.Container;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月17日
 */
@ThreadSafe
public class HolaRemoteServiceManager implements RemoteServiceManager
{
    protected final List<RemoteServiceListener> listeners=new ArrayList<RemoteServiceListener>();
    private final Map<String, ExchangeServer> servers = new ConcurrentHashMap<String, ExchangeServer>(); // <host:port,Exchanger>
    private final Map<String, ReferenceCountExchangeClient> clients = new ConcurrentHashMap<String, ReferenceCountExchangeClient>(); // <host:port,Exchanger>

    private final Container container;
    protected Map<ID, HolaRemoteServiceRegistration<?>>  publishedServices = 
        new ConcurrentHashMap<ID, HolaRemoteServiceRegistration<?>>(50);
    private final ExchangeHandler handler=new ExchangeHandlerAdaptor(){
        
        @Override
        public Object reply(ExchangeChannel channel, Object msg)
            throws TransportException {
            return null;
        }
    };
    public HolaRemoteServiceManager(final Container container,RemoteServiceListener...listeners ){
        this.container=container;
        if(listeners!=null){
            for(RemoteServiceListener listener:listeners){
                addRemoteServiceListener(listener);
            }
        }
        
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#registerRemoteService(java.lang.String[], java.lang.Object, java.util.Map)
     * @param properties {@link org.solmix.hola.osgi.rsa.HolaRemoteServiceAdmin#createExportEndpointDescriptionProperties}
     */
    @Override
    public RemoteServiceRegistration<?> registerRemoteService(String[] clazzes,
        Object service, RemoteInfo info) throws RemoteServiceException{
        Assert.isNotNull(service, "register service is null");
        if (clazzes==null || clazzes.length == 0) {
            throw new IllegalArgumentException( "Service classes list is empty");
        }
        final String[] copy = new String[clazzes.length];
        for (int i = 0; i < clazzes.length; i++) {
            copy[i] = new String(clazzes[i].getBytes());
        }
        clazzes = copy;
        final String invalidService = checkServiceClass(clazzes, service);
        if (invalidService != null) {
            throw new IllegalArgumentException("Service=" + invalidService
                + " is invalid");
        }
        final HolaRemoteServiceRegistration<Object> reg 
        = new HolaRemoteServiceRegistration<Object>(this, clazzes, service, info);
        adapteServer(info);
        publishedServices.put(reg.getID(), reg);
        fireRemoteServiceListeners(createRegisteredEvent(reg));
        return reg;
    }
    
    /**
     * @param reg
     */
    private void adapteServer(RemoteInfo info) {
       boolean isServer= info.getServer(true);
       String key = info.getAddress();
       if(isServer){
           ExchangeServer server=  servers.get(key);
           if(server==null){
               servers.put(key, createServer(info));
           }else{
               server.refresh(info);
           }
       }
    }
  
    /**
     * @param param
     * @return
     */
    private ExchangeServer createServer(RemoteInfo info) {
        RemoteInfo.Builder b= RemoteInfo.newBuilder(info);
        b.setCodec(HolaCodec.CODEC_NAME)
        .setServer(true)
        .setPropertyIfAbsent(RemoteInfo.HEARTBEAT, HolaConstants.DEFAULT_HEARTBEAT)
        .setPropertyIfAbsent(RemoteInfo.TRANSPORT, HolaConstants.DEFAULT_TRANSPORTER)
        .setPropertyIfAbsent(RemoteInfo.EXCHANGER, HolaConstants.DEFAULT_EXCHANGER);
        info=b.build();
        ExchangeServer server=null;
        try {
            ExchangerProvider provider=   container.getExtensionLoader(ExchangerProvider.class).getExtension(info.getExchanger());
            server=provider.bind(info,handler);
        } catch (TransportException e) {
           throw new RemoteServiceException("Failed to start server ",e);
        }
        return server;
    }


    protected  void unregisterRemoteService(HolaRemoteServiceRegistration<?> reg){
        
        fireRemoteServiceListeners(createUnregisteredEvent(reg));
        publishedServices.remove(reg.getID());
    }
 

    /**
     * @param createRegisteredEvent
     */
    protected void fireRemoteServiceListeners(
        RemoteServiceEvent registeredEvent) {
        List<RemoteServiceListener> entries;
        synchronized (listeners) {
              entries = new ArrayList<RemoteServiceListener>(listeners);
        }
        for(RemoteServiceListener listener:entries){
            listener.onHandle(registeredEvent);
        }
    }

    /**
     * @param reg
     * @return
     */
    private RemoteServiceRegisteredEvent createRegisteredEvent(
        HolaRemoteServiceRegistration<Object> reg) {
        return new RemoteServiceRegisteredEvent(reg.getReference());
    }
    /**
     * @param reg
     * @return
     */
    private RemoteServiceUnregisteredEvent createUnregisteredEvent(
        HolaRemoteServiceRegistration<?> reg) {
        return new RemoteServiceUnregisteredEvent(reg.getReference());
    }
  
   
    @Override
    public RemoteServiceReference<?> getRemoteServiceReferences(
        String clazz,RemoteInfo info) throws InvalidSyntaxException, RemoteConnectException {
        return new HolaRemoteServiceReference<Object>(clazz,info,this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#getAllRemoteServiceReferences(java.lang.String, java.lang.String)
     */
    @Override
    public RemoteServiceReference<?>[] getAllRemoteServiceReferences(
        String clazz, String filter) throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#getRemoteServiceNamespace()
     */
    @Override
    public Namespace getRemoteServiceNamespace() {
        return IDFactory.getDefault().getNamespaceByName(HolaNamespace.NAME);
    }

    private final Map<RemoteServiceReference<?>,RemoteService> remoteServices=
        new ConcurrentHashMap<RemoteServiceReference<?>,RemoteService>();
    
    private final ConcurrentMap<String, LazyConnectExchangeClient> ghostClients 
    = new ConcurrentHashMap<String, LazyConnectExchangeClient>();

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#getRemoteService(org.solmix.hola.rs.generic.RemoteServiceReference)
     */
    @Override
    public RemoteService getRemoteService(RemoteServiceReference<?> reference) {
        // TODO Auto-generated method stub
        return null;
    }
    public RemoteService getRemoteService(String clazz,RemoteInfo info) {
        ExchangeClient[] clients= getClients(info);
        HolaRemoteService rs= new HolaRemoteService(new String[]{clazz},info,clients,this);
        return rs;
    }
    
    private ExchangeClient[] getClients(RemoteInfo info) {
       int connections=info.getConnections(0);
       boolean shared=false;
       if(connections==0){
           shared=true;
           connections=1;
       }
       ExchangeClient[] clients = new ExchangeClient[connections];
       for (int i = 0; i < clients.length; i++) {
           if (shared){
               clients[i] = getSharedClient(info);
           } else {
               clients[i] = initClient(info);
           }
       }
       return clients;
    }
    /**
     * @param info
     * @return
     */
    private ExchangeClient getSharedClient(RemoteInfo info) {
        String key=info.getAddress();
        clients.get(key);
        ReferenceCountExchangeClient client = clients.get(key);
        if ( client != null ){
            if ( !client.isClosed()){
                client.incrementAndGetCount();
                return client;
            } else {
                clients.remove(key);
            }
        }
        ExchangeClient exchagneclient = initClient(info);
        
        client = new ReferenceCountExchangeClient(exchagneclient, ghostClients,this);
        clients.put(key, client);
        ghostClients.remove(key);
        return client; 
    }
    /**
     * @param info
     * @return
     */
    private ExchangeClient initClient(RemoteInfo info) {
        info= info.addPropertyIfAbsent(RemoteInfo.HEARTBEAT, HolaConstants.DEFAULT_HEARTBEAT);
        ExchangeClient client ;
        try{
            if(info.getBoolean("lazy",false)){
                client=new LazyConnectExchangeClient(info, handler, this);
            }else{
                ExchangerProvider provider=   container
                    .getExtensionLoader(ExchangerProvider.class)
                    .getExtension(info.getExchanger(HolaConstants.DEFAULT_EXCHANGER));
                client= provider.connect(info, handler);
            }
            
        }catch(TransportException e){
            throw new RemoteServiceException("Failed to create remote service for url:"+info.toString(),e);
        }
        return client;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#ungetRemoteService(org.solmix.hola.rs.generic.RemoteServiceReference)
     */
    @Override
    public boolean ungetRemoteService(RemoteServiceReference<?> reference) {
       if(reference==null){
           return false;
       }
       if(reference.getID()==null){
           return false;
       }
       RemoteService rs= remoteServices.remove(reference);
       if(rs!=null){
           //     shutdown rs
           return true;
       }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#createRemoteFilter(java.lang.String)
     */
    @Override
    public RemoteFilter createRemoteFilter(String filter)
        throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#addRemoteServiceListener(org.solmix.hola.rs.generic.RemoteServiceListener)
     */
    @Override
    public void addRemoteServiceListener(RemoteServiceListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
      }
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#removeRemoteServiceListener(org.solmix.hola.rs.generic.RemoteServiceListener)
     */
    @Override
    public void removeRemoteServiceListener(RemoteServiceListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
      }
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.generic.RemoteServiceProvider#destroy()
     */
    @Override
    public void destroy() {
       publishedServices.clear();
        synchronized (listeners) {
            listeners.clear();
        }
        
    }
    /**
     * 检查服务是否为接口的实例
     * @param clazzes
     * @param service
     * @return
     */
    static String checkServiceClass(final String[] clazzes, final Object service) {
        final ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return service.getClass().getClassLoader();
            }
        });
        for (int i = 0; i < clazzes.length; i++) {
            try {
                final Class<?> serviceClazz = cl == null ? Class.forName(clazzes[i])
                    : cl.loadClass(clazzes[i]);
                if (!serviceClazz.isInstance(service)) {
                    return clazzes[i];
                }
            } catch (final ClassNotFoundException e) {
                // This check is rarely done
                if (extensiveCheckServiceClass(clazzes[i], service.getClass())) {
                    return clazzes[i];
                }
            }
        }
        return null;
    }
    /**
     * 检查父类和接口
     * @param clazz
     * @param serviceClazz
     * @return
     */
    static boolean extensiveCheckServiceClass(String clazz,
        Class<?> serviceClazz) {
        if (clazz.equals(serviceClazz.getName())) {
            return false;
        }
        final Class<?>[] interfaces = serviceClazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (!extensiveCheckServiceClass(clazz, interfaces[i])) {
                return false;
            }
        }
        final Class<?> superClazz = serviceClazz.getSuperclass();
        if (superClazz != null) {
            if (!extensiveCheckServiceClass(clazz, superClazz)) {
                return false;
            }
        }
        return true;
    }
    protected HolaServiceID createRemoteServiceID(String serviceName,String version,String group,int port) {
        Namespace ns = IDFactory.getDefault().getNamespaceByName( HolaNamespace.NAME);
        return (HolaServiceID) IDFactory.getDefault().createID(ns,
            new Object[] { getStringUrl(serviceName,version,group,new Integer(port)) });
    }
    protected HolaServiceID createRemoteServiceID(RemoteInfo info) {
        Namespace ns = IDFactory.getDefault().getNamespaceByName( HolaNamespace.NAME);
       String strUrl=getStringUrl(info.getPath(),info.getVersion(),info.getGroup(),info.getPort());
        return (HolaServiceID) IDFactory.getDefault().createID(ns,
            new Object[] { strUrl });
    }
   
    private String getStringUrl(String path, String version, String group,
        Integer port) {
       StringBuilder sb= new StringBuilder();
       if(group!=null){
           sb.append(group).append("/");
       }
       sb.append(path).append(":");
       sb.append(port);
       if(version!=null&&"0.0.0".equals(version.trim())){
           sb.append(":");
           sb.append(version);
       }
        return sb.toString();
    }
    /**
     * @param info
     * @param requestHandler
     * @return
     * @throws TransportException 
     */
    public ExchangeClient createClient(RemoteInfo info,
        ExchangeHandler requestHandler) throws TransportException {
        return container.getExtensionLoader(ExchangerProvider.class)
            .getExtension(info.getExchanger())
            .connect(info, requestHandler);
    }

}
