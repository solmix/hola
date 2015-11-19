package org.solmix.hola.cluster.support;

import org.solmix.hola.cluster.Cluster;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.rs.RemoteService;


public class AvailableCluster implements Cluster
{

    public static final String NAME = "available";
    @Override
    public <T> RemoteService<T> join(Directory<T> directory) {
        // TODO Auto-generated method stub
        return null;
    }

}
