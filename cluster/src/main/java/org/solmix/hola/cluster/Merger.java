package org.solmix.hola.cluster;


public interface Merger<T>
{
    T merge(T... items);
}
