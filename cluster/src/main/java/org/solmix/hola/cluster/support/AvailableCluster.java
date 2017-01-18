
package org.solmix.hola.cluster.support;

import org.solmix.hola.cluster.Cluster;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.rs.RemoteService;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;

@Extension( AvailableCluster.NAME)
public class AvailableCluster implements Cluster,ContainerAware
{

    public static final String NAME = "available";

    private Container container;

    @Override
    public <T> RemoteService<T> join(Directory<T> directory) {
        return new AvailableRemoteService<T>(directory,container);
    }

    @Override
    public void setContainer(Container container) {
        this.container=container;
    }

}
