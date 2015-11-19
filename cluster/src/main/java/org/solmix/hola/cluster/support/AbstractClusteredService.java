package org.solmix.hola.cluster.support;

import java.util.Dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.directory.AbstractDirectory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteService;


public abstract class AbstractClusteredService<T> implements RemoteService<T>
{
    private static final Logger LOG  = LoggerFactory.getLogger(AbstractDirectory.class);
    protected final Directory<T>               directory;

    protected final boolean                    availablecheck;
    
    private volatile boolean                   destroyed = false;
    
    public AbstractClusteredService(Directory<T> directory,Dictionary<String, ?> properties){
        this.directory=directory;
        this.availablecheck=PropertiesUtils.getBoolean(properties, HOLA.CLUSTER_AVAILABLE_CHECK, true);
    }
}
