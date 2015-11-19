package org.solmix.hola.cluster;

import java.util.List;

import org.solmix.hola.discovery.model.ServiceID;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;

public interface Router extends Comparable<Router>
{
    <T> List<RemoteService<T>> route(List<RemoteService<T>> routes,ServiceID consumer,RemoteRequest request);

}
