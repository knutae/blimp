package org.boblycat.blimp;

public class Util {
    public static int constrainedValue(int value, int min, int max) {
        if (max < min)
            throw new IllegalArgumentException("max (" + max + ") must be larger than min (" + min + ")");
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
}