
package org.solmix.hola.cluster.directory;

import java.util.List;

import org.solmix.hola.cluster.Router;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;

public class PreparedDirectory<T> extends AbstractDirectory<T>
{

    private final List<RemoteService<T>> remoteServices;

    public PreparedDirectory(List<Router> reouters, ServiceID id)
    {
        this(null, reouters, id);
    }

    public PreparedDirectory(List<RemoteService<T>> remoteServices, List<Router> reouters, ServiceID id)
    {
        super(reouters, id);
        if (remoteServices == null || remoteServices.size() < 0) {
            throw new IllegalArgumentException("PreparedDirectory remoteservice is empty");
        }
        this.remoteServices = remoteServices;
    }

    @Override
    public List<RemoteService<T>> doList(RemoteRequest request) {
        return remoteServices;
    }

    @Override
    public void destroy() {
        if (isDestroyed()) {
            return;
        }
        super.destroy();
        if (remoteServices != null) {
            for (RemoteService<T> rs : remoteServices) {
                rs.destroy();
            }
            remoteServices.clear();
        }
    }

    @Override
    public boolean isAvailable() {
        if (isDestroyed()) {
            return false;
        }
        for (RemoteService<T> rs : remoteServices) {
            if (rs.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<T> getServiceClass() {
        return null;
    }
}
