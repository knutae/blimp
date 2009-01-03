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

import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;

/**
 * An abstract operation for modifying RGB pixels individually as doubles.
 * Only works for RGB images ({@link RGB24Image} and {@link RGB48Image}). 
 *
 * @author Knut Arild Erstad
 */
public abstract class RGBDoubleOperation extends RGBOperation {
    double[] rgb;
    
    public RGBDoubleOperation() {
        rgb = new double[3];
    }
    
    /**
     * Modify each pixel and put the result in the <i>out</i> array.
     *
     * Each input value is in the range [0, 1].  Output values that are
     * not in the [0, 1] range will be clamped automatically.
     *
     * @param r
     *            the red value
     * @param g
     *            the green value
     * @param b
     *            the blue value
     * @param out
     *            an uninitialized array of length 3.
     */
    protected abstract void adjustColor(double r, double g, double b,
            double[] out);

    private static final double MAX_8BIT = 255.0;
    private static final double MAX_16BIT = 65535.0;

    @Override
    protected void adjust24BitColor(int r, int g, int b, RGB out) {
        adjustColor(r / MAX_8BIT, g / MAX_8BIT, b / MAX_8BIT, rgb);
        out.r = (int) (rgb[0] * MAX_8BIT);
        out.g = (int) (rgb[1] * MAX_8BIT);
        out.b = (int) (rgb[2] * MAX_8BIT);
    }

    @Override
    protected void adjust48BitColor(int r, int g, int b, RGB out) {
        adjustColor(r / MAX_16BIT, g / MAX_16BIT, b / MAX_16BIT, rgb);
        out.r = (int) (rgb[0] * MAX_16BIT);
        out.g = (int) (rgb[1] * MAX_16BIT);
        out.b = (int) (rgb[2] * MAX_16BIT);
    }
}
