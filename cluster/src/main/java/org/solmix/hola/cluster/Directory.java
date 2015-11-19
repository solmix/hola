package org.solmix.hola.cluster;

import java.util.List;

import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;

public interface Directory<T>
{
    List<RemoteService<T>> list(RemoteRequest request) throws ClusterException;

}
