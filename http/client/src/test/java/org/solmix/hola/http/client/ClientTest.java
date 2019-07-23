package org.solmix.hola.http.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.http.client.transport.Transport;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.security.DefaultSSLProvider;
import org.solmix.runtime.security.KeystoreInfo;
import org.solmix.test.TestUtils;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.proxy.HttpProxyHandler;

public class ClientTest {

	static Container c;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestUtils.load();
		c = ContainerFactory.getDefaultContainer(true);
	}
	@AfterClass
	public static void shutDownAfterClass() throws Exception {
		if(c!=null) {
			c.close();
		}
	}
	@Test
	public void mock() {
		
	}
	private KeystoreInfo create() {
		KeystoreInfo ki = new KeystoreInfo();
		ki.setAlias("test");
		ki.setFilePassword("tes1231t");
		ki.setFilePath(TestUtils.destdir().getAbsolutePath()+File.separator+"keystore");
		ki.setIsDefault(true);
		ki.setKeyCN("LOc");
		return ki;
		
	}
	
	private FullHttpResponse response;

	public void setResponse(FullHttpResponse response) {
		this.response = response.copy();
	}
	@Test
	public void testssl() throws IOException, InterruptedException, ExecutionException {
		 
		DefaultSSLProvider provider =new DefaultSSLProvider(c, create(), true,true);
		SSLEngine ssl=provider.getSSLContext().createSSLEngine();
		ssl.setUseClientMode(true);
		Client client=Client.builder()
//					.enableDebug()
					.setConnectTimeoutMillis(3000)
					.setReadTimeoutMillis(3000)
					.setSslEngine(ssl)
					.build();
		Request  req = Request.builder(HttpMethod.GET)
							  .uri("https://www.tmall.com")
							  .setUserAgent(UserAgent.getUserAgent())
							  .addHeader(HttpHeaderNames.HOST.toString(), "localhost")
							  .build()
							  .setResponseListener(this::setResponse); 
//		(res)->{
//			  FullHttpResponse  f=res;
//			  System.out.println(f.content().toString(StandardCharsets.UTF_8));
//			  System.out.println(f.headers().get(HttpHeaderNames.CONTENT_LENGTH));
//		  }
		
		Transport t=	client.execute(req);
		t.get();
		System.out.println(response.content().toString(StandardCharsets.UTF_8));
		
	}
//	@Test
	public void test() throws IOException, InterruptedException, ExecutionException {
		HttpProxyHandler proxy = new HttpProxyHandler(new InetSocketAddress("localhost", 6666));
		Client client=Client.builder()
					.enableDebug()
					.setConnectTimeoutMillis(3000)
					.setReadTimeoutMillis(3000)
					//http代理
					//.setHttpProxyHandler(proxy)
					//http2.0协商
					//.enableNegotiation(true)
					//jdk ssl provider
					//.setJdkSslProvider()
					//.setSslContextProvider(provider)
					.build();
		FullHttpResponse f;
		Request  req = Request.builder(HttpMethod.GET)
							  .uri("http://www.baidu.com")
							  .setUserAgent(UserAgent.getUserAgent())
							  .addHeader(HttpHeaderNames.HOST.toString(), "localhost")
							  .build()
							  .setResponseListener((res)->{
								  System.out.println(res.content().toString(StandardCharsets.UTF_8));
							  }); 
		
		
		Transport t=	client.execute(req);
		t.get();
		client.logDiagnostics(Level.INFO);
	}

}
