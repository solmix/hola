package org.solmix.hola.monitor.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.monitor.Monitor;
import org.solmix.hola.monitor.MonitorQuery;
import org.solmix.hola.monitor.MonitorService;
import org.solmix.hola.monitor.MonitorState;
import org.solmix.hola.monitor.StateUnit;
import org.solmix.hola.rs.RemoteReference;


public class DefaultMonitor implements Monitor
{
    private static final Logger LOG  = LoggerFactory.getLogger(DefaultMonitor.class);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3, new NamedThreadFactory("holaMonitorSendTimer", true));

    private final ScheduledFuture<?> sendFuture;
    private final long monitorInterval;
    private final ConcurrentMap<StateUnit, AtomicReference<long[]>> statesMap = new ConcurrentHashMap<StateUnit, AtomicReference<long[]>>();

   private RemoteReference<MonitorService> reference;
   private MonitorService monitor;
   private final String address;
    public DefaultMonitor(RemoteReference<MonitorService> ref, MonitorService monitor,String address){
        this.reference=ref;
        this.monitor=monitor;
        this.address=address;
        
        Object intrenal =reference.getProperty("internal");
        if(intrenal==null){
            this.monitorInterval=60000;
        }else{
           int ii= Integer.valueOf(intrenal.toString());
           this.monitorInterval=ii>0?ii:60000;
        }
        sendFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    send();
                } catch (Throwable t) { 
                    LOG.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
                }
            }
        }, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
    }
    
    protected void send() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Send statistics to monitor " + getAddress());
        }
        long timestamp = System.currentTimeMillis();
        for (Map.Entry<StateUnit, AtomicReference<long[]>> entry : statesMap.entrySet()) {
            StateUnit unit = entry.getKey();
            AtomicReference<long[]> reference = entry.getValue();
            long[] numbers = reference.get();
            long success = numbers[0];
            long failure = numbers[1];
            long elapsed = numbers[2];
            long concurrent = numbers[3];
            long maxElapsed = numbers[4];
            long maxConcurrent = numbers[5];
            MonitorState state = new MonitorState();
            state.setApplication(unit.getApplication());
            state.setGroup(unit.getGroup());
            state.setOperation(unit.getOperation());
            state.setVersion(unit.getVersion());
            state.setSuccess(success);
            state.setFailure(failure);
            state.setElapsed(elapsed);
            state.setConcurrent(concurrent);
            state.setMaxConcurrent(maxConcurrent);
            state.setMaxElapsed(maxElapsed);
            state.setTimestamp(timestamp);
            monitor.collect(state);
            long[] current;
            long[] update = new long[6];
            do {
                current = reference.get();
                if (current == null) {
                    update[0] = 0;
                    update[1] = 0;
                    update[2] = 0;
                    update[3] = 0;
                } else {
                    update[0] = current[0] - success;
                    update[1] = current[1] - failure;
                    update[2] = current[2] - elapsed;
                    update[3] = current[3] - concurrent;
                }
            } while (! reference.compareAndSet(current, update));
        }
        
    }
    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean isAvailable() {
        return reference.isAvailable();
    }

    @Override
    public void destroy() {
        try {
            sendFuture.cancel(true);
        } catch (Throwable t) {
            LOG.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
        }
        reference.destroy();
    }

    @Override
    public void collect(MonitorState state) {
        StateUnit unit  = new StateUnit(state);
        AtomicReference<long[]> reference = statesMap.get(unit);
        if (reference == null) {
            statesMap.putIfAbsent(unit, new AtomicReference<long[]>());
            reference = statesMap.get(unit);
        }
        long[] current;
        long[] update = new long[6];
        do{
            current = reference.get();
            if(current==null){
                update[0] = state.getSuccess();
                update[1] = state.getFailure();
                update[2] = state.getElapsed();
                update[3] = state.getConcurrent();
                update[4] = state.getElapsed();
                update[5] = state.getConcurrent();
            }else{
                update[0] = current[0] +  state.getSuccess();;
                update[1] = current[1] +  state.getFailure();
                update[2] = current[2] + state.getElapsed();
                update[3] = (current[3] +  state.getConcurrent()) / 2;
                update[4] = current[4] > current[2] ? current[4] : current[2];
                update[5] = current[5] > current[3] ? current[5] : current[3] ;
            }
        }while (! reference.compareAndSet(current, update));
    }

    @Override
    public List<MonitorState> fetch(MonitorQuery query) {
        return monitor.fetch(query);
    }
    
 

}
