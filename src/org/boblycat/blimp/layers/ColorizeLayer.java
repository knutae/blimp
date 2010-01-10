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

import net.sourceforge.jiu.color.adjustment.Colorize;
import net.sourceforge.jiu.util.MathUtil;

import org.boblycat.blimp.data.Bitmap;

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
        Colorize op = new Colorize();
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
        this.hue = MathUtil.clamp(hue, 0, 360);
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
        this.lightness = MathUtil.clamp(lightness, 0, 400);
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
        this.saturationMultiplier = MathUtil.clamp(saturationMultiplier, 0, 400);
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
        this.baseSaturation = MathUtil.clamp(baseSaturation, 0, 400);
    }

    /**
     * @return the baseSaturation
     */
    public int getBaseSaturation() {
        return baseSaturation;
    }

}
