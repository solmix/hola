package org.solmix.hola.monitor.support;

import java.util.Dictionary;

import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterFactory;
import org.solmix.runtime.Extension;

@Extension( "monitor")
public class MonitorFilterFactory implements InvokeFilterFactory
{

    @Override
    public InvokeFilter create(Dictionary<String, ?> serviceProperties) {
        return new MonitorInvokeFilter(serviceProperties);
    }

}
