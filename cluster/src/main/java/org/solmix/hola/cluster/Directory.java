package org.solmix.hola.cluster;

import java.util.List;

import org.solmix.exchange.Node;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Extension;

@Extension
public interface Directory<T> extends Node
{
    List<RemoteService<T>> list(RemoteRequest request) throws ClusterException;

    ConsumerInfo getConsumerInfo();
    Class<T> getServiceClass();
    
    ServiceProperties getServiceProperties();

}
