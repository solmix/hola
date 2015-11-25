package org.solmix.hola.cluster.merger;

import java.util.HashSet;
import java.util.Set;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension(name="map")
public class SetMerger implements Merger<Set<?>> {

    @Override
    public Set<Object> merge(Set<?>... items) {

        Set<Object> result = new HashSet<Object>();

        for (Set<?> item : items) {
            if (item != null) {
                result.addAll(item);
            }
        }

        return result;
    }
}
