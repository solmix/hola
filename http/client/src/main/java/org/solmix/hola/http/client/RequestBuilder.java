package org.solmix.hola.http.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.Base64Utils;
import org.solmix.exchange.QueryParameters;
import org.solmix.exchange.URL;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;

public class RequestBuilder {

    private static final HttpMethod DEFAULT_METHOD = HttpMethod.GET;

    private static final HttpVersion DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_1;

    private static final String DEFAULT_USER_AGENT = UserAgent.getUserAgent();

    private static final URL DEFAULT_URL =  URL.from("http://localhost");

    private static final boolean DEFAULT_GZIP = true;

    private static final boolean DEFAULT_KEEPALIVE = true;

    private static final boolean DEFAULT_FOLLOW_REDIRECT = true;

    private static final long DEFAULT_TIMEOUT_MILLIS = -1L;

    private static final int DEFAULT_MAX_REDIRECT = 10;

    private static final HttpVersion HTTP_2_0 = HttpVersion.valueOf("HTTP/2.0");

    private final ByteBufAllocator allocator;

    private final List<String> removeHeaders;

    private final Collection<Cookie> cookies;

    private HttpMethod httpMethod;

    private HttpHeaders headers;

    private HttpVersion httpVersion;

    private String userAgent;

    private boolean keepalive;

    private boolean gzip;

    private URL url;

    private String uri;

    private QueryParameters queryParameters;

    private ByteBuf content;

    private long timeoutInMillis;

    private boolean followRedirect;

    private int maxRedirects;

    private boolean enableBackOff;

    private BackOff backOff;

    RequestBuilder(ByteBufAllocator allocator) {
        this.allocator = allocator;
        httpMethod = DEFAULT_METHOD;
        httpVersion = DEFAULT_HTTP_VERSION;
        userAgent = DEFAULT_USER_AGENT;
        gzip = DEFAULT_GZIP;
        keepalive = DEFAULT_KEEPALIVE;
        url = DEFAULT_URL;
        timeoutInMillis = DEFAULT_TIMEOUT_MILLIS;
        followRedirect = DEFAULT_FOLLOW_REDIRECT;
        maxRedirects = DEFAULT_MAX_REDIRECT;
        headers = new DefaultHttpHeaders();
        removeHeaders = new ArrayList<>();
        cookies = new HashSet<>();
        queryParameters = new QueryParameters();
    }

    public RequestBuilder setMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public RequestBuilder enableHttp1() {
        this.httpVersion = HttpVersion.HTTP_1_1;
        return this;
    }

    public RequestBuilder enableHttp2() {
        this.httpVersion = HTTP_2_0;
        return this;
    }

    public RequestBuilder setVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    public RequestBuilder setVersion(String httpVersion) {
        this.httpVersion = HttpVersion.valueOf(httpVersion);
        return this;
    }

    public RequestBuilder setTimeoutInMillis(long timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
        return this;
    }

    public RequestBuilder remoteAddress(HttpAddress httpAddress) {
        this.url = URL.builder()
                .scheme(httpAddress.isSecure() ? "https" : "http")
                .host(httpAddress.getInetSocketAddress().getHostString())
                .port(httpAddress.getInetSocketAddress().getPort())
                .build();
        this.httpVersion = httpAddress.getVersion();
        return this;
    }

    public RequestBuilder url(String url) {
        return url(URL.from(url));
    }

    public RequestBuilder url(URL url) {
        this.url = url;
        return this;
    }

    public RequestBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    public RequestBuilder setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public RequestBuilder addHeader(String name, Object value) {
        this.headers.add(name, value);
        return this;
    }

    public RequestBuilder setHeader(String name, Object value) {
        this.headers.set(name, value);
        return this;
    }

    public RequestBuilder removeHeader(String name) {
        removeHeaders.add(name);
        return this;
    }

    public RequestBuilder addParameter(String name, String value) {
        if (queryParameters != null) {
            queryParameters.add(name, value);
        }
        return this;
    }

    public RequestBuilder addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public RequestBuilder contentType(String contentType) {
        addHeader(HttpHeaderNames.CONTENT_TYPE, contentType);
        return this;
    }

    public RequestBuilder acceptGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    public RequestBuilder keepAlive(boolean keepalive) {
        this.keepalive = keepalive;
        return this;
    }

    public RequestBuilder setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    public RequestBuilder setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
        return this;
    }

    public RequestBuilder enableBackOff(boolean enableBackOff) {
        this.enableBackOff = enableBackOff;
        return this;
    }

    public RequestBuilder setBackOff(BackOff backOff) {
        this.backOff = backOff;
        return this;
    }

    public RequestBuilder setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public RequestBuilder text(String text) {
        content(text, HttpHeaderValues.TEXT_PLAIN);
        return this;
    }

    public RequestBuilder json(String json) {
        content(json, HttpHeaderValues.APPLICATION_JSON);
        return this;
    }

    public RequestBuilder xml(String xml) {
        content(xml, "application/xml");
        return this;
    }

    public RequestBuilder content(ByteBuf byteBuf) {
        this.content = byteBuf;
        return this;
    }

    public RequestBuilder content(CharSequence charSequence, String contentType) {
        content(charSequence.toString().getBytes(StandardCharsets.UTF_8), AsciiString.of(contentType));
        return this;
    }

    public RequestBuilder content(byte[] buf, String contentType) {
        content(buf, AsciiString.of(contentType));
        return this;
    }

    public RequestBuilder content(ByteBuf body, String contentType)  {
        content(body, AsciiString.of(contentType));
        return this;
    }

    public RequestBuilder addCredentials(String user,String pass,boolean isProxied) {
    	 StringBuffer buffer = new StringBuffer(32);
         if (isProxied) {
             buffer.append("Proxy-Authorization");
         } else {
             buffer.append("WWW-Authenticate");
         }
         buffer.append(": Basic ");
         
         StringBuilder tmp = new StringBuilder();
         tmp.append(user);
         tmp.append(":");
         tmp.append((pass == null) ? "null" : pass);
         try {
			buffer.append(Base64Utils.encode(tmp.toString().getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
         this.addHeader(HttpHeaderNames.AUTHORIZATION.toString(),buffer.toString());
         return this;
    }

    public Request build() {
        if (url == null) {
            throw new IllegalStateException("URL not set");
        }
        if (url.getHost() == null) {
            throw new IllegalStateException("host in URL not defined: " + url);
        }
        if (uri != null) {
            try {
                url = URL.base(url).resolve(uri);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        // add explicit parameters to URL
        queryParameters.forEach(param -> url.getQueryParams().add(param));
        // let Netty's query string decoder/encoder work over the URL to add paramters given implicitly in url()
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(URI.create(url.toString()), StandardCharsets.UTF_8);
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder(queryStringDecoder.path());
        for (Map.Entry<String, List<String>> entry : queryStringDecoder.parameters().entrySet()) {
            for (String value : entry.getValue()) {
                queryStringEncoder.addParam(entry.getKey(), value);
            }
        }
        // build uri from QueryStringDecoder
        StringBuilder sb = new StringBuilder();
        String pathAndQuery = queryStringEncoder.toString();
        sb.append(pathAndQuery.isEmpty() ? "/" : pathAndQuery);
        String ref = url.getFragment();
        if (ref != null && !ref.isEmpty()) {
            sb.append('#').append(ref);
        }
        String uri = sb.toString();
        DefaultHttpHeaders validatedHeaders = new DefaultHttpHeaders(true);
        validatedHeaders.set(headers);
        String scheme = url.getScheme();
        if (httpVersion.majorVersion() == 2) {
            validatedHeaders.set(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme);
        }
        validatedHeaders.set(HttpHeaderNames.HOST, url.getHostInfo());
        validatedHeaders.set(HttpHeaderNames.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
        if (userAgent != null) {
            validatedHeaders.set(HttpHeaderNames.USER_AGENT, userAgent);
        }
        if (gzip) {
            validatedHeaders.set(HttpHeaderNames.ACCEPT_ENCODING, "gzip");
        }
        int length = content != null ? content.capacity() : 0;
        if (!validatedHeaders.contains(HttpHeaderNames.CONTENT_LENGTH) && !validatedHeaders.contains(HttpHeaderNames.TRANSFER_ENCODING)) {
            if (length < 0) {
                validatedHeaders.set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");
            } else {
                validatedHeaders.set(HttpHeaderNames.CONTENT_LENGTH, Long.toString(length));
            }
        }
        if (!validatedHeaders.contains(HttpHeaderNames.ACCEPT)) {
            validatedHeaders.set(HttpHeaderNames.ACCEPT, "*/*");
        }
        // RFC 2616 Section 14.10
        // "An HTTP/1.1 client that does not support persistent connections MUST include the "close" connection
        // option in every request message."
        if (httpVersion.majorVersion() == 1 && !keepalive) {
            validatedHeaders.set(HttpHeaderNames.CONNECTION, "close");
        }
        // at last, forced removal of unwanted headers
        for (String headerName : removeHeaders) {
            validatedHeaders.remove(headerName);
        }
        return new Request(url, httpVersion, httpMethod, validatedHeaders, cookies, uri, content,
                timeoutInMillis, followRedirect, maxRedirects, 0, enableBackOff, backOff);
    }

    public void addHeader(AsciiString name, Object value) {
        if (!headers.contains(name)) {
            headers.add(name, value);
        }
    }

    private void content(CharSequence charSequence, AsciiString contentType)  {
        content(ByteBufUtil.writeUtf8(allocator, charSequence), contentType);
    }

    private void content(byte[] buf, AsciiString contentType) {
        ByteBuf byteBuf = allocator.buffer();
        content(byteBuf.writeBytes(buf), contentType);
    }

    private void content(ByteBuf body, AsciiString contentType) {
        this.content = body;
        addHeader(HttpHeaderNames.CONTENT_LENGTH, (long) body.readableBytes());
        addHeader(HttpHeaderNames.CONTENT_TYPE, contentType);
    }
}
