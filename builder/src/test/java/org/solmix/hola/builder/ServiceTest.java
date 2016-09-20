package org.solmix.hola.builder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
    static AtomicInteger succ=new  AtomicInteger();
    static AtomicInteger failed=new  AtomicInteger();
    static AtomicInteger send=new  AtomicInteger();
    @BeforeClass
    public static void setup(){
        container= ContainerFactory.newInstance().createContainer();
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
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(container,hs);
        definition.setInterface(HelloService.class.getName());
        definition.setPath("hello");
        ProviderDefinition provider=new ProviderDefinition();
        provider.setPort(port);
        definition.setProvider(provider);
        
        ReferenceDefinition<HelloService> refer = new ReferenceDefinition<HelloService>(container);
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
        ServiceDefinition<HelloService> definition = new ServiceDefinition<HelloService>(container,hs);
        definition.setInterface(HelloService.class.getName());
        ProviderDefinition provider=new ProviderDefinition();
        definition.setPort(port);
        definition.setAccepts(8);
        provider.setPort(port+1);
//        definition.setConnectTimeout(5000);
//        provider.setPalyload(5000);
//        provider.setContextpath("/hola_s");
//        provider.setProtocol("rmi");
//        provider.setTransporter("mina");
        definition.setHeartbeat(10*1000);
        definition.setHeartbeatTimeout(100*1000);
        definition.setFilter("test-filter");
//        provider.setTimeout(5000);
        definition.setProvider(provider);
        
        ReferenceDefinition<HelloService> refer = new ReferenceDefinition<HelloService>(container);
        refer.setInterface(HelloService.class.getName());
        ConsumerDefinition consumer = new ConsumerDefinition();
        consumer.setHost("localhost");
        consumer.setFilter("test-filter");
        consumer.setPort(port);
//        consumer.setProtocol("rmi");
//        consumer.setPalyload(500);
        consumer.setSerial("hola");
//        consumer.setHeartbeat(3*1000);
        consumer.setPipelines(8);
//        consumer.setTransporter("mina");
        consumer.setTimeout(50000);
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
           
            int total=20;
            ThreadPool pool = new DefaultThreadPool(total,"TEST");
           final CountDownLatch la= new CountDownLatch(total);
          final String ECHO = getString();
          long _$=System.currentTimeMillis();
            while(i<total){
                i++;
                
                pool.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        send.incrementAndGet();
                        try {
                            String str = hello.echo(ECHO);
//                            System.out.println(System.currentTimeMillis());
                            succ.incrementAndGet();
                        }catch(Throwable e){
                            e.printStackTrace();
                            failed.incrementAndGet();
                        } finally {
                            
                            la.countDown();
                        }
                        
                    }
                });
                
            }
            la.await();
           
            System.out.println(succ+"----"+failed+"----"+send+"==========="+(System.currentTimeMillis()-_$));
        } finally{
            refer.destroy();
            definition.unregister();
        }
    }
    private String getString() {
        StringBuffer sb = new StringBuffer();
        while(sb.length()<1024){
            sb.append("abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*(");
        }
        return sb.toString();
    }
}
