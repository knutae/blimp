/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp.jiuops;

/**
 * Miscellaneous static numeric utilities functions.
 * @author Knut Arild Erstad
 */
public class MathUtil {
    /**
     * Clamp (constrain) an integer value to the given min-max range.
     * @param value the value
     * @param min the minimum output value
     * @param max the maximum output value
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        if (max < min)
            throw new IllegalArgumentException("max (" + max
                    + ") must be larger than min (" + min + ")");
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    /**
     * Clamp (constrain) a double value to the given min-max range.
     * @param value the value
     * @param min the minimum output value
     * @param max the maximum output value
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        if (max < min)
            throw new IllegalArgumentException("max (" + max
                    + ") must be larger than min (" + min + ")");
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    /**
     * Returns the integer value as a byte, clamped to the byte range 0-255.
     * @param signedIntValue a signed integer value
     * @return a value which should be regarded as an unsigned byte
     */
    public static byte clampToUnsignedByte(int signedIntValue) {
        if (signedIntValue < 0)
            signedIntValue = 0;
        else if (signedIntValue > 255)
            signedIntValue = 255;
        return (byte) (0xff & signedIntValue);
    }

    /**
     * Returns the integer value as a short, clamped to the short range 0-65535.
     * @param signedIntValue a signed integer value
     * @return a value which should be regarded as an unsigned short
     */
    public static short clampToUnsignedShort(int signedIntValue) {
        final int UNSIGNED_SHORT_MAX = 65535;
        if (signedIntValue < 0)
            signedIntValue = 0;
        else if (signedIntValue > UNSIGNED_SHORT_MAX)
            signedIntValue = UNSIGNED_SHORT_MAX;
        return (short) (0xffff & signedIntValue);
    }
}
