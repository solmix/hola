package org.solmix.hola.http.server;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import org.solmix.hola.http.server.context.VirtualServer;

import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;

public class ServerConfig {

    interface Defaults {

        /**
         * Default bind address. We do not want to use port 80 or 8080.
         */
        HttpAddress ADDRESS = HttpAddress.http1("localhost", 8008);

        /**
         * If frame logging/traffic logging is enabled or not.
         */
        boolean DEBUG = false;

        /**
         * Default debug log level.
         */
        LogLevel DEBUG_LOG_LEVEL = LogLevel.DEBUG;

        /**
         * The default for selecting epoll. If available, select epoll.
         */
        boolean EPOLL =false/** Epoll.isAvailable()**/;

        /**
         * Let Netty decide about parent thread count.
         */
        int PARENT_THREAD_COUNT = 0;

        /**
         * Child thread count. Let Netty decide.
         */
        int CHILD_THREAD_COUNT = 0;

        /**
         * Default for SO_REUSEADDR.
         */
        boolean SO_REUSEADDR = true;

        /**
         * Default for TCP_NODELAY.
         */
        boolean TCP_NODELAY = true;

        /**
         * Set TCP send buffer to 64k per socket.
         */
        int TCP_SEND_BUFFER_SIZE = 64 * 1024;

        /**
         * Set TCP receive buffer to 64k per socket.
         */
        int TCP_RECEIVE_BUFFER_SIZE = 64 * 1024;

        /**
         * Default for socket back log.
         */
        int SO_BACKLOG = 10 * 1024;

        /**
         * Default connect timeout in milliseconds.
         */
        int CONNECT_TIMEOUT_MILLIS = 5000;

        /**
         * Default connect timeout in milliseconds.
         */
        int READ_TIMEOUT_MILLIS = 15000;

        /**
         * Default idle timeout in milliseconds.
         */
        int IDLE_TIMEOUT_MILLIS = 30000;

        /**
         * Set HTTP chunk maximum size to 8k.
         * See {@link io.netty.handler.codec.http.HttpClientCodec}.
         */
        int MAX_CHUNK_SIZE = 8 * 1024;

        /**
         * Set HTTP initial line length to 4k.
         * See {@link io.netty.handler.codec.http.HttpClientCodec}.
         */
        int MAX_INITIAL_LINE_LENGTH = 4 * 1024;

        /**
         * Set HTTP maximum headers size to 8k.
         * See  {@link io.netty.handler.codec.http.HttpClientCodec}.
         */
        int MAX_HEADERS_SIZE = 8 * 1024;

        /**
         * Set maximum content length to 100 MB.
         */
        int MAX_CONTENT_LENGTH = 100 * 1024 * 1024;

        /**
         * This is Netty's default.
         * See {@link io.netty.handler.codec.MessageAggregator#DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS}.
         */
        int MAX_COMPOSITE_BUFFER_COMPONENTS = 1024;

        /**
         * Default write buffer water mark.
         */
        WriteBufferWaterMark WRITE_BUFFER_WATER_MARK = WriteBufferWaterMark.DEFAULT;

        /**
         * Default for gzip codec.
         */
        boolean ENABLE_GZIP = true;

        /**
         * Default HTTP/2 settings.
         */
        Http2Settings HTTP_2_SETTINGS = Http2Settings.defaultSettings();

        /**
         * Default for HTTP/2 upgrade under HTTP 1.
         */
        boolean INSTALL_HTTP_UPGRADE2 = false;

        /**
         * Default SSL provider.
         */
        SslProvider DEFAULT_SSL_PROVIDER = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;

        /**
         * Default ciphers.
         */
        Iterable<String> DEFAULT_CIPHERS = Http2SecurityUtil.CIPHERS;

        /**
         * Default cipher suite filter.
         */
        CipherSuiteFilter DEFAULT_CIPHER_SUITE_FILTER = SupportedCipherSuiteFilter.INSTANCE;
    }

    private static TrustManagerFactory TRUST_MANAGER_FACTORY;

    static {
        try {
            TRUST_MANAGER_FACTORY = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (Exception e) {
            TRUST_MANAGER_FACTORY = null;
        }
    }

    private HttpAddress httpAddress = Defaults.ADDRESS;

    private boolean debug = Defaults.DEBUG;

    private LogLevel debugLogLevel = Defaults.DEBUG_LOG_LEVEL;

    private boolean epoll = Defaults.EPOLL;

    private int parentThreadCount = Defaults.PARENT_THREAD_COUNT;

    private int childThreadCount = Defaults.CHILD_THREAD_COUNT;

    private boolean reuseAddr = Defaults.SO_REUSEADDR;

    private boolean tcpNodelay = Defaults.TCP_NODELAY;

    private int tcpSendBufferSize = Defaults.TCP_SEND_BUFFER_SIZE;

    private int tcpReceiveBufferSize = Defaults.TCP_RECEIVE_BUFFER_SIZE;

    private int backLogSize = Defaults.SO_BACKLOG;

    private int maxInitialLineLength = Defaults.MAX_INITIAL_LINE_LENGTH;

    private int maxHeadersSize = Defaults.MAX_HEADERS_SIZE;

    private int maxChunkSize = Defaults.MAX_CHUNK_SIZE;

    private int maxContentLength = Defaults.MAX_CONTENT_LENGTH;

    private int maxCompositeBufferComponents = Defaults.MAX_COMPOSITE_BUFFER_COMPONENTS;

    private int connectTimeoutMillis = Defaults.CONNECT_TIMEOUT_MILLIS;

    private int readTimeoutMillis = Defaults.READ_TIMEOUT_MILLIS;

    private int idleTimeoutMillis = Defaults.IDLE_TIMEOUT_MILLIS;

    private WriteBufferWaterMark writeBufferWaterMark = Defaults.WRITE_BUFFER_WATER_MARK;

    private boolean enableGzip = Defaults.ENABLE_GZIP;

    private Http2Settings http2Settings = Defaults.HTTP_2_SETTINGS;

    private boolean installHttp2Upgrade = Defaults.INSTALL_HTTP_UPGRADE2;

    private SslProvider sslProvider = Defaults.DEFAULT_SSL_PROVIDER;

    private Iterable<String> ciphers = Defaults.DEFAULT_CIPHERS;

    private CipherSuiteFilter cipherSuiteFilter = Defaults.DEFAULT_CIPHER_SUITE_FILTER;

    private InputStream keyCertChainInputStream;

    private InputStream keyInputStream;

    private String keyPassword;

    private List<VirtualServer> virtualServers;

    private TrustManagerFactory trustManagerFactory = TRUST_MANAGER_FACTORY;

    private KeyStore trustManagerKeyStore = null;

    public ServerConfig() {
        this.virtualServers = new ArrayList<>();
        addVirtualServer(new VirtualServer(null));
    }

    public ServerConfig enableDebug() {
        this.debug = true;
        return this;
    }

    public ServerConfig disableDebug() {
        this.debug = false;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public ServerConfig setDebugLogLevel(LogLevel debugLogLevel) {
        this.debugLogLevel = debugLogLevel;
        return this;
    }

    public LogLevel getDebugLogLevel() {
        return debugLogLevel;
    }

    public ServerConfig enableEpoll() {
        this.epoll = true;
        return this;
    }

    public ServerConfig disableEpoll() {
        this.epoll = false;
        return this;
    }

    public ServerConfig setEpoll(boolean epoll) {
        this.epoll = epoll;
        return this;
    }

    public boolean isEpoll() {
        return epoll;
    }

    public ServerConfig setParentThreadCount(int parentThreadCount) {
        this.parentThreadCount = parentThreadCount;
        return this;
    }

    public int getParentThreadCount() {
        return parentThreadCount;
    }

    public ServerConfig setChildThreadCount(int childThreadCount) {
        this.childThreadCount = childThreadCount;
        return this;
    }

    public int getChildThreadCount() {
        return childThreadCount;
    }

    public ServerConfig setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
        return this;
    }

    public boolean isReuseAddr() {
        return reuseAddr;
    }

    public ServerConfig setTcpNodelay(boolean tcpNodelay) {
        this.tcpNodelay = tcpNodelay;
        return this;
    }

    public boolean isTcpNodelay() {
        return tcpNodelay;
    }

    public ServerConfig setTcpSendBufferSize(int tcpSendBufferSize) {
        this.tcpSendBufferSize = tcpSendBufferSize;
        return this;
    }

    public int getTcpSendBufferSize() {
        return tcpSendBufferSize;
    }

    public ServerConfig setTcpReceiveBufferSize(int tcpReceiveBufferSize) {
        this.tcpReceiveBufferSize = tcpReceiveBufferSize;
        return this;
    }

    public int getTcpReceiveBufferSize() {
        return tcpReceiveBufferSize;
    }

    public ServerConfig setBackLogSize(int backLogSize) {
        this.backLogSize = backLogSize;
        return this;
    }

    public int getBackLogSize() {
        return backLogSize;
    }

    public ServerConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public ServerConfig setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public ServerConfig setIdleTimeoutMillis(int idleTimeoutMillis) {
        this.idleTimeoutMillis = idleTimeoutMillis;
        return this;
    }

    public int getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }

    public ServerConfig setAddress(HttpAddress httpAddress) {
        this.httpAddress = httpAddress;
        return this;
    }

    public HttpAddress getAddress() {
        return httpAddress;
    }

    public ServerConfig setMaxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
        return this;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public ServerConfig setMaxHeadersSize(int maxHeadersSize) {
        this.maxHeadersSize = maxHeadersSize;
        return this;
    }

    public int getMaxHeadersSize() {
        return maxHeadersSize;
    }

    public ServerConfig setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public ServerConfig setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return this;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public ServerConfig setMaxCompositeBufferComponents(int maxCompositeBufferComponents) {
        this.maxCompositeBufferComponents = maxCompositeBufferComponents;
        return this;
    }

    public int getMaxCompositeBufferComponents() {
        return maxCompositeBufferComponents;
    }

    public ServerConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        this.writeBufferWaterMark = writeBufferWaterMark;
        return this;
    }

    public WriteBufferWaterMark getWriteBufferWaterMark() {
        return writeBufferWaterMark;
    }

    public ServerConfig setEnableGzip(boolean enableGzip) {
        this.enableGzip = enableGzip;
        return this;
    }

    public boolean isEnableGzip() {
        return enableGzip;
    }

    public ServerConfig setInstallHttp2Upgrade(boolean http2Upgrade) {
        this.installHttp2Upgrade = http2Upgrade;
        return this;
    }

    public boolean isInstallHttp2Upgrade() {
        return installHttp2Upgrade;
    }

    public ServerConfig setHttp2Settings(Http2Settings http2Settings) {
        this.http2Settings = http2Settings;
        return this;
    }

    public Http2Settings getHttp2Settings() {
        return http2Settings;
    }

    public ServerConfig setSslProvider(SslProvider sslProvider) {
        this.sslProvider = sslProvider;
        return this;
    }

    public SslProvider getSslProvider() {
        return sslProvider;
    }

    public ServerConfig setCiphers(Iterable<String> ciphers) {
        this.ciphers = ciphers;
        return this;
    }

    public Iterable<String> getCiphers() {
        return ciphers;
    }

    public ServerConfig setCipherSuiteFilter(CipherSuiteFilter cipherSuiteFilter) {
        this.cipherSuiteFilter = cipherSuiteFilter;
        return this;
    }

    public CipherSuiteFilter getCipherSuiteFilter() {
        return cipherSuiteFilter;
    }

    public ServerConfig setKeyCertChainInputStream(InputStream keyCertChainInputStream) {
        this.keyCertChainInputStream = keyCertChainInputStream;
        return this;
    }

    public InputStream getKeyCertChainInputStream() {
        return keyCertChainInputStream;
    }

    public ServerConfig setKeyInputStream(InputStream keyInputStream) {
        this.keyInputStream = keyInputStream;
        return this;
    }

    public InputStream getKeyInputStream() {
        return keyInputStream;
    }

    public ServerConfig setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
        return this;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public ServerConfig addVirtualServer(VirtualServer virtualServer) {
        this.virtualServers.add(virtualServer);
        return this;
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public ServerConfig setTrustManagerFactory(TrustManagerFactory trustManagerFactory) {
        this.trustManagerFactory = trustManagerFactory;
        return this;
    }

    public TrustManagerFactory getTrustManagerFactory() {
        return trustManagerFactory;
    }

    public ServerConfig setTrustManagerKeyStore(KeyStore trustManagerKeyStore) {
        this.trustManagerKeyStore = trustManagerKeyStore;
        return this;
    }

    public KeyStore getTrustManagerKeyStore() {
        return trustManagerKeyStore;
    }
}
