package org.solmix.hola.discovery.redis;

import java.util.Dictionary;

import javax.annotation.Resource;

import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.DiscoveryProvider;
import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;

@Extension(name = RedisProvider.NAME)
public class RedisProvider implements DiscoveryProvider
{

    public static final String NAME = "redis";

    @Resource
    private Container container;

    @Override
    public Discovery createDiscovery(Dictionary<String, ?> info) throws DiscoveryException {
        return new RedisDiscovery(info, container);
    }

}
