package org.solmix.hola.cluster.merger;

import java.util.ArrayList;
import java.util.List;

import org.solmix.hola.cluster.Merger;

public class ListMerger implements Merger<List<?>> {

    @Override
    public List<Object> merge(List<?>... items) {
        List<Object> result = new ArrayList<Object>();
        for (List<?> item : items) {
            if (item != null) {
                result.addAll(item);
            }
        }
        return result;
    }

}
