package org.solmix.hola.http.client.transport;

import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

class Flow {

    private final AtomicInteger counter;

    private final SortedMap<Integer, CompletableFuture<Boolean>> map;

    Flow() {
        this.counter = new AtomicInteger(3);
        this.map = new ConcurrentSkipListMap<>();
    }

    CompletableFuture<Boolean> get(Integer key) {
        return map.get(key);
    }

    Set<Integer> keys() {
        return map.keySet();
    }

    Integer firstKey() {
        return map.firstKey();
    }

    Integer lastKey() {
        return map.lastKey();
    }

    void put(Integer key, CompletableFuture<Boolean> promise) {
        map.put(key, promise);
    }

    void remove(Integer key) {
        if (key != null) {
            map.remove(key);
        }
    }

    Integer nextStreamId() {
        Integer streamId = counter.getAndAdd(2);
        if (streamId == Integer.MIN_VALUE) {
            // reset if overflow, Java wraps atomic integers to Integer.MIN_VALUE
            // should we send a GOAWAY?
            counter.set(3);
            streamId = 3;
        }
        map.put(streamId, new CompletableFuture<>());
        return streamId;
    }

    void fail(Throwable throwable) {
        for (CompletableFuture<Boolean> promise : map.values()) {
            promise.completeExceptionally(throwable);
        }
    }

    public void close() {
        map.clear();
    }

    @Override
    public String toString() {
        return "[next=" + counter + ", " + map + "]";
    }
}
