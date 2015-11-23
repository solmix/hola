package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;

public class BooleanArrayMerger implements Merger<boolean[]> {

    public boolean[] merge(boolean[]... items) {
        int totalLen = 0;
        for(boolean[] array : items) {
            totalLen += array.length;
        }
        boolean[] result = new boolean[totalLen];
        int index = 0;
        for(boolean[] array : items) {
            for(boolean item : array) {
                result[index++] = item;
            }
        }
        return result;
    }

}