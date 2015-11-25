package org.solmix.hola.cluster;

import org.solmix.runtime.Extension;

@Extension
public interface MergerFactory
{
    <T> Merger<T> getMerger(Class<T> returnType) ;
}
