package org.solmix.hola.builder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.builder.service.HelloService;
import org.solmix.hola.builder.service.HelloServiceImpl;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;


public class ServiceTest extends Assert
{
    static Container container;
    
    @BeforeClass
    public static void setup(){
        container= ContainerFactory.getDefaultContainer(true);
    }

    @AfterClass
    public static void tearDown(){
        if(container!=null){
            container.close();
        }
    }
    
    @Test
    public void testRegister(){
        int port = NetUtils.getRandomPort();
        HelloService hs = new HelloServiceImpl();
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(hs);
        definition.setInterface(HelloService.class.getName());
        definition.setPath("hello");
        ProviderDefinition provider=new ProviderDefinition();
        provider.setPort(port);
        definition.setProvider(provider);
        
        ReferenceDefinition<HelloService> refer = new ReferenceDefinition<HelloService>();
        refer.setInterface(HelloService.class.getName());
        refer.setUrl("hola://localhost:"+port+"/hello");
        
        try {
            definition.register();
            HelloService hello  = refer.refer();
            assertNotNull(hello);
            String he = hello.echo("aaaaaaasss");
            assertEquals(he, "aaaaaaasss");
        } finally{
            refer.destroy();
            definition.unregister();
        }
    }
    @Test
    public void testRegister2(){
        int port = NetUtils.getRandomPort();
        HelloService hs = new HelloServiceImpl();
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(hs);
        definition.setInterface(HelloService.class.getName());
        ProviderDefinition provider=new ProviderDefinition();
        provider.setPort(port);
        definition.setProvider(provider);
        
        ReferenceDefinition<HelloService> refer = new ReferenceDefinition<HelloService>();
        refer.setInterface(HelloService.class.getName());
        ConsumerDefinition consumer = new ConsumerDefinition();
        consumer.setHost("localhost");
        consumer.setPort(port);
        refer.setConsumer(consumer);
        
        try {
            definition.register();
            HelloService hello  = refer.refer();
            assertNotNull(hello);
            String he = hello.echo("aaaaaaasss");
            assertEquals(he, "aaaaaaasss");
        } finally{
            refer.destroy();
            definition.unregister();
        }
    }
    
    @Test
    public void testRemoteWithDiscovery(){
        int port = NetUtils.getRandomPort();
    }
}
