package org.solmix.hola.cluster;

import org.solmix.hola.rs.RemoteService;
import org.solmix.runtime.Extension;

@Extension
public interface Cluster
{

    <T> RemoteService<T> join(Directory<T> directory);
}
