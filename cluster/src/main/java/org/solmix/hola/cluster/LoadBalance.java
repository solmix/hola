package org.solmix.hola.cluster;

import java.util.List;

import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Extension;

@Extension
public interface LoadBalance
{
    <T> RemoteService<T> select(List<RemoteService<T>> services,RemoteRequest request)throws ClusterException;

}
