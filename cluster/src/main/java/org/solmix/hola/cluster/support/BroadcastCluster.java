package org.solmix.hola.cluster.support;

import org.solmix.hola.cluster.Cluster;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.rs.RemoteService;


public class BroadcastCluster implements Cluster
{

    @Override
    public <T> RemoteService<T> join(Directory<T> directory) {
        // TODO Auto-generated method stub
        return null;
    }

}
