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
package org.boblycat.blimp.layers;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.jiuops.ColorUtil;
import org.boblycat.blimp.jiuops.RGBDoubleOperation;

class ColorizeOperation extends RGBDoubleOperation {
    double hue;
    double lightnessFactor;
    double saturationFactor;
    double baseSaturation;

    void init(int hue, int lightness, int saturation, int baseSaturation) {
        this.hue = hue;
        this.lightnessFactor = lightness / 100.0;
        this.saturationFactor = saturation / 100.0;
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

/**
 * Colorize an image using a single hue.
 *
 * @author Knut Arild Erstad
 */
public class ColorizeLayer extends AdjustmentLayer {
    private int hue;
    private int lightness;
    private int baseSaturation;
    private int saturationMultiplier;

    public ColorizeLayer() {
        hue = 27; // sepia-like hue
        lightness = 100;
        baseSaturation = 30;
        saturationMultiplier = 30;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        ColorizeOperation op = new ColorizeOperation();
        op.init(hue, lightness, saturationMultiplier, baseSaturation);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Colorize";
    }

    /**
     * @param hue the hue to set
     */
    public void setHue(int hue) {
        this.hue = Util.constrainedValue(hue, 0, 360);
    }

    /**
     * @return the hue
     */
    public int getHue() {
        return hue;
    }

    /**
     * @param lightness the lightness to set
     */
    public void setLightness(int lightness) {
        this.lightness = Util.constrainedValue(lightness, 0, 400);
    }

    /**
     * @return the lightness
     */
    public int getLightness() {
        return lightness;
    }

    /**
     * @param saturation the saturation to set
     */
    public void setSaturationMultiplier(int saturationMultiplier) {
        this.saturationMultiplier = Util.constrainedValue(saturationMultiplier, 0, 400);
    }

    /**
     * @return the saturation
     */
    public int getSaturationMultiplier() {
        return saturationMultiplier;
    }

    /**
     * @param baseSaturation the baseSaturation to set
     */
    public void setBaseSaturation(int baseSaturation) {
        this.baseSaturation = Util.constrainedValue(baseSaturation, 0, 400);
    }

    /**
     * @return the baseSaturation
     */
    public int getBaseSaturation() {
        return baseSaturation;
    }

}
