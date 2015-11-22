
package org.solmix.hola.cluster.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.cluster.ClusterException;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.Router;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;

public abstract class AbstractDirectory<T> extends Object implements Directory<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDirectory.class);

    private volatile boolean destroyed = false;

    private volatile List<Router> routers;

    private ServiceID consumerId;
    public AbstractDirectory(ServiceID consumerID){
        this(null,consumerID);
    }

    public AbstractDirectory(List<Router> routers, ServiceID consumerID)
    {
        setRouters(routers);
    }

    @Override
    public List<RemoteService<T>> list(RemoteRequest request) throws ClusterException {
        if (destroyed) {
            throw new ClusterException("Directory already destroyed");
        }
        List<RemoteService<T>> remoteServices = doList(request);
        if (routers != null && routers.size() > 0) {
            for (Router router : routers) {
                try {
                    remoteServices = router.route(remoteServices, getConsumerID(), request);
                } catch (Exception e) {
                    LOG.error("Failed to excute router :" + consumerId.getName(), e);
                }
            }
        }
        return remoteServices;
    }

    public abstract List<RemoteService<T>> doList(RemoteRequest request);

    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    public ServiceID getConsumerID() {
        return consumerId;
    }

    public void setRouters(List<Router> routers) {
        routers = routers == null ? new ArrayList<Router>() : new ArrayList<Router>(routers);
        Collections.sort(routers);
        this.routers = routers;
    }
    
    @Override
    public ServiceID getConsumerServiceID(){
        return consumerId;
    }

    @Override
    public String getAddress() {
        return consumerId.getName();
    }

}
