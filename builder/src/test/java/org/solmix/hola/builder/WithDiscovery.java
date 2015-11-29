
package org.solmix.hola.builder;

import org.solmix.commons.util.NetUtils;
import org.solmix.hola.builder.service.HelloService;
import org.solmix.hola.builder.service.HelloServiceImpl;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

public class WithDiscovery
{

    public static void main(String[] args) {
        Container container = ContainerFactory.getDefaultContainer(true);
        int port = NetUtils.getRandomPort();
        HelloService hs = new HelloServiceImpl();
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(container,hs);
        definition.setInterface(HelloService.class.getName());
        definition.setPath("hello");
        ProviderDefinition provider=new ProviderDefinition();
        provider.setPort(port);
        definition.setProvider(provider);
        
        DiscoveryDefinition discovery = new DiscoveryDefinition();
        discovery.setAddress("localhost:6379");
        discovery.setProtocol("redis");
        definition.setDiscovery(discovery);
        
        definition.register();
        definition.unregister();
    }

}
