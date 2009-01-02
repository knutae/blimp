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
 * Adjust the color tones of an image along three "axes":
 * cyan to red, magenta to green and yellow to blue.
 * Only works for RGB images.
 *
 * @author Knut Arild Erstad
 */
public class ColorBalanceOperation extends RGBDoubleOperation {
    private double cyanRedFactor;
    private double magentaGreenFactor;
    private double yellowBlueFactor;
    private boolean preserveLightness;

    private static double toFactor(int ivalue) {
        return ivalue / 100.0;
    }

    private static double adjust(double input, double factor) {
        if (factor <= 0.0)
            return input * (1.0 + factor);
        else
            return input + (1.0 - input) * factor;
    }

    /**
     * Set the modifiers for adjusting colors along the three "color axes".
     * Useful values are between -100 and 100, where 0 means no change.
     * 
     * @param cyanRed
     *      a negative value will tilt the tones toward cyan, positive towards red.
     * @param magentaGreen
     *      a negative value will tilt the tones toward magenta, positive towards green.
     * @param yellowBlue
     *      a negative value will tilt the tones toward yellow, positive towards blue.
     * @param preserveLightness
     *      if <code>true</code>, preserve the relative lightness of each sample using the
     *      HSL (hue-saturation-lightness) color space.  If set to <code>false</code>,
     *      negative and positive values will cause the image to become darker or lighter,
     *      respectively.  A <code>true</code> value tends to add some noise to the image. 
     */
    public void setModifiers(int cyanRed, int magentaGreen, int yellowBlue,
            boolean preserveLightness) {
        cyanRedFactor = toFactor(cyanRed);
        magentaGreenFactor = toFactor(magentaGreen);
        yellowBlueFactor = toFactor(yellowBlue);
        this.preserveLightness = preserveLightness;
    }

    @Override
    protected void adjustColor(double r, double g, double b, double[] out) {
        out[0] = adjust(r, cyanRedFactor);
        out[1] = adjust(g, magentaGreenFactor);
        out[2] = adjust(b, yellowBlueFactor);
        if (preserveLightness) {
            // get the original lightness
            double[] tmp = ColorUtil.rgbToHsl(r, g, b, null);
            double lightness = tmp[2];
            // restore it
            ColorUtil.rgbToHsl(out[0], out[1], out[2], tmp);
            ColorUtil.hslToRgb(tmp[0], tmp[1], lightness, out);
        }
    }
}
