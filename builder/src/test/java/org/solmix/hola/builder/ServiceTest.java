package org.solmix.hola.builder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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
        HelloService hs = new HelloServiceImpl();
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(hs);
        definition.setInterface(HelloService.class.getName());
        definition.setPath("hello");
        ProviderDefinition provider=new ProviderDefinition();
        provider.setPort(8180);
        definition.setProvider(provider);
        try {
            definition.register();
        } finally{
            definition.unregister();
        }
    }
}
