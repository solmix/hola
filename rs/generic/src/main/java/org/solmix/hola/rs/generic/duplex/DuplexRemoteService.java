package org.solmix.hola.rs.generic.duplex;

import java.util.Dictionary;

import org.solmix.exchange.Client;
import org.solmix.exchange.Server;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.generic.exchange.HolaRemoteService;
import org.solmix.hola.rs.generic.exchange.HolaServerFactory;
import org.solmix.hola.rs.support.RemoteReferenceImpl;
import org.solmix.hola.transport.netty.NettyTransporter;
import org.solmix.runtime.Container;

public class DuplexRemoteService<T,K> extends HolaRemoteService<T> {

	private Class<K> serverClass;
	private Object service;
	private Server server;

	public DuplexRemoteService(Container container, RemoteReferenceImpl<T> refer,Class<K> serverClass,Object service) {
		super(container, refer);
		this.serverClass=serverClass;
		this.service=service;
	}
	protected Client doGetClient(RemoteRequest request) {
		Client client = super.doGetClient(request);
		EndpointInfo ei = client.getEndpoint().getEndpointInfo();
		server=doSetupServer(serverClass);
		NettyTransporter trans =(NettyTransporter)server.getTransporter();
		ei.addExtension(trans);
		
		return client;
	}
	private Server doSetupServer(Class<K> serverClass) {
		Container container = getClientFactory().getContainer();
		Dictionary<String, ?> properties=clientFactory.getProperties();
		Dictionary<String, Object> serverProperties =PropertiesUtils.toProperties(PropertiesUtils.toAddress(properties));
		serverProperties.put(HOLA.DUPLEX_MODE_KEY, Boolean.TRUE);
		HolaServerFactory factory = new HolaServerFactory(true);
        factory.setContainer(container);
        factory.setProperties(serverProperties);
        factory.setServiceClass(serverClass);
        factory.setServiceBean(service);
        return factory.create();
	}
	  @Override
	    public void destroy() {
	        super.destroy();
	        if(server!=null) {
	        	server.destroy();
	        }
	    }
}
