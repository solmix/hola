package org.solmix.hola.discovery.jmdns;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


public class JmDNSDiscoveryTest extends Assert
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
}
