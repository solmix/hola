
package org.solmix.hola.discovery.rs;

import java.util.Dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.Discovery;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.support.AbstractRemoteServiceFactory;
import org.solmix.runtime.Extension;

@Extension(name = HOLA.DISCOVERY)
@SuppressWarnings("rawtypes")
public class DiscoveryRemoteServiceFactory extends AbstractRemoteServiceFactory
{

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryRemoteServiceFactory.class);

    private RemoteServiceFactory factory;

    public void setRemoteServiceFactory(RemoteServiceFactory factory) {
        this.factory = factory;
    }

    @Override
    public <S> RemoteRegistration<S> doRegister(Class<S> clazz, S service, Dictionary properties) throws RemoteException {
        RemoteRegistration<S> reg = doLocalRegister(clazz, service, properties);
        // discovery
        final Discovery dis = getDiscovery();
        return reg;
    }

    @SuppressWarnings("unchecked")
    private <S> RemoteRegistration<S> doLocalRegister(Class<S> clazz, S service, Dictionary properties) {
        String discoveryUrl = PropertiesUtils.getStringAndDecoded(properties, HOLA.DISCOVERY_URL);
        if (DataUtils.isEmpty(discoveryUrl)) {
            throw new IllegalArgumentException("The discovery url is null! discovery: " + PropertiesUtils.toAddress(properties));
        }
        Dictionary<String, ?> disp = PropertiesUtils.toProperties(discoveryUrl);
        return factory.register(clazz, service, disp);
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RemoteService getRemoteService(RemoteReference<?> reference) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <S> S doGetRemoteService(RemoteReference<S> reference) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

}
