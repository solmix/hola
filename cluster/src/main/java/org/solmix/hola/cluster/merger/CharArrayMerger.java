package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension("char")
public class CharArrayMerger implements Merger<char[]> {

    @Override
    public char[] merge(char[]... items) {
        int total = 0;
        for (char[] array : items) {
            total += array.length;
        }
        char[] result = new char[total];
        int index = 0;
        for (char[] array : items) {
            for (char item : array) {
                result[index++] = item;
            }
        }
        return result;
    }
}