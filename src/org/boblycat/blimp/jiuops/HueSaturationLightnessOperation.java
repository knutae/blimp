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
 * Adjust the Hue, Saturation and Lightness (Luminance) of an image.
 *
 * Each pixel's color is adjusted individually by converting the color from
 * RGB to HSL, adjusting it, and converting it back to RGB.
 *
 * The hue adjustment is given as an offset in degrees according to a color circle
 * going from red (0) to yellow (60), green (120), cyan (180), blue (240),
 * magenta (300) and back to red.
 *
 * The saturation and lightness adjustments are given as percentage multipliers.
 *
 * @author Knut Arild Erstad
 */
public class HueSaturationLightnessOperation extends RGBDoubleOperation {
    private double hueOffset;
    private double saturationFactor;
    private double lightnessFactor;

    /**
     * Initialize the parameters for the HSL operation.
     * @param hue
     *      hue offset in degrees
     * @param saturation
     *      saturation multiplier in percentage (100 equals no change)
     * @param lightness
     *      lightness multiplier in percentage (100 equals no change)
     */
    public void init(double hue, int saturation, int lightness) {
        hueOffset = MathUtil.mod(hue, 360.0);
        saturationFactor = saturation / 100.0;
        lightnessFactor = lightness / 100.0;
    }

    private static double adjust(double input, double factor) {
        return input * factor;
    }

    protected void adjustColor(double r, double g, double b, double[] out) {
        ColorUtil.rgbToHsl(r, g, b, out);
        out[0] += hueOffset;
        if (out[0] >= 360)
            out[0] -= 360;
        out[1] = adjust(out[1], saturationFactor);
        out[2] = adjust(out[2], lightnessFactor);
        ColorUtil.hslToRgb(out[0], out[1], out[2], out);
    }
}
