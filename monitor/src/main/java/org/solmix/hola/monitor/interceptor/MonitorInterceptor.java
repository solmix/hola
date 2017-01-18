package org.solmix.hola.monitor.interceptor;

import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.solmix.exchange.Endpoint;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhaseInterceptorSupport;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.model.NamedID;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.monitor.Monitor;
import org.solmix.hola.monitor.MonitorFactory;
import org.solmix.hola.monitor.MonitorState;
import org.solmix.runtime.Extension;

@Extension("monitor")
public class MonitorInterceptor extends PhaseInterceptorSupport<Message>
{

    private final ConcurrentMap<NamedID, AtomicInteger> concurrents = new ConcurrentHashMap<NamedID, AtomicInteger>();

    private static final String MONITOR_START_TIME="MONITOR_START_TIME";
    public MonitorInterceptor()
    {
        super(Phase.USER_LOGICAL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Exchange ex = message.getExchange();
       
        
        if((message.isRequest()&&!message.isInbound())//client 请求输出记录时间
            ||(message.isRequest()&&message.isInbound())){//client 接收请求输入
            ex.put(MONITOR_START_TIME, System.currentTimeMillis());
            Endpoint edp=ex.getEndpoint();
            Object monitor= edp.getEndpointInfo().getProperty(HOLA.MONITOR_KEY);
            if(monitor!=null){
                String id=(String)edp.getEndpointInfo().getProperty("MONITOR_IDENTIRY");
                MonitorFactory factory=  ex.getContainer().getExtension(MonitorFactory.class);
                Monitor mon=factory.getMonitor(id,(Dictionary)monitor);
                ex.put(Monitor.class, mon);
                AtomicInteger ai =getConcurrent(message);
                ai.incrementAndGet();
                ex.put(AtomicInteger.class, ai);
            }
        }else{
            collect(message.getExchange(),getOperation(message),false);
        }
    }
    
    @Override
    public void handleFault(Message message) {
        collect(message.getExchange(),getOperation(message),true);
    }
    
    private NamedID getOperation(Message message){
        NamedID id =(NamedID)message.get(Message.OPERATION);
        Exchange ex=message.getExchange();
        if(id==null){
            if(message.isInbound()){
                id =(NamedID)ex.getOut().get(Message.OPERATION);
            }else{
                id =(NamedID)ex.getIn().get(Message.OPERATION);
            }
        }
        return id;
    }
    
    
    public AtomicInteger getConcurrent(Message message) {
        NamedID id =(NamedID)message.get(Message.OPERATION);
        if(id!=null){
            AtomicInteger concurrent = concurrents.get(id);
            if (concurrent == null) {
                concurrents.putIfAbsent(id, new AtomicInteger());
                concurrent = concurrents.get(id);
            }
            return concurrent;
        }
        return new AtomicInteger();
        
    }
    protected static void collect(Exchange ex, NamedID id,boolean error){
        Monitor monitor = ex.get(Monitor.class);
        if(monitor!=null){
           
            long start = (Long)ex.get(MONITOR_START_TIME);
            AtomicInteger ai =ex.get(AtomicInteger.class);
            if(ai!=null){
                ai.decrementAndGet();
                ex.remove(AtomicInteger.class);
            }
            collect(monitor,ex.getEndpoint().getEndpointInfo(),id,ai, start, error);
        }
    }
    protected static void collect(Monitor monitor,EndpointInfo info,NamedID operation,AtomicInteger con,long start,boolean error){
        long elapsed = System.currentTimeMillis() - start; 
        int concurrent = con.get();
        MonitorState state = new MonitorState();
        state.setElapsed(elapsed);
        state.setConcurrent(concurrent);
        state.setOperation(operation.toIdentityString());
        state.setApplication((String)info.getProperty(HOLA.APPLICATION_KEY));
        state.setGroup((String)info.getProperty(HOLA.GROUP_KEY));
        if(error){
            state.setFailure(1);
        }else{
            state.setSuccess(1);
        }
        monitor.collect(state);
    }

}
