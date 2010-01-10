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

import net.sourceforge.jiu.color.adjustment.HueSaturationLightness;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.util.MathUtil;

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
        HueSaturationLightness op = new HueSaturationLightness();
        op.init(hue, saturation, lightness);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Hue/Saturation/Lightness";
    }

    public void setSaturation(int saturation) {
        this.saturation = MathUtil.clamp(saturation, 0, 400);
    }

    public int getSaturation() {
        return saturation;
    }

    public void setLightness(int value) {
        this.lightness = MathUtil.clamp(value, 0, 400);
    }

    public int getLightness() {
        return lightness;
    }

    public void setHue(int hue) {
        this.hue = MathUtil.clamp(hue, -180, 180);
    }

    public int getHue() {
        return hue;
    }

}
