package org.solmix.hola.cluster;

import org.solmix.runtime.Extension;

@Extension
public interface Merger<T>
{
    T merge(T... items);
}
