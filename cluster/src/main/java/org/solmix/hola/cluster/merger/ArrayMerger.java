package org.solmix.hola.cluster.merger;

import java.lang.reflect.Array;

import org.solmix.hola.cluster.Merger;


public class ArrayMerger implements Merger<Object[]> {

    public static final ArrayMerger INSTANCE = new ArrayMerger();

    @Override
    public Object[] merge(Object[]... others) {
        if (others.length == 0) {
            return null;
        }
        int totalLen = 0;
        for (int i = 0; i < others.length; i++) {
            Object item = others[i];
            if (item != null && item.getClass().isArray()) {
                totalLen += Array.getLength(item);
            } else {
                throw new IllegalArgumentException(
                        new StringBuilder(32).append(i + 1)
                                .append("th argument is not an array").toString());
            }
        }

        if (totalLen == 0) {
            return null;
        }

        Class<?> type = others[0].getClass().getComponentType();

        Object result = Array.newInstance(type, totalLen);
        int index = 0;
        for (Object array : others) {
            for (int i = 0; i < Array.getLength(array); i++) {
                Array.set(result, index++, Array.get(array, i));
            }
        }
        return (Object[])result;
    }

}
