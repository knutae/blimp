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

import net.sourceforge.jiu.data.GrayImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;

/**
 * Converts an RBG image to grayscale by supplying red, green and blue weights.
 * 
 * Note that the output image will be of the same type as the input
 * (which must be {@link RGB24Image} or {@link RGB48Image}), and not a
 * {@link GrayImage}.
 * 
 * @author Knut Arild Erstad
 */
public class GrayscaleMixerOperation extends RGBOperation {
    private int redWeight, greenWeight, blueWeight;

    /**
     * Set the red, green and blue weights.
     * A "full" weight is 100, and it is often a good idea to make the
     * sum of the weights close to 100.  Negative weights are allowed.
     * 
     * @param red the red weight
     * @param green the green weight
     * @param blue the blue weight
     */
    public void setWeights(int red, int green, int blue) {
        redWeight = red;
        greenWeight = green;
        blueWeight = blue;
    }
    
    private void adjust(int r, int g, int b, RGB out) {
        // this simple formula works both for 24- and 48-bit color depths
        int intensity = (redWeight * r + greenWeight * g + blueWeight * b) / 100;
        out.r = out.g = out.b = intensity;
    }

    @Override
    protected void adjust24BitColor(int r, int g, int b, RGB out) {
        adjust(r, g, b, out);
    }

    @Override
    protected void adjust48BitColor(int r, int g, int b, RGB out) {
        adjust(r, g, b, out);
    }
}
