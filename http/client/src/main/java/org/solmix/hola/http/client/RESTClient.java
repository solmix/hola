package org.solmix.hola.http.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.solmix.exchange.URL;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

public class RESTClient {

    private static final Client client = new Client();

    private FullHttpResponse response;

    private RESTClient() {
    }

    public void setResponse(FullHttpResponse response) {
        this.response = response.copy();
    }

    public FullHttpResponse getResponse() {
        return response;
    }

    public String asString() {
        return asString(StandardCharsets.UTF_8);
    }

    public String asString(Charset charset) {
        ByteBuf byteBuf = response != null ? response.content() : null;
        try {
            return byteBuf != null && byteBuf.isReadable() ? response.content().toString(charset) : null;
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }

    public void close() throws IOException {
        client.shutdownGracefully();
    }

    public static RESTClient get(String urlString) throws IOException {
        return method(urlString, null, null, HttpMethod.GET);
    }

    public static RESTClient delete(String urlString) throws IOException {
        return method(urlString, null, null, HttpMethod.DELETE);
    }

    public static RESTClient post(String urlString, String body) throws IOException {
        return method(urlString, body, StandardCharsets.UTF_8, HttpMethod.POST);
    }

    public static RESTClient post(String urlString, ByteBuf content) throws IOException {
        return method(urlString, content, HttpMethod.POST);
    }

    public static RESTClient put(String urlString, String body) throws IOException {
        return method(urlString, body, StandardCharsets.UTF_8, HttpMethod.PUT);
    }

    public static RESTClient put(String urlString, ByteBuf content) throws IOException {
        return method(urlString, content, HttpMethod.PUT);
    }

    public static RESTClient method(String urlString,
                                    String body, Charset charset,
                                    HttpMethod httpMethod) throws IOException {
        ByteBuf byteBuf = null;
        if (body != null && charset != null) {
            byteBuf = client.getByteBufAllocator().buffer();
            byteBuf.writeCharSequence(body, charset);
        }
        return method(urlString, byteBuf, httpMethod);
    }

    public static RESTClient method(String urlString,
                                    ByteBuf byteBuf,
                                    HttpMethod httpMethod) throws IOException {
        URL url = URL.create(urlString);
        RESTClient rESTClient = new RESTClient();
        RequestBuilder requestBuilder = Request.builder(httpMethod).url(url);
        if (byteBuf != null) {
            requestBuilder.content(byteBuf);
        }
        client.newTransport(HttpAddress.http1(url))
                .execute(requestBuilder.build().setResponseListener(rESTClient::setResponse)).get();
        return rESTClient;
    }
}
