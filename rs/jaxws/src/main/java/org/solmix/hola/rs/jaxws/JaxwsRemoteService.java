
package org.solmix.hola.rs.jaxws;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.support.AbstractRemoteService;
import org.solmix.hola.rs.support.InvokeFilterWrapper;
import org.solmix.hola.rs.support.RemoteReferenceImpl;

public class JaxwsRemoteService<T> extends AbstractRemoteService<T> implements RemoteService<T>
{

    private RemoteReferenceImpl<T> refer;

    private JaxWsClientFactoryBean clientFactory;

    private String address;

    protected Object client;

    private ReentrantLock lock = new ReentrantLock();

    private final List<InvokeFilter> filters;

    public JaxwsRemoteService(Bus bus, RemoteReferenceImpl<T> refer, List<InvokeFilter> filters,InterceptorProvider provider)
    {
        this.refer = refer;
        this.filters = filters;
        clientFactory = new JaxWsClientFactoryBean();
        clientFactory.setBus(bus);
        clientFactory.setServiceClass(refer.getServiceClass());
        Hashtable<String, Object> cxfAddress = new Hashtable<String, Object>();
        cxfAddress.put(HOLA.PROTOCOL_KEY, "http");
        cxfAddress.put(HOLA.HOST_KEY, refer.getProperty(HOLA.HOST_KEY));
        if (refer.getProperty(HOLA.PORT_KEY) != null)
            cxfAddress.put(HOLA.PORT_KEY, refer.getProperty(HOLA.PORT_KEY));
        cxfAddress.put(HOLA.PATH_KEY, refer.getProperty(HOLA.PATH_KEY));
        clientFactory.setAddress(PropertiesUtils.toAddress(cxfAddress));
        if(provider!=null){
        	if(provider.getInInterceptors()!=null)
        	clientFactory.getInInterceptors().addAll(provider.getInInterceptors());
        	if(provider.getOutInterceptors()!=null)
        	clientFactory.getOutInterceptors().addAll(provider.getOutInterceptors());
        	if(provider.getInFaultInterceptors()!=null)
        	clientFactory.getInFaultInterceptors().addAll(provider.getInFaultInterceptors());
        	if(provider.getOutFaultInterceptors()!=null)
        	clientFactory.getOutFaultInterceptors().addAll(provider.getOutFaultInterceptors());
        }
        this.address = PropertiesUtils.toAddress(refer.getServiceProperties());
    }

    public JaxWsClientFactoryBean getJaxWsClientFactoryBean() {
        return clientFactory;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean isAvailable() {
        return client != null;
    }

    @Override
    public void destroy() {
        refer.setRemoteService(null);
        refer.destroy();

    }

    @Override
    public Class<T> getServiceClass() {
        return refer.getServiceClass();
    }

    @Override
    public ServiceProperties getServiceProperties() {
        return refer.getServiceProperties();
    }

    @Override
    public Object[] invoke(ClientCallback callback, RemoteRequest request, boolean oneway) throws RemoteException {
        lock.lock();
        try {
            if (client == null) {
                client = getProxyObject();
            }
        } finally {
            lock.unlock();
        }
        Object result = null;
        try {
            result = Reflection.invokeMethod(client, request.getMethod(), request.getParameters());
        } catch (Exception e) {
            throw new RemoteException(e);
        }
        return new Object[] { result };
    }
    
    public Object getProxyObject(){
        JaxWsProxyFactoryBean pf = new JaxWsProxyFactoryBean(clientFactory);
        Object  client = pf.create();
        if(DataUtils.isNotNullAndEmpty(filters)){
            return new InvokeFilterWrapper(client, filters,refer.getServiceProperties());
        }else{
            return client;
        }
    }

}
