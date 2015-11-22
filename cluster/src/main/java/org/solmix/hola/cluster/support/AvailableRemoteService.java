package org.solmix.hola.cluster.support;

import org.solmix.exchange.Client;
import org.solmix.hola.cluster.Directory;


public class AvailableRemoteService<T> extends AbstractClusteredService<T>
{

    public AvailableRemoteService(Directory<T> directory)
    {
        super(directory, directory.getConsumerServiceID());
    }

    @Override
    protected Client doGetClient() {
        // TODO Auto-generated method stub
        return null;
    }

}
