package org.solmix.hola.http.client.listener;

@FunctionalInterface
public interface ExceptionListener {

    void onException(Throwable throwable);
}
