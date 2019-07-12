package org.solmix.hola.http.client;

import java.io.Closeable;

public interface Pool<T> extends Closeable {

    void prepare(int count) throws Exception;

    T acquire() throws Exception;

    void release(T t, boolean close) throws Exception;
}
