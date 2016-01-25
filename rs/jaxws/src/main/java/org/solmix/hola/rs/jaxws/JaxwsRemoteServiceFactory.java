
package org.solmix.hola.rs.jaxws;

import java.util.Dictionary;

import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteListener;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteReference.ReferenceType;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.support.AbstractRemoteServiceFactory;
import org.solmix.hola.rs.support.RemoteReferenceHolder;
import org.solmix.hola.rs.support.RemoteReferenceImpl;
import org.solmix.hola.rs.support.ServiceRegistry;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;

@Extension(name=JaxwsRemoteServiceFactory.PROVIDER_ID)
public class JaxwsRemoteServiceFactory implements RemoteServiceFactory, ContainerAware
{
    public static final String PROVIDER_ID = "jaxws";
    
    protected volatile ServiceRegistry registry = new ServiceRegistry(this);

    protected Container container;
    
    private final ExtensionManagerBus bus = new ExtensionManagerBus();

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public RemoteRegistration<?> register(String clazze, Object service, Dictionary<String, ?> properties) throws RemoteException {
        if (StringUtils.isEmpty(clazze)) {
            throw new IllegalArgumentException("register class  is null");
        }
        @SuppressWarnings("unchecked")
        Class<Object> cls = (Class<Object>) AbstractRemoteServiceFactory.checkServiceClass(clazze, service);
        if (cls == null) {
            throw new RemoteException("No found class :" + clazze);
        }
        RemoteRegistration<Object> reg = register(cls, service, properties);
        return reg;
    }

    @Override
    public <S> RemoteRegistration<S> register(Class<S> clazze, S service, Dictionary<String, ?> properties) throws RemoteException {
        String address = PropertiesUtils.getString(properties, HOLA.PATH_KEY);
        Assert.assertNotNull(address,"cxf webservice publish address is null ,check <hola:service protocol=\"jaxws\" path/>");
        EndpointImpl jaxws = new EndpointImpl(bus,service);
        jaxws.setAddress(address);
        jaxws.publish();
        JaxwsRegistration<S> reg  = new JaxwsRegistration<S>(this, registry, clazze, service, properties);
        reg.setEndpoint(jaxws);
        return reg;
    }

    @Override
    public <S> RemoteReference<S> getReference(Class<S> clazz, Dictionary<String, ?> properties) {
        RemoteReference<S> refer=  new RemoteReferenceImpl<S>(clazz,registry,properties,this);
        if(refer!=null){
            registry.addServiceReference(refer);
        }
        return refer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getService(RemoteReference<S> reference) {
        Assert.assertNotNull(reference,"RemoteReference");
        ReferenceType type = reference.getReferenceType();
        if(type==ReferenceType.LOCAL){
           return (S) registry.getService((RemoteReferenceHolder<S>)reference);
        }else if(type==ReferenceType.REMOTE){
            return doGetService(reference);
        }
        return null;
    }

   

    @Override
    public <S> RemoteService<S> getRemoteService(RemoteReference<S> reference) {
        ReferenceType type = reference.getReferenceType();
        if(type==ReferenceType.LOCAL){
            throw new IllegalArgumentException("Reference is Local"); 
        }else if(type==ReferenceType.REMOTE){
            RemoteReferenceImpl<S> impl =(RemoteReferenceImpl<S>)reference;
            if(impl.getRemoteService()!=null){
                return impl.getRemoteService();
            }
            return doGetRemoteService(impl);
        }
        return null;
    }

    private  <S>RemoteService<S> doGetRemoteService(RemoteReferenceImpl<S> impl) {
        // TODO Auto-generated method stub
        return null;
    }
    private<S> S doGetService(RemoteReference<S> reference) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRemoteListener(RemoteListener listener) {
        registry.addRemoteListener(listener);
    }

    @Override
    public void removeRemoteListener(RemoteListener listener) {
        registry.removeRemoteListener(listener);
    }

    @Override
    public void destroy() {
        registry.destroy();
    }

}
