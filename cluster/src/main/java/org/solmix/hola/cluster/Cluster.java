package org.solmix.hola.cluster;

import org.solmix.hola.rs.RemoteService;

public interface Cluster
{

    <T> RemoteService<T> join(Directory<T> directory);
}
