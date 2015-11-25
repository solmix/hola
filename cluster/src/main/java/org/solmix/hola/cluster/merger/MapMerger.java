package org.solmix.hola.cluster.merger;

import java.util.HashMap;
import java.util.Map;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension(name="map")
public class MapMerger implements Merger<Map<?, ?>> {

    @Override
    public Map<?, ?> merge(Map<?, ?>... items) {
        if (items.length == 0) {
            return null;
        }
        Map<Object, Object> result = new HashMap<Object, Object>();
        for (Map<?, ?> item : items) {
            if (item != null) {
                result.putAll(item);
            }
        }
        return result;
    }

}
