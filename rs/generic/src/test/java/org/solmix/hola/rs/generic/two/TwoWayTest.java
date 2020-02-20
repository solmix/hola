package org.solmix.hola.rs.generic.two;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.RemoteProxyFactory;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.hola.rs.call.DefaultRemoteRequest;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.call.RemoteResponse;
import org.solmix.hola.rs.generic.HolaRemoteServiceFactory;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;

import io.netty.channel.Channel;

public class TwoWayTest extends Assert {

	String TOKEN = "TOKEN-Semcp334m32";

	@Test
	public void test() throws InterruptedException {
		RemoteServiceManager rm = container.getExtension(RemoteServiceManager.class);
		assertNotNull(rm);
		HolaRemoteServiceFactory rsf = (HolaRemoteServiceFactory) rm.getRemoteServiceFactory("hola");
		assertNotNull(rsf);
		DefaultStationService hs = new DefaultStationService();
		DefaultAgentService agentservice = new DefaultAgentService();
		// 注册
		RemoteRegistration<StationService> reg = rsf.register(StationService.class, hs, mockConfig());

		Dictionary<String, Object> properties = mockConfig();
		properties.put(HOLA.HEARTBEAT_KEY, 1000);
		RemoteReference<StationService> reference = rsf.getReference(StationService.class, properties);
		assertNotNull(reference);
		RemoteService<StationService> remote = rsf.getDuplexRemoteService(reference,AgentService.class,agentservice);
		assertNotNull(remote);
		
		RemoteRequest serverReq = new DefaultRemoteRequest("login", new Object[] { TOKEN });
		RemoteResponse serverRes = remote.sync(serverReq);
		RemoteProxyFactory.getProxy(remote,container);
		System.out.println("************:"+serverRes.getValue());

		//
		Map<String, Object> table = new HashMap<String, Object>();
		table.put(Channel.class.getName(), hs.getChannel());
		Dictionary<String, Object> props = mockConfig();
		props.put(HOLA.HOST_KEY, "share");
		RemoteReference<AgentService> aref =  rsf.getReference(AgentService.class,mockConfig());
		RemoteService<AgentService> agent = rsf.getRemoteService(aref);

		RemoteRequest request = new DefaultRemoteRequest("getConfig", new Object[] {  }, table);
		RemoteResponse res = agent.sync(request);
		System.out.println("************:"+res.getValue());

		reference.destroy();
		reg.unregister();
	}

	public static final int PORT = 55555/* NetUtils.getAvailablePort() */;

	private Dictionary<String, Object> mockConfig() {
		Hashtable<String, Object> table = new Hashtable<String, Object>();
		table.put(HOLA.PATH_KEY, "/hola");
		table.put(HOLA.PORT_KEY, PORT);
		table.put(HOLA.TIMEOUT_KEY, 1000 * 600);
//        table.put(HOLA.TRANSPORTER_KEY, "local");
		table.put(HOLA.HOST_KEY, HOLA.LOCALHOST_VALUE);
		// ipv6
//        table.put(HOLA.HOST_KEY, "111::2");
		return table;
	}

	static Container container;

	@BeforeClass
	public static void setup() {
		ContainerFactory.setThreadDefaultContainer(null);
		container = ContainerFactory.getThreadDefaultContainer(true);
	}

	@AfterClass
	public static void tearDown() {
		if (container != null) {
			container.close();
		}
	}
}
