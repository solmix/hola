package org.solmix.hola.builder.delegate;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.cluster.Cluster;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.DefaultServiceID;
import org.solmix.hola.common.model.DefaultServiceType;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.common.model.ServiceType;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteListener;
import org.solmix.hola.rs.RemoteProxyFactory;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.support.AbstractRemoteServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;
import org.solmix.runtime.extension.ExtensionLoader;

@Extension(name=DelegateRemoteServiceFactory.NAME)
public class DelegateRemoteServiceFactory implements RemoteServiceFactory,ContainerAware
{

    private static final Logger LOG  = LoggerFactory.getLogger(DelegateRemoteServiceFactory.class);
    public static final String NAME ="delegate";
    private final Map<String, RemoteRegistrationWrapper<?>> bounds = new ConcurrentHashMap<String, RemoteRegistrationWrapper<?>>();
    
    private Container container;
    private ExtensionLoader<RemoteServiceFactory> extensionLoader;
    
    @Override
    public RemoteRegistration<?> register(String clazz, Object service, Dictionary<String, ?> properties) throws RemoteException {

        if (StringUtils.isEmpty(clazz)) {
            throw new IllegalArgumentException("register class  is null");
        }
        @SuppressWarnings("unchecked")
        Class<Object> cls = (Class<Object>) AbstractRemoteServiceFactory.checkServiceClass(clazz, service);
        if (cls == null) {
            throw new RemoteException("No found class :" + clazz);
        }
        RemoteRegistration<Object> reg = register(cls, service, properties);
        return reg;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <S> RemoteRegistration<S> register(Class<S> clazz, S service, Dictionary<String, ?> properties) throws RemoteException {
        final RemoteRegistrationWrapper<S> reg = doLocalRegister(clazz,service,properties);
        Dictionary<String, Object> discoveryInfo  = (Dictionary<String, Object>)properties.get(HOLA.DISCOVERY_KEY);
        final Discovery discovery = getDiscovery(discoveryInfo);
        final DiscoveryInfo serviceInfo = getDiscoveryInfo(properties);
        discovery.register(serviceInfo);
        return new RemoteRegistration<S>() {

            @Override
            public RemoteReference<S> getReference() {
                return reg.getReference();
            }

            @Override
            public void unregister() {
                try {
                    reg.unregister();
              } catch (Throwable t) {
                  LOG.warn(t.getMessage(), t);
            }
            try {
                discovery.unregister(serviceInfo);
            } catch (Throwable t) {
                LOG.warn(t.getMessage(), t);
            }
                
                
            }
        };
    }
   

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DiscoveryInfo getDiscoveryInfo(Dictionary properties) {
       Dictionary<String, Object> serviceProp = new Hashtable<String, Object>();
        PropertiesUtils.filterCopy(properties, serviceProp, HOLA.MONITOR_KEY);
        DiscoveryInfo info  = new DiscoveryInfoImpl(serviceProp);
        return info;
    }

    private Discovery getDiscovery(Dictionary<String, Object> discoveryInfo) {
        String protocol = PropertiesUtils.getString(discoveryInfo, HOLA.PROTOCOL_KEY);
        DiscoveryProvider provider= container.getExtensionLoader(DiscoveryProvider.class).getExtension(protocol);
        return provider.createDiscovery(discoveryInfo);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <S> RemoteRegistrationWrapper<S> doLocalRegister(Class<S> clazz, S service, Dictionary dic) {
        String key = PropertiesUtils.toAddress(dic);
        RemoteRegistrationWrapper<S> reg = (RemoteRegistrationWrapper<S>) bounds.get(key);
        if (reg == null) {
            synchronized (bounds) {
                reg = (RemoteRegistrationWrapper<S>) bounds.get(key);
                if (reg == null) {
                    String protocol = PropertiesUtils.getString(dic, HOLA.PROTOCOL_KEY);
                    RemoteServiceFactory factory = getRemoteServiceFactory(protocol);
                    RemoteRegistration<S> registration = factory.register(clazz, service, dic);
                    reg = new RemoteRegistrationWrapper<S>(registration, key);
                    bounds.put(key, reg);
                }
            }
        }
        return reg;
    }

    private synchronized RemoteServiceFactory getRemoteServiceFactory(String protocol) {
        if(extensionLoader==null){
            extensionLoader= container.getExtensionLoader(RemoteServiceFactory.class);
        }
        return extensionLoader.getExtension(protocol);
    }

   
    private class RemoteRegistrationWrapper<S> implements RemoteRegistration<S>{

        private RemoteRegistration<S> reg;
        private String key;
        private RemoteRegistrationWrapper(RemoteRegistration<S> reg,String key){
            this.reg=reg;
            this.key=key;
        }
        @Override
        public RemoteReference<S> getReference() {
            return reg.getReference();
        }

        @Override
        public void unregister() {
            bounds.remove(key);
            reg.unregister();
            
        }
        
    }
    private class RemoteReferenceWrapper<S> implements RemoteReference<S>{

        private Class<S> clazz;
        private Dictionary<String, ?> properties;
        public RemoteReferenceWrapper(Class<S> clazz, Dictionary<String, ?> properties){
            this.clazz=clazz;
            this.properties=properties;
        }
        public Dictionary<String, ?> getProperties(){
            return properties;
        }
        @Override
        public org.solmix.hola.rs.RemoteReference.ReferenceType getReferenceType() {
            return null;
        }

        @Override
        public Object getProperty(String key) {
            return null;
        }

        @Override
        public String[] getPropertyKeys() {
            return null;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void destroy() {
            
        }

        @Override
        public Class<S> getServiceClass() {
            return clazz;
        }

        @Override
        public RemoteServiceFactory getRemoteServiceFactory() {
            return DelegateRemoteServiceFactory.this;
        }
        
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties) {
        return new RemoteReferenceWrapper(clazz,properties);
    }

    @Override
    public <S> S getService(RemoteReference<S> reference) {
        RemoteService<S> rs = getRemoteService(reference);
        S obj = RemoteProxyFactory.getProxy(rs,container);
        return  obj;
    }

    @Override
    public <S> RemoteService<S> getRemoteService(RemoteReference<S> reference) {
        RemoteReferenceWrapper<S> refer =(RemoteReferenceWrapper)reference;
        Dictionary<String, ?> prop =refer.getProperties();
        String interfaceName = PropertiesUtils.getServiceInterface(prop);
        String group = PropertiesUtils.getString(prop, HOLA.GROUP_KEY);
        ServiceType type  =new  DefaultServiceType(interfaceName,group,HOLA.CONSUMER_CATEGORY);
        ServiceID id  = new DefaultServiceID(type, prop);
        
        DiscoveriedDirectory directory = new DiscoveriedDirectory<S>(container, id, refer.getServiceClass());
        Dictionary<String, Object> discoveryInfo  = (Dictionary<String, Object>)prop.get(HOLA.DISCOVERY_KEY);
        final Discovery discovery = getDiscovery(discoveryInfo);
        directory.setDiscovery(discovery);
        String protocol = PropertiesUtils.getString(prop, HOLA.PROTOCOL_KEY);
        
        RemoteServiceFactory factory = container.getExtensionLoader(RemoteServiceFactory.class).getExtension(protocol);
        
        directory.setRemoteServiceFactory(factory);
        directory.addDiscoveryListener();
        String clusterType = PropertiesUtils.getString(refer.getProperties(), HOLA.CLUSTER_KEY);
        Cluster cluster = container.getExtensionLoader(Cluster.class).getExtension(clusterType);
        RemoteService<S> remoteService=cluster.join(directory);
        return remoteService;
    }

    @Override
    public void addRemoteListener(RemoteListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRemoteListener(RemoteListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
            List<RemoteRegistrationWrapper<?>> exporters = new ArrayList<RemoteRegistrationWrapper<?>>(bounds.values());
            for(RemoteRegistrationWrapper<?> exporter :exporters){
                exporter.unregister();
            }
            bounds.clear();
    }

    @Override
    public void setContainer(Container container) {
        this.container=container;
    }


}
