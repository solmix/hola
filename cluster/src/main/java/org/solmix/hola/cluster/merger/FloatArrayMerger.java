package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension("float")
public class FloatArrayMerger implements Merger<float[]> {

    @Override
    public float[] merge(float[]... items) {
        int total = 0;
        for (float[] array : items) {
            total += array.length;
        }
        float[] result = new float[total];
        int index = 0;
        for (float[] array : items) {
            for (float item : array) {
                result[index++] = item;
            }
        }
        return result;
    }
}