package org.solmix.hola.rs.jaxws;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.apache.cxf.jaxws.EndpointImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteListener;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.event.RemoteUnregisteredEvent;
import org.solmix.hola.rs.support.ServiceRegistry;


public class JaxwsRegistration<S> implements RemoteRegistration<S>
{
    public static final int REGISTERED = 0x00;

    public static final int UNREGISTERING = 0x01;

    public static final int UNREGISTERED = 0x02;
    
    private static final Logger LOG = LoggerFactory.getLogger(JaxwsRegistration.class);

    protected transient Object registrationLock = new Object();

    protected final S service;

    protected final Class<S> clazze;

    protected int state;

    protected RemoteReference<S> reference;

    private final RemoteServiceFactory manager;

    protected final ServiceRegistry registry;

    protected ServiceProperties properties;

    protected final List<RemoteListener> listeners = new ArrayList<RemoteListener>(4);

    protected EndpointImpl endpoint;
    
    public JaxwsRegistration(RemoteServiceFactory manager, ServiceRegistry registry,
        Class<S> clazze, S service,Dictionary<String,?> properties) {
        this.clazze = clazze;
        this.service = service;
        this.manager = manager;
        this.registry = registry;
        synchronized (registrationLock) {
            this.state = REGISTERED;
            this.properties=createProperties(properties);
            final JaxwsRegistration<S> reg = this;
            reference = new  RemoteReference<S>(){
                boolean available=true;
                @Override
                public org.solmix.hola.rs.RemoteReference.ReferenceType getReferenceType() {
                    return ReferenceType.LOCAL;
                }

                @Override
                public Object getProperty(String key) {
                    return reg.getProperty(key);
                }

                @Override
                public String[] getPropertyKeys() {
                    return reg.getPropertyKeys();
                }

                @Override
                public boolean isAvailable() {
                    return available;
                }

                @Override
                public void destroy() {
                    available=false;
                }

                @Override
                public Class<S> getServiceClass() {
                    return reg.getClazze();
                }

                @Override
                public RemoteServiceFactory getRemoteServiceFactory() {
                    return reg.manager;
                }
                
            };
        }
       
    }

    protected ServiceProperties createProperties(Dictionary<String, ?> props) {
        assert Thread.holdsLock(registrationLock);
        ServiceProperties sp = new ServiceProperties(props);
        sp.setReadOnly();
        return sp;
    }

    public S getServiceObject(){
        return service;
    }
    public Class<S> getClazze(){
        return clazze;
    }

    @Override
    public RemoteReference<S> getReference() {
        return getReferenceImpl();
    }

    public RemoteReference<S> getReferenceImpl() {
        synchronized (registrationLock) {
            if (reference == null) {
                throw new IllegalStateException("Service already nuregistered.");
            }
            return reference;
        }
    }

    @Override
    public void unregister() {
        final RemoteReference<S> ref;
        synchronized (registry) {
            synchronized (registrationLock) {
                if (state != REGISTERED) {
                    throw new IllegalStateException( "Service already unregistered.");
                }
                registry.removeServiceRegistration( this);
                state = UNREGISTERING;
                ref = reference;
            }
        }
        //停止server
        if(endpoint!=null){
            try {
                endpoint.close();
            } catch (Exception e) {
                LOG.error("unregister ",e);
            }
        }
        registry.publishServiceEvent(new RemoteUnregisteredEvent(ref));
        synchronized (registrationLock) {
            state = UNREGISTERED;
        }

    }

    public Object getProperty(String key) {
        synchronized (registrationLock) {
            return properties.getProperty(key);
        }
    }

    /**
     * @return
     */
    public String[] getPropertyKeys() {
        synchronized (registrationLock) {
            return properties.getPropertyKeys();
        }
    }

    /**
     * @return
     */
    public RemoteServiceFactory getManager() {
        synchronized (registrationLock) {
            if (reference == null) {
                return null;
            }
            return manager;
        }
    }

    public Object getService() {
        return service;
    }
    
    public void setEndpoint(EndpointImpl endpoint) {
        this.endpoint=endpoint;
    }

}
