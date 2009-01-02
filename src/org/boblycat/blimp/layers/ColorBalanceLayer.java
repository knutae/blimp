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
import org.boblycat.blimp.ColorUtil;
import org.boblycat.blimp.RGBDoubleOperation;
import org.boblycat.blimp.Util;

class ColorMixerOperation extends RGBDoubleOperation {
    double cyanRedFactor;
    double magentaGreenFactor;
    double yellowBlueFactor;
    boolean preserveLightness;

    private static double toFactor(int ivalue) {
        return ivalue / 100.0;
    }

    private static double adjust(double input, double factor) {
        if (factor <= 0.0)
            return input * (1.0 + factor);
        else
            return input + (1.0 - input) * factor;
    }

    void init(int cyanRed, int magentaGreen, int yellowBlue,
            boolean preserveLightness) {
        cyanRedFactor = toFactor(cyanRed);
        magentaGreenFactor = toFactor(magentaGreen);
        yellowBlueFactor = toFactor(yellowBlue);
        this.preserveLightness = preserveLightness;
    }

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

/**
 * A layer for modifying specific color tones (hues).
 *
 * @author Knut Arild Erstad
 */
public class ColorBalanceLayer extends AdjustmentLayer {
    public static final int MIN_VALUE = -100;
    public static final int MAX_VALUE = 100;

    private static int constrain(int value) {
        return Util.constrainedValue(value, MIN_VALUE, MAX_VALUE);
    }

    private int cyanRed;
    private int magentaGreen;
    private int yellowBlue;
    private boolean preserveLightness;

    /**
     * Constructs a color balance layer with preserveLightness enabled.
     */
    public ColorBalanceLayer() {
        cyanRed = 0;
        magentaGreen = 0;
        yellowBlue = 0;
        preserveLightness = true;
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.AdjustmentLayer#applyLayer(org.boblycat.blimp.Bitmap)
     */
    @Override
    public Bitmap applyLayer(Bitmap source) {
        ColorMixerOperation op = new ColorMixerOperation();
        op.init(cyanRed, magentaGreen, yellowBlue, preserveLightness);
        //op.cyanRed = cyanRed;
        //op.magentaGreen = magentaGreen;
        //op.yellowBlue = yellowBlue;
        //op.preserveLightness = preserveLightness;
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    /* (non-Javadoc)
     * @see org.boblycat.blimp.layers.Layer#getDescription()
     */
    @Override
    public String getDescription() {
        return "Color Balance";
    }

    /**
     * @param cyanRed the cyan-red modifier to set.
     */
    public void setCyanRed(int cyanRed) {
        this.cyanRed = constrain(cyanRed);
    }

    /**
     * @return the cyan-red modifier.
     */
    public int getCyanRed() {
        return cyanRed;
    }

    /**
     * @param magentaGreen the magenta-green modifier to set
     */
    public void setMagentaGreen(int magentaGreen) {
        this.magentaGreen = constrain(magentaGreen);
    }

    /**
     * @return the magenta-green modifier
     */
    public int getMagentaGreen() {
        return magentaGreen;
    }

    /**
     * @param yellowBlue the yellow-blue modifier to set
     */
    public void setYellowBlue(int yellowBlue) {
        this.yellowBlue = constrain(yellowBlue);
    }

    /**
     * @return the yellow-blue modifier
     */
    public int getYellowBlue() {
        return yellowBlue;
    }

    /**
     * @param preserveLuminosity the preserveLuminosity to set
     */
    public void setPreserveLightness(boolean preserveLuminosity) {
        this.preserveLightness = preserveLuminosity;
    }

    /**
     * @return the preserveLuminosity
     */
    public boolean getPreserveLightness() {
        return preserveLightness;
    }

}
