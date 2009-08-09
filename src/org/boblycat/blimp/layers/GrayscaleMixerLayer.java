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

import static org.boblycat.blimp.util.MathUtil.*;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.jiuops.GrayscaleMixerOperation;

import net.sourceforge.jiu.data.PixelImage;

public class GrayscaleMixerLayer extends AdjustmentLayer {
    public static final int FULL_WEIGHT = 100;
    public static final int MINIMUM_WEIGHT = -2 * FULL_WEIGHT;
    public static final int MAXIMUM_WEIGHT = 2 * FULL_WEIGHT;
    public static final int DEFAULT_RED = 40;
    public static final int DEFAULT_GREEN = 30;
    public static final int DEFAULT_BLUE = 30;

    private int blue;
    private int green;
    private int red;

    public GrayscaleMixerLayer() {
        red = DEFAULT_RED;
        green = DEFAULT_GREEN;
        blue = DEFAULT_BLUE;
    }

    static int constrain(int value) {
        return clamp(value, MINIMUM_WEIGHT, MAXIMUM_WEIGHT);
    }

    public void setRed(int red) {
        this.red = constrain(red);
    }

    public int getRed() {
        return red;
    }

    public void setGreen(int green) {
        this.green = constrain(green);
    }

    public int getGreen() {
        return green;
    }

    public void setBlue(int blue) {
        this.blue = constrain(blue);
    }

    public int getBlue() {
        return blue;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        GrayscaleMixerOperation mixerOp = new GrayscaleMixerOperation();
        mixerOp.setWeights(red, green, blue);
        PixelImage image = source.getImage();
        image = applyJiuOperation(image, mixerOp);
        return new Bitmap(image);
    }

    @Override
    public String getDescription() {
        return "Grayscale Mixer";
    }

}
