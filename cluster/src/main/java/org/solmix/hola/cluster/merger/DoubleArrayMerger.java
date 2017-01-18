package org.solmix.hola.cluster.merger;

import org.solmix.hola.cluster.Merger;
import org.solmix.runtime.Extension;
@Extension("double")
public class DoubleArrayMerger implements Merger<double[]> {

    @Override
    public double[] merge(double[]... items) {
        int total = 0;
        for (double[] array : items) {
            total += array.length;
        }
        double[] result = new double[total];
        int index = 0;
        for (double[] array : items) {
            for (double item : array) {
                result[index++] = item;
            }
        }
        return result;
    }
}
