
package org.solmix.hola.cluster.merger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.solmix.hola.cluster.Merger;
import org.solmix.hola.cluster.MergerFactory;
import org.solmix.runtime.Container;

public class DefaultMergerFactory implements MergerFactory
{

    private final ConcurrentMap<Class<?>, Merger<?>> mergerCache = new ConcurrentHashMap<Class<?>, Merger<?>>();
    private Container container;

    public DefaultMergerFactory(Container container){
        this.container=container;
    }
    @Override
    public <T> Merger<T> getMerger(Class<T> returnType) {
        Merger result;
        if (returnType.isArray()) {
            Class type = returnType.getComponentType();
            result = mergerCache.get(type);
            if (result == null) {
                loadMergers();
                result = mergerCache.get(type);
            }
            if(result == null && ! type.isPrimitive()) {
                result = ArrayMerger.INSTANCE;
            }
        } else {
            result = mergerCache.get(returnType);
            if (result == null) {
                loadMergers();
                result = mergerCache.get(returnType);
            }
        }
        return result;
    }

    private void loadMergers() {
        
    }

}
