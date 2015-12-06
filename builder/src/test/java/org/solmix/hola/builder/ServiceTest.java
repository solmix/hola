package org.solmix.hola.builder;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.builder.service.HelloService;
import org.solmix.hola.builder.service.HelloServiceImpl;
import org.solmix.hola.builder.service.Person;
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
    public void testRegister2() throws InterruptedException{
        int port = NetUtils.getRandomPort();
        HelloService hs = new HelloServiceImpl();
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(hs);
        definition.setInterface(HelloService.class.getName());
        ProviderDefinition provider=new ProviderDefinition();
        provider.setPort(port);
        provider.setHeartbeat(10*1000);
        provider.setTimeout(5000);
        definition.setProvider(provider);
        
        ReferenceDefinition<HelloService> refer = new ReferenceDefinition<HelloService>();
        refer.setInterface(HelloService.class.getName());
        ConsumerDefinition consumer = new ConsumerDefinition();
        consumer.setHost("localhost");
        consumer.setPort(port);
//        consumer.setSerial("hola");
        consumer.setHeartbeat(3*1000);
        consumer.setTimeout(5000);
        refer.setConsumer(consumer);
        
        try {
            definition.register();
            HelloService hello  = refer.refer();
            assertNotNull(hello);
            String he = hello.echo("aaaaaaasss");
            assertEquals(he, "aaaaaaasss");
            List<Person> ps = hello.listPerson();
            assertTrue(ps!=null&&ps.size()==1);
//            while(true){
                try {
                    hello.echo("aaaaaaasss");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.currentThread().sleep(5000);
//            }2
        } finally{
            refer.destroy();
            definition.unregister();
        }
    }
  
}
