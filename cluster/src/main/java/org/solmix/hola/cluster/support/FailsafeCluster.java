package org.solmix.hola.cluster.support;

import org.solmix.hola.cluster.Cluster;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.rs.RemoteService;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;

/**
 * 失败安全策略.<br>
 *    A fail-safe or fail-secure device is one that, in the event of a specific type of failure, 
 * responds in a way that will cause no harm, or at least a minimum of harm, 
 * to other devices or to personnel.
 *    Since many types of failure are possible, it must be specified to what failure a component is fail safe. 
 *For example, a system may be fail-safe in the event of a power outage (electrical failure), 
 *but may not be fail safe in the event of mechanical failures.
 * 
 * @author solmix.f@gmail.com
 */
@Extension(name=FailsafeCluster.NAME)
public class FailsafeCluster implements Cluster,ContainerAware
{

    public static final String NAME = "failsafe";

    private Container container;

    @Override
    public <T> RemoteService<T> join(Directory<T> directory) {
        return new FailsafeRemoteService<T>(directory,container);
    }

    @Override
    public void setContainer(Container container) {
        this.container=container;
    }

}
