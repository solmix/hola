package org.solmix.hola.rs.jaxws;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.solmix.commons.util.Reflection;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.support.AbstractRemoteService;
import org.solmix.hola.rs.support.RemoteReferenceImpl;

public class JaxwsRemoteService<T> extends AbstractRemoteService<T> implements
		RemoteService<T> {
	private RemoteReferenceImpl<T> refer;

	private JaxWsClientFactoryBean clientFactory;

	private String address;

	protected Object client;

	private ReentrantLock lock = new ReentrantLock();

	public JaxwsRemoteService() {

	}

	public JaxwsRemoteService(Bus bus, RemoteReferenceImpl<T> refer) {
		this.refer = refer;
		clientFactory = new JaxWsClientFactoryBean();
		clientFactory.setBus(bus);
		clientFactory.setServiceClass(refer.getServiceClass());
		Hashtable<String, Object> cxfAddress = new Hashtable<String, Object>();
		cxfAddress.put(HOLA.PROTOCOL_KEY, "http");
		cxfAddress.put(HOLA.HOST_KEY, refer.getProperty(HOLA.HOST_KEY));
		if(refer.getProperty(HOLA.PORT_KEY)!=null)
		cxfAddress.put(HOLA.PORT_KEY, refer.getProperty(HOLA.PORT_KEY));
		cxfAddress.put(HOLA.PATH_KEY, refer.getProperty(HOLA.PATH_KEY));
		clientFactory.setAddress(PropertiesUtils.toAddress(cxfAddress));

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
	public Object[] invoke(ClientCallback callback, RemoteRequest request,
			boolean oneway) throws RemoteException {
		lock.lock();
		try {
			if (client == null) {
				JaxWsProxyFactoryBean pf = new JaxWsProxyFactoryBean(
						clientFactory);
				client = pf.create();
			}
		} finally {
			lock.unlock();
		}
		Object result = null;
		try {
			result = Reflection.invokeMethod(client, request.getMethod(),
					request.getParameters());
		} catch (Exception e) {
			throw new RemoteException(e);
		}
		return new Object[] { result };
	}

}
