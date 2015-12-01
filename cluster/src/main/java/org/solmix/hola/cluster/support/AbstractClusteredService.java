
package org.solmix.hola.cluster.support;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DataUtils;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.ClusterException;
import org.solmix.hola.cluster.ConsumerInfo;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.cluster.directory.AbstractDirectory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.support.AbstractRemoteService;
import org.solmix.runtime.Container;

public abstract class AbstractClusteredService<T> extends AbstractRemoteService<T> implements RemoteService<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDirectory.class);

    protected final Directory<T> directory;

    protected final boolean availablecheck;

    private volatile boolean destroyed = false;

    private volatile RemoteService<T> stickyService = null;

    private ConsumerInfo consumerId;

    private Container container;

    public AbstractClusteredService(Directory<T> directory, ConsumerInfo consumerId, Container container)
    {
        Assert.assertNotNull(directory);
        this.container = container;
        this.directory = directory;
        this.consumerId = consumerId;
        this.availablecheck = PropertiesUtils.getBoolean(consumerId.getServiceProperties(), HOLA.CLUSTER_AVAILABLE_CHECK, true);
    }
    
    @Override
    public ServiceProperties getServiceProperties() {
        return directory.getServiceProperties();
    }
    
    public AbstractClusteredService(Directory<T> directory, Container container)
    {
        this(directory, directory.getConsumerInfo(), container);
    }

    @Override
    public Class<T> getServiceClass() {
        return directory.getServiceClass();
    }

    @Override
    public String getAddress() {
        return directory.getAddress();
    }
    
    protected void checkDestroyed(){
        if (destroyed) {
            throw new ClusterException("Cluster remote service on " + consumerId + " is destroyed!");
        }
    }

    @Override
    public Object[] invoke(ClientCallback callback, RemoteRequest request, boolean oneway) throws RemoteException {
       
        checkDestroyed();
        LoadBalance loadbalance = null;
        List<RemoteService<T>> remotes = directory.list(request);
        if (remotes != null && remotes.size() > 0) {
            String loadbalanceKey = PropertiesUtils.getString(consumerId.getServiceProperties(), HOLA.LOADBALANCE_KEY, HOLA.DEFAULT_LOADBALANCE);
            loadbalance = container.getExtensionLoader(LoadBalance.class).getExtension(loadbalanceKey);
        }

        return doInvoke(loadbalance, remotes, request, callback, oneway);
    }

    public abstract Object[] doInvoke(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request, ClientCallback callback,
        boolean oneway) throws RemoteException;

    @Override
    public void destroy() {
        directory.destroy();
        destroyed = true;
    }

    @Override
    public boolean isAvailable() {
        RemoteService<T> service = stickyService;
        if (service != null) {
            return service.isAvailable();
        }
        return directory.isAvailable();
    }

    protected void checkRemoteServices(List<RemoteService<T>> services, RemoteRequest request) throws RemoteException {
        if (services == null || services.size() == 0) {
            throw new RemoteException("Failed to call method " + request.getMethodName() + " on remote service :" + getServiceClass().getName()
                + ".No provider available for the service " + consumerId);
        }
    }

    protected RemoteService<T> select(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request,
        List<RemoteService<T>> selected) {
        if (services == null || services.size() == 0) {
            return null;
        }
        if (stickyService != null || !services.contains(stickyService)) {
            stickyService = null;
        }
        Object value = consumerId.getServiceProperties().get(request.getMethodName() + "." + HOLA.CLUSTER_STICKY_KEY);
        boolean sticky = value == null ? false : DataUtils.asBoolean(value);
        if (sticky && stickyService != null && (selected == null || !selected.contains(stickyService))) {
            if (availablecheck && stickyService.isAvailable()) {
                return stickyService;
            }
        }
        RemoteService<T> remoteservice = doSelect(loadbalance, services, request, selected);
        if (sticky) {
            stickyService = remoteservice;
        }
        return remoteservice;
    }

    protected RemoteService<T> doSelect(LoadBalance loadbalance, List<RemoteService<T>> services, RemoteRequest request,
        List<RemoteService<T>> selected) {
        if (services.size() == 1) {
            return services.get(0);
        }
        // 两个就轮询
        if (services.size() == 2 && selected != null && selected.size() > 0) {
            return selected.get(0) == services.get(0) ? services.get(1) : services.get(0);
        }
        RemoteService<T> remoteservice = loadbalance.select(services, request);
        if ((selected != null && selected.contains(remoteservice)) || (remoteservice.isAvailable() && availablecheck)) {
            try {
                RemoteService<T> rremoteservice = reselect(loadbalance, services, selected, request, availablecheck);
                if (rremoteservice != null) {
                    remoteservice = rremoteservice;
                } else {
                    int index = services.indexOf(remoteservice);
                    try {
                        // 最后在避免碰撞
                        remoteservice = index < services.size() - 1 ? services.get(index + 1) : remoteservice;
                    } catch (Exception e) {
                        LOG.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
                    }
                }

            } catch (Throwable t) {
                LOG.error(
                    "clustor relselect fail reason is :" + t.getMessage() + " if can not slove ,you can set cluster.availablecheck=false in url", t);
            }
        }
        return remoteservice;
    }

    private RemoteService<T> reselect(LoadBalance loadbalance, List<RemoteService<T>> services, List<RemoteService<T>> selected,
        RemoteRequest request, boolean availablecheck) {
        List<RemoteService<T>> reselectedservices = new ArrayList<RemoteService<T>>(services.size() > 1 ? (services.size() - 1) : services.size());
        if (availablecheck) {
            for (RemoteService<T> rs : services) {
                if (rs.isAvailable())
                    if (selected == null || !selected.contains(rs)) {
                        reselectedservices.add(rs);
                    }
            }
            if (reselectedservices.size() > 0) {
                return loadbalance.select(reselectedservices, request);
            }
        } else {
            for (RemoteService<T> rs : services) {
                if (selected == null || !selected.contains(rs)) {
                    reselectedservices.add(rs);
                }
            }
            if (reselectedservices.size() > 0) {
                return loadbalance.select(reselectedservices, request);
            }
        }
        if (selected != null) {
            for (RemoteService<T> rs : selected) {
                if ((rs.isAvailable()) // 优先选available
                    && !reselectedservices.contains(rs)) {
                    reselectedservices.add(rs);
                }
            }
        }
        if (reselectedservices.size() > 0) {
            return loadbalance.select(reselectedservices, request);
        }
        return null;
    }

    
    public ConsumerInfo getConsumerInfo() {
        return consumerId;
    }
    
}
