
package org.solmix.hola.monitor.support;

import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.monitor.Monitor;
import org.solmix.hola.monitor.MonitorFactory;
import org.solmix.hola.monitor.MonitorState;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterChain;
import org.solmix.runtime.Container;

public class MonitorInvokeFilter implements InvokeFilter
{

    private final ConcurrentMap<Method, AtomicInteger> concurrents = new ConcurrentHashMap<Method, AtomicInteger>();

    @Resource
    private Container container;

    @Resource
    private MonitorFactory monitorFactory;

    private String monitorAddress;

    private String serviceKey;

    private String application;

    private String group;

    private Monitor monitor;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MonitorInvokeFilter(Dictionary properties)
    {
        serviceKey = PropertiesUtils.toIndentityAddress(properties);
        this.application = PropertiesUtils.getString(properties, HOLA.APPLICATION_KEY);
        this.group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY);
        Dictionary monitor = (Dictionary) properties.get(HOLA.MONITOR_KEY);
        monitorAddress = PropertiesUtils.toIndentityAddress(monitor);
        if (monitor != null) {
            this.monitor = monitorFactory.getMonitor(monitorAddress, monitor);
        }
    }

    @Override
    public Object doFilter(RemoteRequest request, InvokeFilterChain chain) throws Throwable {
        if (monitor != null) {
            long start = System.currentTimeMillis();
            getConcurrent(request.getMethod()).incrementAndGet();
            try {
                Object result = chain.doFilter(request);
                collect(request, start, false);
                return result;
            } catch (RemoteException e) {
                collect(request, start, true);
                throw e;
            } finally {
                getConcurrent(request.getMethod()).incrementAndGet();
            }
        } else {
            return chain.doFilter(request);
        }

    }

    public AtomicInteger getConcurrent(Method method) {
        AtomicInteger concurrent = concurrents.get(method);
        if (concurrent == null) {
            concurrents.putIfAbsent(method, new AtomicInteger());
            concurrent = concurrents.get(method);
        }
        return concurrent;

    }

    protected void collect(RemoteRequest request, long start, boolean error) {
        long elapsed = System.currentTimeMillis() - start;
        AtomicInteger con = getConcurrent(request.getMethod());
        int concurrent = con.get();
        MonitorState state = new MonitorState();
        state.setElapsed(elapsed);
        state.setConcurrent(concurrent);
        state.setOperation(serviceKey + "/" + request.getMethodName());
        state.setApplication(application);
        state.setGroup(group);
        if (error) {
            state.setFailure(1);
        } else {
            state.setSuccess(1);
        }
        monitor.collect(state);
    }
}
