package org.solmix.hola.monitor;

import java.util.Dictionary;

public interface MonitorFactory
{

    Monitor getMonitor(String id,Dictionary<String, Object> properties);
}
