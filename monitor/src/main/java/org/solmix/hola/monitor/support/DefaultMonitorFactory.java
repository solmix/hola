
package org.solmix.hola.monitor.support;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.monitor.Monitor;
import org.solmix.hola.monitor.MonitorFactory;
import org.solmix.hola.monitor.MonitorService;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.extension.ExtensionLoader;

public class DefaultMonitorFactory implements MonitorFactory
{

    private static final Map<String, Monitor> MONITORS = new ConcurrentHashMap<String, Monitor>();

    private static final ReentrantLock LOCK = new ReentrantLock();
 
    
    @Resource
    private Container container;
    
    @Override
    public Monitor getMonitor(String key,Dictionary<String, Object> properties) {
        properties.put(HOLA.INTERFACE_KEY, Monitor.class.getName());
        LOCK.lock();
        try {
            Monitor monitor = MONITORS.get(key);
            if (monitor != null) {
                return monitor;
            }
            monitor = createMonitor(properties);
            if (monitor == null) {
                throw new IllegalStateException("Can not create monitor " + PropertiesUtils.toAddress(properties));
            }
            MONITORS.put(key, monitor);
            return monitor;
        } finally {
            LOCK.unlock();
        }
    }

    private Monitor createMonitor(Dictionary<String, Object> properties) {
        ExtensionLoader<RemoteServiceFactory> loader= container.getExtensionLoader(RemoteServiceFactory.class);
        String protocol =PropertiesUtils.getString(properties, HOLA.PROTOCOL_KEY);
        String path =PropertiesUtils.getString(properties, HOLA.PATH_KEY);
        if(StringUtils.isEmpty(path)){
            properties.put(HOLA.PATH_KEY, MonitorService.class.getName());
        }
        properties.put(HOLA.CLUSTER_KEY, "failsafe");
        properties.put(HOLA.CHECK_KEY, false);
        RemoteServiceFactory factory = loader.getExtension(protocol);
        RemoteReference<MonitorService> ref=factory.getReference(MonitorService.class, properties);
        MonitorService monitor=factory.getService(ref);
        return new DefaultMonitor(ref,monitor,PropertiesUtils.toAddress(properties));
    }

}
