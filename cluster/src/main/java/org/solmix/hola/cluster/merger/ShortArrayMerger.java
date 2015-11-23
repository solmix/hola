package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;

public class ShortArrayMerger implements Merger<short[]> {

    public short[] merge(short[]... items) {
        int total = 0;
        for (short[] array : items) {
            total += array.length;
        }
        short[] result = new short[total];
        int index = 0;
        for (short[] array : items) {
            for (short item : array) {
                result[index++] = item;
            }
        }
        return result;
    }
}
