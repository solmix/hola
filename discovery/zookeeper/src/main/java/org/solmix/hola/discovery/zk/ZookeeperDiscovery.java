
package org.solmix.hola.discovery.zk;

import java.io.IOException;
import java.util.Dictionary;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.ServiceID;
import org.solmix.hola.discovery.model.ServiceType;
import org.solmix.hola.discovery.support.FailbackDiscovery;
import org.solmix.runtime.Container;

public class ZookeeperDiscovery extends FailbackDiscovery
{

    private final CuratorFramework client;
    private final String root;
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperDiscovery.class);
    public ZookeeperDiscovery(Dictionary<String, ?> properties, Container container)
    {
        super(properties, container);
        if(PropertiesUtils.getString(properties, HOLA.HOST_KEY)==null){
            throw new IllegalArgumentException("redis registor address is null");
        }
        
        String group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY,HOLA.DEFAULT_ROOT);
        if (! group.startsWith(HOLA.PATH_SEPARATOR)) {
            group = HOLA.PATH_SEPARATOR + group;
        }
        if (! group.endsWith(HOLA.PATH_SEPARATOR)) {
            group = group + HOLA.PATH_SEPARATOR;
        }
        this.root=group;
        
        
        String backs = PropertiesUtils.getString(properties, HOLA.BACKUP_KEY);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
            .connectString(backs)
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
    public void destroy() throws IOException {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DiscoveryInfo[] getServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DiscoveryInfo[] getServices(ServiceType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceType[] getServiceTypes() {
        // TODO Auto-generated method stub
        return null;
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
    
    public String toServicePath(DiscoveryInfo meta){
        ServiceType type = meta.getServiceID().getServiceType();
        String strType = type.getIdentityName();
        String path = root+strType + HOLA.PATH_SEPARATOR + meta.getServiceID().getName();
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
    protected void doSubscribe(ServiceType type, ServiceTypeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doUnsubscribe(ServiceType type, ServiceTypeListener listener) {
        // TODO Auto-generated method stub

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
