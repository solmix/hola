package org.solmix.hola.http.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.exchange.util.NetworkUtils;
import org.solmix.hola.http.client.handler.Http2ChannelInitializer;
import org.solmix.hola.http.client.handler.HttpChannelInitializer;
import org.solmix.hola.http.client.transport.BoundedChannelPool;
import org.solmix.hola.http.client.transport.Http2Transport;
import org.solmix.hola.http.client.transport.HttpTransport;
import org.solmix.hola.http.client.transport.Transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

public final class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private static final ThreadFactory httpClientThreadFactory = new NamedThreadFactory("Hola-HTTP-C",true);

    static {
        if (System.getProperty("hola.http.client.properties") != null) {
            NetworkUtils.extendSystemProperties();
        }
        // change Netty defaults to safer ones, but still allow override from arg line
        if (System.getProperty("io.netty.noUnsafe") == null) {
            System.setProperty("io.netty.noUnsafe", Boolean.toString(true));
        }
        if (System.getProperty("io.netty.noKeySetOptimization") == null) {
            System.setProperty("io.netty.noKeySetOptimization", Boolean.toString(true));
        }
    }

    private final ClientConfig clientConfig;

    private final ByteBufAllocator byteBufAllocator;

    private final EventLoopGroup eventLoopGroup;

    private final Class<? extends SocketChannel> socketChannelClass;

    private final Bootstrap bootstrap;

    private final List<Transport> transports;

    private BoundedChannelPool<HttpAddress> pool;

    public Client() {
        this(new ClientConfig());
    }

    public Client(ClientConfig clientConfig) {
        this(clientConfig, null, null, null);
    }

    public Client(ClientConfig clientConfig, ByteBufAllocator byteBufAllocator,
                  EventLoopGroup eventLoopGroup, Class<? extends SocketChannel> socketChannelClass) {
        Objects.requireNonNull(clientConfig);
        this.clientConfig = clientConfig;
        initializeTrustManagerFactory(clientConfig);
        this.byteBufAllocator = byteBufAllocator != null ?
                byteBufAllocator : ByteBufAllocator.DEFAULT;
        this.eventLoopGroup = eventLoopGroup != null ? eventLoopGroup : /**clientConfig.isEpoll() ?
                    new EpollEventLoopGroup(clientConfig.getThreadCount(), httpClientThreadFactory) :**/
                    new NioEventLoopGroup(clientConfig.getThreadCount(), httpClientThreadFactory);
        this.socketChannelClass = socketChannelClass != null ? socketChannelClass : /**clientConfig.isEpoll() ?
                EpollSocketChannel.class : **/
                	NioSocketChannel.class;
        this.bootstrap = new Bootstrap()
                .group(this.eventLoopGroup)
                .channel(this.socketChannelClass)
                .option(ChannelOption.ALLOCATOR, byteBufAllocator)
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNodelay())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isKeepAlive())
                .option(ChannelOption.SO_REUSEADDR, clientConfig.isReuseAddr())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getTcpSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getTcpReceiveBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, clientConfig.getWriteBufferWaterMark());
        this.transports = new CopyOnWriteArrayList<>();
        if (!clientConfig.getPoolNodes().isEmpty()) {
            List<HttpAddress> nodes = clientConfig.getPoolNodes();
            Integer limit = clientConfig.getPoolNodeConnectionLimit();
            if (limit == null || limit < 1) {
                limit = 1;
            }
            Semaphore semaphore = new Semaphore(limit);
            Integer retries = clientConfig.getRetriesPerPoolNode();
            if (retries == null || retries < 0) {
                retries = 0;
            }
            ClientChannelPoolHandler clientChannelPoolHandler = new ClientChannelPoolHandler(clientConfig);
            this.pool = new BoundedChannelPool<>(semaphore, clientConfig.getPoolVersion(),
                    nodes, bootstrap, clientChannelPoolHandler, retries,
                    BoundedChannelPool.PoolKeySelectorType.ROUNDROBIN);
            Integer nodeConnectionLimit = clientConfig.getPoolNodeConnectionLimit();
            if (nodeConnectionLimit == null || nodeConnectionLimit == 0) {
                nodeConnectionLimit = nodes.size();
            }
            try {
                this.pool.prepare(nodeConnectionLimit);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public ByteBufAllocator getByteBufAllocator() {
        return byteBufAllocator;
    }

    public boolean hasPooledConnections() {
        return pool != null && !clientConfig.getPoolNodes().isEmpty();
    }

    public void logDiagnostics(Level level) {
        logger.log(level, () -> "OpenSSL available: " + OpenSsl.isAvailable() +
                " OpenSSL ALPN support: " + OpenSsl.isAlpnSupported() +
                " Local host name: " + NetworkUtils.getLocalHostName("localhost") +
                " event loop group: " + eventLoopGroup +
                " socket: " + socketChannelClass.getName() +
                " allocator: " + byteBufAllocator.getClass().getName());
        logger.log(level, NetworkUtils::displayNetworkInterfaces);
    }

    public Transport newTransport() {
        return newTransport(null);
    }

    public Transport newTransport(HttpAddress httpAddress) {
        Transport transport;
        if (httpAddress != null) {
            if (httpAddress.getVersion().majorVersion() == 1) {
                transport = new HttpTransport(this, httpAddress);
            } else {
                transport = new Http2Transport(this, httpAddress);
            }
        } else if (hasPooledConnections()) {
            if (pool.getVersion().majorVersion() == 1) {
                transport = new HttpTransport(this, null);
            } else {
                transport = new Http2Transport(this, null);
            }
        } else {
            throw new IllegalStateException("no address given to connect to");
        }
        transports.add(transport);
        return transport;
    }

    public Channel newChannel(HttpAddress httpAddress) throws IOException {
        Channel channel;
        if (httpAddress != null) {
            HttpVersion httpVersion = httpAddress.getVersion();
            ChannelInitializer<Channel> initializer;
            
            SslHandler sslHandler=null ;
            if(httpAddress.isSecure()) {
            	if(clientConfig.getSslEngine()!=null) {
            		sslHandler=new SslHandler(clientConfig.getSslEngine());
            	}else {
            		sslHandler= newSslHandler(clientConfig, byteBufAllocator, httpAddress);
            	}
            }
            if (httpVersion.majorVersion() == 1) {
                initializer = new HttpChannelInitializer(clientConfig, httpAddress, sslHandler,
                        new Http2ChannelInitializer(clientConfig, httpAddress, sslHandler));
            } else {
                initializer = new Http2ChannelInitializer(clientConfig, httpAddress, sslHandler);
            }
            try {
                channel = bootstrap.handler(initializer)
                        .connect(httpAddress.getInetSocketAddress()).sync().await().channel();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        } else {
            if (hasPooledConnections()) {
                try {
                    channel = pool.acquire();
                } catch (Exception e) {
                    throw new IOException(e);
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return channel;
    }

    public void releaseChannel(Channel channel, boolean close) throws IOException{
        if (channel == null) {
            return;
        }
        if (hasPooledConnections()) {
            try {
                pool.release(channel, close);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if (close) {
           channel.close();
        }
    }

    public Transport execute(Request request) throws IOException {
        Transport transport = newTransport(HttpAddress.of(request.url(), request.httpVersion()));
        transport.execute(request);
        return transport;
    }

    public <T> CompletableFuture<T> execute(Request request,
                                            Function<FullHttpResponse, T> supplier) throws IOException {
        return newTransport(HttpAddress.of(request.url(), request.httpVersion()))
                .execute(request, supplier);
    }

    /**
     * For following redirects, construct a new transport.
     * @param transport the previous transport
     * @param request the new request for continuing the request.
     * @throws IOException if continuation fails
     */
    public void continuation(Transport transport, Request request) throws IOException {
        Transport nextTransport = newTransport(HttpAddress.of(request.url(), request.httpVersion()));
        nextTransport.setCookieBox(transport.getCookieBox());
        nextTransport.execute(request);
        nextTransport.get();
        close(nextTransport);
    }

    /**
     * Retry request by following a back-off strategy.
     *
     * @param transport the transport to retry
     * @param request the request to retry
     * @throws IOException if retry failed
     */
    public void retry(Transport transport, Request request) throws IOException {
        transport.execute(request);
        transport.get();
        close(transport);
    }

    public void close(Transport transport) throws IOException {
        transport.close();
        transports.remove(transport);
    }

    public void close() throws IOException {
        for (Transport transport : transports) {
            close(transport);
        }
        // how to wait for all responses for the pool?
        if (hasPooledConnections()) {
            pool.close();
        }
    }

    public void shutdownGracefully() throws IOException {
        close();
        shutdown();
    }

    public void shutdown() {
        eventLoopGroup.shutdownGracefully();
        try {
            eventLoopGroup.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Initialize trust manager factory once per client lifecycle.
     * @param clientConfig the client config
     */
    private static void initializeTrustManagerFactory(ClientConfig clientConfig) {
        TrustManagerFactory trustManagerFactory = clientConfig.getTrustManagerFactory();
        if (trustManagerFactory != null) {
            try {
                trustManagerFactory.init(clientConfig.getTrustManagerKeyStore());
            } catch (KeyStoreException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private static SslHandler newSslHandler(ClientConfig clientConfig, ByteBufAllocator allocator, HttpAddress httpAddress) {
        try {
            SslContext sslContext = newSslContext(clientConfig, httpAddress.getVersion());
            InetSocketAddress peer = httpAddress.getInetSocketAddress();
            SslHandler sslHandler = sslContext.newHandler(allocator, peer.getHostName(), peer.getPort());
            SSLEngine engine = sslHandler.engine();
            List<String> serverNames = clientConfig.getServerNamesForIdentification();
            if (serverNames.isEmpty()) {
                serverNames = Collections.singletonList(peer.getHostName());
            }
            SSLParameters params = engine.getSSLParameters();
            // use sslContext.newHandler(allocator, peerHost, peerPort) when using params.setEndpointIdentificationAlgorithm
            params.setEndpointIdentificationAlgorithm("HTTPS");
            List<SNIServerName> sniServerNames = new ArrayList<>();
            for (String serverName : serverNames) {
                sniServerNames.add(new SNIHostName(serverName));
            }
            params.setServerNames(sniServerNames);
            engine.setSSLParameters(params);
            switch (clientConfig.getClientAuthMode()) {
                case NEED:
                    engine.setNeedClientAuth(true);
                    break;
                case WANT:
                    engine.setWantClientAuth(true);
                    break;
                default:
                    break;
            }
            return sslHandler;
        } catch (SSLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static SslContext newSslContext(ClientConfig clientConfig, HttpVersion httpVersion) throws SSLException {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                .sslProvider(clientConfig.getSslProvider())
                .ciphers(Http2SecurityUtil.CIPHERS, clientConfig.getCipherSuiteFilter())
                .applicationProtocolConfig(newApplicationProtocolConfig(httpVersion));
        if (clientConfig.getSslContextProvider() != null) {
            sslContextBuilder.sslContextProvider(clientConfig.getSslContextProvider());
        }
        if (clientConfig.getTrustManagerFactory() != null) {
            sslContextBuilder.trustManager(clientConfig.getTrustManagerFactory());
        }
        return sslContextBuilder.build();
    }

    private static ApplicationProtocolConfig newApplicationProtocolConfig(HttpVersion httpVersion) {
        return httpVersion.majorVersion() == 1 ?
                new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_1_1) :
                new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2);
    }


    class ClientChannelPoolHandler implements ChannelPoolHandler {
    	
    	private ClientConfig clientConfig;
        public ClientChannelPoolHandler(ClientConfig clientConfig) {
			this.clientConfig=clientConfig;
		}

		@Override
        public void channelReleased(Channel channel) {
        }

        @Override
        public void channelAcquired(Channel channel) {
        }

        @Override
        public void channelCreated(Channel channel) {
            HttpAddress httpAddress = channel.attr(pool.getAttributeKey()).get();
            HttpVersion httpVersion = httpAddress.getVersion();
            SslHandler sslHandler =null;
            if(httpAddress.isSecure()) {
            	if(clientConfig.getSslEngine()!=null) {
            		sslHandler=  new SslHandler(clientConfig.getSslEngine());
            	}else {
            		sslHandler= newSslHandler(clientConfig, byteBufAllocator, httpAddress);
            	}
            	
            }
            if (httpVersion.majorVersion() == 1) {
                HttpChannelInitializer initializer = new HttpChannelInitializer(clientConfig, httpAddress, sslHandler,
                        new Http2ChannelInitializer(clientConfig, httpAddress, sslHandler));
                initializer.initChannel(channel);
            } else {
                Http2ChannelInitializer initializer = new Http2ChannelInitializer(clientConfig, httpAddress, sslHandler);
                initializer.initChannel(channel);
            }
        }
    }
}
