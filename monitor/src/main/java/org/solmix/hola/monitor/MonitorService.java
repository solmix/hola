package org.solmix.hola.monitor;

import java.util.List;

public interface MonitorService
{
 void collect(MonitorState state);
    
    List<MonitorState> fetch(MonitorQuery query);
}
