package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension("byte")
public class ByteArrayMerger implements Merger<byte[]>{

    @Override
    public byte[] merge(byte[]... items) {
        int total = 0;
        for (byte[] array : items) {
            total += array.length;
        }
        byte[] result = new byte[total];
        int index = 0;
        for (byte[] array : items) {
            for (byte item : array) {
                result[index++] = item;
            }
        }
        return result;
    }

}
