package org.solmix.hola.http.client.listener;

import io.netty.handler.codec.http.FullHttpResponse;

@FunctionalInterface
public interface ResponseListener {

    void onResponse(FullHttpResponse fullHttpResponse);
}
