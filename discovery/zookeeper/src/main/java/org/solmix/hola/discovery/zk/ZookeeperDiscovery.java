
package org.solmix.hola.discovery.zk;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.DefaultServiceType;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.common.model.ServiceType;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.hola.discovery.support.FailbackDiscovery;
import org.solmix.runtime.Container;

public class ZookeeperDiscovery extends FailbackDiscovery
{

    private final CuratorFramework client;
    private final String root;
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperDiscovery.class);
    
    private final ConcurrentMap<ServiceType, ConcurrentMap<ServiceTypeListener, CuratorWatcher>> typeListeners = new ConcurrentHashMap<ServiceType, ConcurrentMap<ServiceTypeListener, CuratorWatcher>>();

    
    public ZookeeperDiscovery(Dictionary<String, ?> properties, Container container)
    {
        super(properties, container);
        if(PropertiesUtils.getString(properties, HOLA.HOST_KEY)==null){
            throw new IllegalArgumentException("zookeeper discovery address is null");
        }
        
        String group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY,HOLA.DEFAULT_ROOT);
        if (! group.startsWith(HOLA.PATH_SEPARATOR)) {
            group = HOLA.PATH_SEPARATOR + group;
        }
        if (group.endsWith(HOLA.PATH_SEPARATOR)) {
            group = group .substring(0, group.length()-HOLA.PATH_SEPARATOR.length());
        }
        this.root=group;
        
        
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
            .connectString(PropertiesUtils.getConnectString(properties))
            .retryPolicy( new RetryNTimes(Integer.MAX_VALUE, 1000))
            .connectionTimeoutMs(5000);
        client = builder.build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
               if(newState ==ConnectionState.LOST){
                   
               }else if(newState ==ConnectionState.CONNECTED){
                   
               }else if(newState ==ConnectionState.RECONNECTED){
                   try {
                       recover();
                 } catch (Exception e) {
                       LOG.error(e.getMessage(), e);
                 }
               }

            }
        });
        client.start();
    }
    
    @Override
    public void destroy() {
        super.destroy();
        try {
            client.close();
        } catch (Exception e) {
            LOG.warn("Failed to close zookeeper client " + getDiscoveryAddress() + ", cause: " + e.getMessage(), e);
        }
    }
    
    public boolean isAvailable() {
        return client.getZookeeperClient().isConnected();
    }
    
    
    @Override
    public DiscoveryInfo getService(ServiceID aServiceID) {
        if(aServiceID==null){
            return null;
        }
        DiscoveryInfo[] infos  = getServices(aServiceID.getServiceType());
        if(infos==null||infos.length==0){
            return null;
        }
        for(DiscoveryInfo info:infos){
            if(aServiceID.equals(info.getServiceID())){
                return info;
            }
        }
        return null;
    }

    @Override
    public DiscoveryInfo[] getServices() {
        String root =this.root;
        List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
        
        getDiscoveryInfo(infos,root);
        return infos.toArray(new DiscoveryInfo[]{});
    }
    
    protected void getDiscoveryInfo(List<DiscoveryInfo> infos,String path){
        try {
            List<String> children= client.getChildren().forPath(path);
            boolean shouldAdd =path.endsWith("/"+HOLA.PROVIDER_CATEGORY)
                ||path.endsWith("/"+HOLA.CONSUMER_CATEGORY)
                ||path.endsWith("/"+HOLA.ROUTER_CATEGORY)
                ||path.endsWith("/"+HOLA.CONFIGURATOR_CATEGORY);
            if(children!=null&&children.size()>0){
                for(String child:children){
                   if(shouldAdd){
                               String decoded = PropertiesUtils.decode(child);
                               Dictionary<String, ?> properties = PropertiesUtils.toProperties(decoded);
                               infos.add(new DiscoveryInfoImpl(properties));
                   }else{
                       getDiscoveryInfo(infos,path+"/"+child);
                   }
                }
            }
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
    }
 

    @Override
    public DiscoveryInfo[] getServices(ServiceType type) {
        String path = toServiceTypePath(type);
        List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
        getDiscoveryInfo(infos, path);
        return infos.toArray(new DiscoveryInfo[] {});
    }

    @Override
    public ServiceType[] getServiceTypes() {
        Set<ServiceType> typeSet= new HashSet<ServiceType>();
        for (DiscoveryInfo info:getServices()) {
            if(info.getServiceID()==null){
                continue;
            }
            if(!typeSet.contains(info.getServiceID().getServiceType())){
                typeSet.add(info.getServiceID().getServiceType());
            }
        }
        return typeSet.toArray(new ServiceType[] {});
    }

    @Override
    protected void doRegister(DiscoveryInfo meta) {
        String path = toServicePath(meta);
        boolean dynamic = PropertiesUtils.getBoolean(meta.getServiceProperties(), HOLA.DYNAMIC_KEY, true);
        try {
            create(path,dynamic);
        } catch (Exception e) {
            throw new DiscoveryException("Failed to register "+meta +" to zookeeper ,cause:", e);
        }

    }
    public String toServiceTypePath(ServiceType type){
        String strType = type.getIdentityName();
        return root+HOLA.PATH_SEPARATOR+strType;
    }
    public String toServicePath(DiscoveryInfo meta){
        ServiceType type = meta.getServiceID().getServiceType();
       
        String path = toServiceTypePath(type)+ HOLA.PATH_SEPARATOR + PropertiesUtils.encode(meta.getServiceID().getName());
        return path;
    }
    
    @Override
    protected void doUnregister(DiscoveryInfo meta) {
        String path = toServicePath(meta);
        try {
            delete(path);
        } catch (Exception e) {
            throw new DiscoveryException("Failed to unregister "+meta +" to zookeeper ,cause:", e);
        }
    }

    @Override
    protected void doSubscribe(final ServiceType type, final ServiceTypeListener listener) {
        try {
            if (HOLA.ANY_VALUE.equals(type.getServiceInterface())) {
                create(root, false);
                ConcurrentMap<ServiceTypeListener, CuratorWatcher> listeners = typeListeners.get(type);
                if (listeners == null) {
                    typeListeners.putIfAbsent(type, new ConcurrentHashMap<ServiceTypeListener, CuratorWatcher>());
                    listeners = typeListeners.get(type);
                }
                CuratorWatcher watcher = listeners.get(listener);
                if(watcher==null){
                    listeners.putIfAbsent(listener, new CuratorWatcher(){

                        @Override
                        public void process(WatchedEvent event) throws Exception {
                           String path = event.getPath();
                           List<String> children=client.getChildren().usingWatcher(this).forPath(path);
                           for(String child:children){
                               ServiceType childType = DefaultServiceType.fromAddress(child);
                               addTypeListener(childType, listener);
                           }
                            
                        }
                        
                    });
                    watcher=listeners.get(listener);
                }
                create(root, false);
                
                List<String> services =addCuratorWatcher(root, watcher);
                if(services!=null && services.size()>0){
                    for(String service:services){
                     ServiceType childType = DefaultServiceType.fromAddress(service);
                     addTypeListener(childType, listener);
                    }
                }
            }else{
                List<DiscoveryInfo> infos  = new ArrayList<DiscoveryInfo>();
                for(String path:toCategoryPath(type)){
                    ConcurrentMap<ServiceTypeListener, CuratorWatcher> listeners = typeListeners.get(type);
                    if (listeners == null) {
                        typeListeners.putIfAbsent(type, new ConcurrentHashMap<ServiceTypeListener, CuratorWatcher>());
                        listeners = typeListeners.get(type);
                    }
                    CuratorWatcher watcher = listeners.get(listener);
                    if(watcher==null){
                        listeners.putIfAbsent(listener, new CuratorWatcher(){

                            @Override
                            public void process(WatchedEvent event) throws Exception {
                               String path = event.getPath();
                               List<String> children=client.getChildren().usingWatcher(this).forPath(path);
                               ZookeeperDiscovery.this.notify(type, listener, matchedChildren(type,path,children), DiscoveryTypeEvent.CHANGED);
                            }
                            
                        });
                        watcher=listeners.get(listener);
                    }
                    create(path, false);
                    
                    List<String> services =addCuratorWatcher(path, watcher);
                    if(services!=null && services.size()>0){
                        infos.addAll(matchedChildren(type, path, services));
                    }
                    notify(type, listener, infos, DiscoveryTypeEvent.CHANGED);
                }
            }
        } catch (Exception e) {
            throw new DiscoveryException("Failed to subscribe " + type + " from zookeeper ,cause:", e);
        }
    }

    protected List<DiscoveryInfo> matchedChildren(ServiceType type, String path, List<String> children) {
        List<DiscoveryInfo> infos  = new ArrayList<DiscoveryInfo>();
        if(children!=null && children.size()>0){
            for(String child:children){
                child=PropertiesUtils.decode(child);
                if(child.contains("://")){
                    Dictionary<String, ?> properteis  = PropertiesUtils.toProperties(child);
                    DiscoveryInfo info = new DiscoveryInfoImpl(properteis);
                    if(isMatch(type, info)){
                        infos.add(info);
                    }
                }
            }
        }
        /*if(infos.size()==0){
            int i = path.lastIndexOf('/');
            String category = i < 0 ? path : path.substring(i + 1);
        }*/
        return infos;
    }

    private String[] toCategoryPath(ServiceType type) {
        String[] categroies;
        if (HOLA.ANY_VALUE.equals(type.getCategory())) {
            categroies = new String[] {HOLA.PROVIDER_CATEGORY, HOLA.CONSUMER_CATEGORY, 
                HOLA.ROUTER_CATEGORY, HOLA.CONFIGURATOR_CATEGORY};
        } else {
            categroies = new String[]{type.getCategory()};
        }
        String[] paths = new String[categroies.length];
        for (int i = 0; i < categroies.length; i ++) {
            paths[i] = root+ HOLA.PATH_SEPARATOR+type.getServiceName()+ HOLA.PATH_SEPARATOR + categroies[i];
        }
        return paths;
    }

    protected List<String> addCuratorWatcher(String path, CuratorWatcher watcher) {
        try {
            return client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    @Override
    protected void doUnsubscribe(ServiceType type, ServiceTypeListener listener) {
        ConcurrentMap<ServiceTypeListener, CuratorWatcher> listeners = typeListeners.get(type);
        if(listeners!=null){
            CuratorWatcher watcher = listeners.get(listener);
            if(watcher!=null){
                listeners.remove(listener);
            }
            typeListeners.remove(type);
        }
    }
    public void delete(String path) {
        try {
              client.delete().forPath(path);
        } catch (NoNodeException e) {
        } catch (Exception e) {
              throw new IllegalStateException(e.getMessage(), e);
        }
  }
    
    public void create(String path, boolean ephemeral) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path);
        } else {
            createPersistent(path);
        }
    }

    public void createPersistent(String path) {
        try {
            client.create().forPath(path);
        } catch (NodeExistsException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createEphemeral(String path) {
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (NodeExistsException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
