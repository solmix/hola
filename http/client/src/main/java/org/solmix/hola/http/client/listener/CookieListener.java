package org.solmix.hola.http.client.listener;

import io.netty.handler.codec.http.cookie.Cookie;

@FunctionalInterface
public interface CookieListener {

    void onCookie(Cookie cookie);
}
