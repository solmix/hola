package org.solmix.hola.builder.delegate;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.solmix.hola.cluster.ConsumerInfo;
import org.solmix.hola.cluster.Router;
import org.solmix.hola.cluster.directory.AbstractDirectory;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;


public class DiscoveriedDirectory<T> extends AbstractDirectory<T> implements ServiceTypeListener
{
    private final Class<T> serviceType;
    private Discovery discovery;
    private RemoteServiceFactory factory;
    private ConcurrentMap<String, RemoteService<T>> remoteServiceMap= new ConcurrentHashMap<String, RemoteService<T>>();
    
    public DiscoveriedDirectory(Container container,Class<T> serviceType,Dictionary<String, ?> properties,ConsumerInfo info)
    {
        this(container,null,serviceType,properties,info);
    }
    public DiscoveriedDirectory(Container container, List<Router> routers, Class<T> serviceType,Dictionary<String, ?> properties,ConsumerInfo info)
    {
        super(container, routers,properties,info);
        if(serviceType == null )
            throw new IllegalArgumentException("service type is null.");
        this.serviceType = serviceType;
    }

    @Override
    public Class<T> getServiceClass() {
        return serviceType;
    }
    
    public void setDiscovery(Discovery discovery){
        this.discovery= discovery;
    }

    @Override
    public boolean isAvailable() {
        if (isDestroyed()) {
            return false;
        }
        Map<String, RemoteService<T>> localServices = remoteServiceMap;
        if(localServices!=null&& localServices.size()>0){
            for(RemoteService<T> service:new ArrayList<RemoteService<T>>(localServices.values())){
                if(service.isAvailable()){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<RemoteService<T>> doList(RemoteRequest request) {
        if(routers!=null){
            
        }
        return new ArrayList<RemoteService<T>>(remoteServiceMap.values());
    }
    
    public void setRemoteServiceFactory(RemoteServiceFactory factory) {
       this.factory=factory;
    }
    
    public void addDiscoveryListener(){
        discovery.addTypeListener(getConsumerInfo().geServiceType(), this);
    }
    
    @Override
    public void handle(DiscoveryTypeEvent event) {
        int type  =event.getEventType();
        List<DiscoveryInfo> infos=  event.getDiscoveryInfoList();
        if(type ==DiscoveryTypeEvent.REGISTER){
            for(DiscoveryInfo info :infos){
                Dictionary<String, ?> prop=  info.getServiceProperties();
                RemoteReference<T> reference= factory.getReference(serviceType, prop);
                RemoteService<T> rs =  factory.getRemoteService(reference);
                remoteServiceMap.putIfAbsent(PropertiesUtils.toIndentityAddress(prop), rs);
            }
            
        }else if(type==DiscoveryTypeEvent.UNREGISTER){
            for(DiscoveryInfo info :infos){
                Dictionary<String, ?> prop=  info.getServiceProperties();
                remoteServiceMap.remove(PropertiesUtils.toIndentityAddress(prop));
            }
        }else if(type==DiscoveryTypeEvent.CHANGED){
            ConcurrentMap<String, RemoteService<T>> changed=  new ConcurrentHashMap<String, RemoteService<T>>();
            for(DiscoveryInfo info :infos){
                Dictionary<String, ?> prop=  info.getServiceProperties();
                RemoteReference<T> reference= factory.getReference(serviceType, prop);
                RemoteService<T> rs =  factory.getRemoteService(reference);
                changed.putIfAbsent(PropertiesUtils.toIndentityAddress(prop), rs);
            }
            synchronized (remoteServiceMap) {
                remoteServiceMap=changed;
            }
        }
       
    }
    @Override
    public ServiceProperties getServiceProperties() {
        return new ServiceProperties(directoryProperties);
    }

}
