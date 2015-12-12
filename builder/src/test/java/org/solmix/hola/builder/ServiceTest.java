package org.solmix.hola.builder;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.builder.service.HelloException;
import org.solmix.hola.builder.service.HelloService;
import org.solmix.hola.builder.service.HelloServiceImpl;
import org.solmix.hola.builder.service.Person;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.threadpool.DefaultThreadPool;
import org.solmix.runtime.threadpool.ThreadPool;


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
    int succ=0,failed=0;
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
        consumer.setSerial("hola");
        consumer.setHeartbeat(3*1000);
        consumer.setTimeout(5000);
        refer.setConsumer(consumer);
        
        try {
            definition.register();
           final HelloService hello  = refer.refer();
            assertNotNull(hello);
            String he = hello.echo("aaaaaaasss");
            assertEquals(he, "aaaaaaasss");
            List<Person> ps = hello.listPerson();
            assertTrue(ps!=null&&ps.size()==1);
            try {
            hello.test();
            } catch (HelloException e) {
                e.printStackTrace();
            }
            int i=0;
            long _$=System.currentTimeMillis();
            ThreadPool pool = new DefaultThreadPool(10000);
           final CountDownLatch la= new CountDownLatch(60);
          
            while(i<60){
                i++;
                pool.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            String str = hello.echo("aaaaaaasss");
//                            System.out.println(System.currentTimeMillis());
                            succ++;
                        }catch(Throwable e){
                            failed++;
                        } finally {
                            la.countDown();
                        }
                        
                    }
                });
                
//                  Thread.currentThread().sleep(5000);
            }
            la.await();
            System.out.println(succ+"----"+failed+"==========="+(System.currentTimeMillis()-_$));
        } finally{
            refer.destroy();
            definition.unregister();
        }
    }
  
}
