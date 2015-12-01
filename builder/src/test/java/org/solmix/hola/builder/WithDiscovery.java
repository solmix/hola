
package org.solmix.hola.builder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.builder.service.HelloService;
import org.solmix.hola.builder.service.HelloServiceImpl;
import org.solmix.hola.builder.service.Person;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

public class WithDiscovery
{
private static final Logger LOG  = LoggerFactory.getLogger(WithDiscovery.class);
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
        
        
        
        ReferenceDefinition<HelloService> refer = new ReferenceDefinition<HelloService>();
        refer.setInterface(HelloService.class.getName());
        refer.setDiscovery(discovery);
        
        HelloService hello  =refer.refer();
        StringBuffer sb = new StringBuffer();
        
       /* for(int i=0;i<10;i++){
            sb.append("sdfefsdfs");
        }
        
        for(int i=0;i<10;i++){
            LOG.info(hello.echo(sb.toString()));
        }*/
        
        List<Person> ps=  hello.listPerson();
        definition.unregister();
    }

}
