package org.solmix.hola.discovery.zk;

import java.util.Dictionary;

import javax.annotation.Resource;

import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;

@Extension(name = ZookeeperProvider.NAME)
public class ZookeeperProvider implements DiscoveryProvider
{
    public static final String NAME = "zk";

    @Resource
    private Container container;

    @Override
    public Discovery createDiscovery(Dictionary<String, ?> info) throws DiscoveryException {
        return new ZookeeperDiscovery(info, container);
    }

}
