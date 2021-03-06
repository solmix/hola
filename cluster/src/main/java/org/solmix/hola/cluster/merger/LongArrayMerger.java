package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension("long")
public class LongArrayMerger implements Merger<long[]> {

    @Override
    public long[] merge(long[]... items) {
        int total = 0;
        for (long[] array : items) {
            total += array.length;
        }
        long[] result = new long[total];
        int index = 0;
        for (long[] array : items) {
            for (long item : array) {
                result[index++] = item;
            }
        }
        return result;
    }
}
