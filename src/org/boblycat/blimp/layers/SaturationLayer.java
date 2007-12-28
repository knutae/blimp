/*
 * Copyright (C) 2007 Knut Arild Erstad
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
package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ColorUtil;
import org.boblycat.blimp.RGBDoubleOperation;
import org.boblycat.blimp.Util;

class SaturationOperation extends RGBDoubleOperation {
    double hueOffset;
    double saturationFactor;
    double lightnessFactor;

    void init(int hue, int saturation, int lightness) {
        hueOffset = hue;
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
        else if (out[0] < 0)
            out[0] += 360;
        out[1] = adjust(out[1], saturationFactor);
        out[2] = adjust(out[2], lightnessFactor);
        ColorUtil.hslToRgb(out[0], out[1], out[2], out);
    }
}

/**
 * Layer which adjusts the Hue, Saturation and Lightness (luminance) of an image.
 *
 * Each pixel's color is adjusted individually by converting the color from
 * RGB to HSL, adjusting it, and converting it back to RGB.
 *
 * The hue adjustment is given in degrees according to a color circle going from
 * red (0) to yellow (60), green (120), cyan (180), blue (240), magenta (300) and
 * back to red.
 *
 * The saturation and lightness adjustments are given as percentage multipliers.
 *
 * @author Knut Arild Erstad
 */
public class SaturationLayer extends AdjustmentLayer {
    private int hue;
    private int saturation;
    private int lightness;

    public SaturationLayer() {
        hue = 0;
        saturation = 100;
        lightness = 100;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        SaturationOperation op = new SaturationOperation();
        op.init(hue, saturation, lightness);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Hue/Saturation/Lightness";
    }

    public void setSaturation(int saturation) {
        this.saturation = Util.constrainedValue(saturation, 0, 400);
    }

    public int getSaturation() {
        return saturation;
    }

    public void setLightness(int value) {
        this.lightness = Util.constrainedValue(value, 0, 400);
    }

    public int getLightness() {
        return lightness;
    }

    public void setHue(int hue) {
        this.hue = Util.constrainedValue(hue, -180, 180);
    }

    public int getHue() {
        return hue;
    }

}
