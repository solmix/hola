package org.solmix.hola.cluster;


public interface MergerFactory
{
    <T> Merger<T> getMerger(Class<T> returnType) ;
}
