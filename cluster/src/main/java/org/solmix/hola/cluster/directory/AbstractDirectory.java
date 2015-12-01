
package org.solmix.hola.cluster.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.cluster.ClusterException;
import org.solmix.hola.cluster.ConsumerInfo;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.Router;
import org.solmix.hola.cluster.RouterFactory;
import org.solmix.hola.cluster.router.ExpressionRouterFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.runtime.Container;

public abstract class AbstractDirectory<T> extends Object implements Directory<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDirectory.class);

    private volatile boolean destroyed = false;

    protected volatile List<Router> routers;

    private ConsumerInfo consumer;

    private Container container;
    
    protected Dictionary<String, ?> directoryProperties;
    
    private String address;

    public AbstractDirectory(Container container, Dictionary<String, ?> directoryProperties)
    {
        this(container, null, directoryProperties,new ConsumerInfo(directoryProperties));
    }

    public AbstractDirectory(Container container, List<Router> routers, Dictionary<String, ?> directoryProperties,ConsumerInfo consumer)
    {
        this.container = container;
        this.directoryProperties=directoryProperties;
        address=PropertiesUtils.toAddress(directoryProperties);
        this.consumer = consumer;
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
                    remoteServices = router.route(remoteServices, getConsumerInfo(), request);
                } catch (Exception e) {
                    LOG.error("Failed to excute router on directory :" +PropertiesUtils.toAddress(directoryProperties), e);
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

    @Override
    public ConsumerInfo getConsumerInfo() {
        return consumer;
    }

    public void setRouters(List<Router> routers) {
        routers = routers == null ? new ArrayList<Router>() : new ArrayList<Router>(routers);
        String rule = PropertiesUtils.getString(directoryProperties, HOLA.ROUTER_RULE_KEY) ;

        if (rule != null && rule.toString().length() > 0) {
            String router = PropertiesUtils.getString(directoryProperties, HOLA.ROUTER_KEY,ExpressionRouterFactory.NAME);
            RouterFactory factory = container.getExtensionLoader(RouterFactory.class).getExtension(router);
            routers.add(factory.createRouter(rule.toString()));
        }
        Collections.sort(routers);
        this.routers = routers;
    }

    @Override
    public String getAddress() {
        return address;
    }

}
