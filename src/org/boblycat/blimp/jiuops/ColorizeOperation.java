/*
 * Copyright (C) 2009 Knut Arild Erstad
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
 * Colorizes an image using a single hue (color tone).
 * The result can be referred to as a monotone image.
 * 
 * This operation uses an HSL color space, see the
 * <a href="http://en.wikipedia.org/wiki/HSL_color_space">Wikipedia article</a>.
 * 
 * @author Knut Arild Erstad
 */
public class ColorizeOperation extends RGBDoubleOperation {
    private double hue;
    private double lightnessFactor;
    private double saturationFactor;
    private double baseSaturation;

    /**
     * Initialize the parameters for the colorize operation.
     * 
     * @param hue
     *      the hue of the output image, given as a color wheel angle in degrees.
     * @param lightness
     *      a lightness (brightness) modifier, where 0 makes the image black and
     *      100 preserves the existing lightness.
     * @param relativeSaturation
     *      how much saturation to add to each pixel of the output image,
     *      relative to the saturation of the corresponding pixel in the
     *      input image.
     * @param baseSaturation
     *      how much overall saturation to add to the output image
     */
    public void init(int hue, int lightness, int relativeSaturation, int baseSaturation) {
        this.hue = hue;
        this.lightnessFactor = lightness / 100.0;
        this.saturationFactor = relativeSaturation / 100.0;
        this.baseSaturation = baseSaturation / 100.0;
    }

    @Override
    protected void adjustColor(double r, double g, double b, double[] out) {
        ColorUtil.rgbToHsl(r, g, b, out);
        out[0] = hue;
        out[1] = baseSaturation + out[1] * saturationFactor;
        out[2] *= lightnessFactor;
        ColorUtil.hslToRgb(out[0], out[1], out[2], out);
    }
}
