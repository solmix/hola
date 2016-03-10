package com.foo.simple.monitor;

import java.util.List;

import org.solmix.hola.monitor.MonitorQuery;
import org.solmix.hola.monitor.MonitorService;
import org.solmix.hola.monitor.MonitorState;


public class FooMonitor implements MonitorService
{

    @Override
    public void collect(MonitorState state) {
        System.out.println(state.toString());

    }

    @Override
    public List<MonitorState> fetch(MonitorQuery query) {
        // TODO Auto-generated method stub
        return null;
    }

}
