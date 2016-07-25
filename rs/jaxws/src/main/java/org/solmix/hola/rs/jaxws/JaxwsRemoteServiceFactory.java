
package org.solmix.hola.rs.jaxws;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.EndpointImpl;
import org.solmix.commons.util.Assert;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterFactory;
import org.solmix.hola.rs.support.AbstractRemoteServiceFactory;
import org.solmix.hola.rs.support.RemoteReferenceImpl;
import org.solmix.runtime.ContainerAware;
import org.solmix.runtime.Extension;
import org.solmix.runtime.extension.ExtensionLoader;

@Extension(name=JaxwsRemoteServiceFactory.PROVIDER_ID)
public class JaxwsRemoteServiceFactory extends AbstractRemoteServiceFactory implements RemoteServiceFactory, ContainerAware
{
    public static final String PROVIDER_ID = "jaxws";

    
    private final ExtensionManagerBus bus = new ExtensionManagerBus();


    @SuppressWarnings("unchecked")
    @Override
    protected <S> S doGetService(RemoteReference<S> reference) throws RemoteException {
        JaxwsRemoteService<S> service = (JaxwsRemoteService<S>)getRemoteService(reference);
        return (S)service.getProxyObject();
    }

    @Override
    protected <S> RemoteService<S> doGetRemoteService(RemoteReferenceImpl<S> reference) throws RemoteException {
       
        List<?>   fstring =PropertiesUtils.getCommaSeparatedList(reference.getServiceProperties(), HOLA.FILTER_KEY);
        List<InvokeFilter> filters =null;
        if(fstring!=null&&fstring.size()>0){
            filters = new ArrayList<InvokeFilter>();
            ExtensionLoader<InvokeFilterFactory> loader = container.getExtensionLoader(InvokeFilterFactory.class);
            for(Object f:fstring){
                InvokeFilterFactory factory = loader.getExtension(f.toString());
                if(factory!=null){
                    InvokeFilter ivf = factory.create(reference.getServiceProperties());
                    filters.add(ivf);
                }
            }
        }
        InterceptorProvider provider=  container.getExtension(InterceptorProvider.class);
        return new JaxwsRemoteService<S>(bus,reference,filters,provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> RemoteRegistration<S> doRegister(Class<S> clazz, S service, Dictionary properties) throws RemoteException {
        String address = PropertiesUtils.getString(properties, HOLA.PATH_KEY);
        Assert.assertNotNull(address,"cxf webservice publish address is null ,check <hola:service protocol=\"jaxws\" path/>");
        EndpointImpl jaxws = new EndpointImpl(bus,service);
        InterceptorProvider provider=  container.getExtension(InterceptorProvider.class);
        if(provider!=null){
        	if(provider.getInInterceptors()!=null)
        		jaxws.getInInterceptors().addAll(provider.getInInterceptors());
            	if(provider.getOutInterceptors()!=null)
            		jaxws.getOutInterceptors().addAll(provider.getOutInterceptors());
            	if(provider.getInFaultInterceptors()!=null)
            		jaxws.getInFaultInterceptors().addAll(provider.getInFaultInterceptors());
            	if(provider.getOutFaultInterceptors()!=null)
            		jaxws.getOutFaultInterceptors().addAll(provider.getOutFaultInterceptors());
        }
        jaxws.setAddress(address);
        jaxws.publish();
        JaxwsRegistration<S> reg  = new JaxwsRegistration<S>(this, registry, clazz, service, properties);
        reg.setEndpoint(jaxws);
        return reg;
    }

}
