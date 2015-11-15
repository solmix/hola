
package org.solmix.hola.discovery.redis;

import java.util.Dictionary;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.common.model.PropertiesBuilder;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.extension.ExtensionLoader;

public class RedisDiscoveryTest extends Assert
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
    public void test() {
        ExtensionLoader<DiscoveryProvider> loader = container.getExtensionLoader(DiscoveryProvider.class);
        DiscoveryProvider provider = loader.getExtension(RedisProvider.NAME);
        assertNotNull(provider);
        PropertiesBuilder builder = PropertiesBuilder.newBuilder();
        Dictionary<String, ?> dic=builder.setProtocol("redis").setHost("localhost").setPort(6379).build();
        
    }

}
