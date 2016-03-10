package org.solmix.hola.monitor;

import java.util.List;

import org.solmix.exchange.Node;


public interface Monitor extends Node,MonitorService
{

    
    void collect(MonitorState state);
    
    List<MonitorState> fetch(MonitorQuery query);
}
