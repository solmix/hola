
package org.solmix.hola.discovery.zk;

import java.util.Dictionary;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.common.model.PropertiesBuilder;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.hola.discovery.model.ServiceType;
import org.solmix.hola.discovery.model.ServiceTypeImpl;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.extension.ExtensionLoader;

public class ZKDiscoveryTest extends Assert
{

    static Container container;

    @BeforeClass
    public static void setup() {
        container = ContainerFactory.getDefaultContainer(true);
    }

    @AfterClass
    public static void tearDown() {
        if (container != null) {
            container.close();
        }
    }
    @Test
    public void test(){
        
    }
    @Test
    public void testRegistor() throws InterruptedException {
        ExtensionLoader<DiscoveryProvider> loader = container.getExtensionLoader(DiscoveryProvider.class);
        DiscoveryProvider provider = loader.getExtension(ZookeeperProvider.NAME);
        assertNotNull(provider);
        PropertiesBuilder builder = PropertiesBuilder.newBuilder();
        Dictionary<String, ?> dic=builder.setProtocol(ZookeeperProvider.NAME).setHost("localhost").setPort(6379).build();
        Discovery discovery= provider.createDiscovery(dic);
        
        Dictionary<String, ?> service=builder.setProtocol("rpc")
            .setHost("localhost")
            .setPort(8081)
            .setPath("com.example.gateway")
            .setProperty("gateway.port", "3242").build();
        DiscoveryInfo info = new DiscoveryInfoImpl(service);
        discovery.register(info);
        discovery.unregister(info);
    }
    
    @Test
    public void testListener() throws InterruptedException {
        ExtensionLoader<DiscoveryProvider> loader = container.getExtensionLoader(DiscoveryProvider.class);
        DiscoveryProvider provider = loader.getExtension(ZookeeperProvider.NAME);
        assertNotNull(provider);
        PropertiesBuilder builder = PropertiesBuilder.newBuilder();
        Dictionary<String, ?> dic=builder.setProtocol("redis").setHost("localhost").setPort(6379).build();
        Discovery discovery= provider.createDiscovery(dic);
      
        ServiceType type = new ServiceTypeImpl("com.example.gateway");
        final CountDownLatch count= new CountDownLatch(1);
        discovery.addTypeListener(type, new ServiceTypeListener() {
            
            @Override
            public void handle(DiscoveryTypeEvent event) {
                System.out.println(event.getServiceType());
                count.countDown();
               
            }
        });
        count.await();
    }

}
