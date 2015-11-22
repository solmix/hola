
package org.solmix.hola.cluster.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.directory.AbstractDirectory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.support.BaseRemoteService;

public abstract class AbstractClusteredService<T> extends BaseRemoteService<T> implements RemoteService<T>
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDirectory.class);

    protected final Directory<T> directory;

    protected final boolean availablecheck;

    private volatile boolean destroyed = false;

    public AbstractClusteredService(Directory<T> directory, ServiceID consumerId)
    {
        Assert.assertNotNull(directory);
        this.directory = directory;
        this.availablecheck = PropertiesUtils.getBoolean(consumerId.getServiceProperties(), HOLA.CLUSTER_AVAILABLE_CHECK, true);
    }

    @Override
    public void destroy() {
        super.destroy();
        directory.destroy();
        destroyed = true;
    }
}
