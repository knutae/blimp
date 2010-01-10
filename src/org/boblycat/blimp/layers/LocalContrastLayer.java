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

import net.sourceforge.jiu.filters.LocalContrast;
import net.sourceforge.jiu.util.MathUtil;

import org.boblycat.blimp.data.Bitmap;

/**
 * A layer for adding contrast in local areas within an image.
 *
 * The local contrast enchancement is implemented like an unsharp mask filter
 * with a big radius, but optimized by taking the average intensity of square
 * areas around each pixel instead of using a convolution matrix.
 *
 * The algorithm is adaptive, which means that is is able to add less contrast
 * for areas that already have a high local contrast.
 *
 * @author Knut Arild Erstad
 */
public class LocalContrastLayer extends AdjustmentLayer {
    public static final int MIN_AMOUNT = 1;
    public static final int MIN_RADIUS = 1;
    public static final int MIN_ADAPTIVE = 0;
    public static final int MAX_AMOUNT = 1000;
    public static final int MAX_RADIUS = 1000;
    public static final int MAX_ADAPTIVE = LocalContrast.MAX_ADAPTIVE;
    private int radius = 100;
    private int amount = 100;
    private int adaptive = 70;

    @Override
    public Bitmap applyLayer(Bitmap source) {
        LocalContrast op = new LocalContrast();
        op.setModifiers((int) (radius / source.getPixelScaleFactor()), amount, adaptive);
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Local Contrast Enhancement";
    }

    public void setRadius(int radius) {
        this.radius = MathUtil.clamp(radius, MIN_RADIUS, MAX_RADIUS);
    }

    public int getRadius() {
        return radius;
    }

    public void setAmount(int level) {
        this.amount = MathUtil.clamp(level, MIN_AMOUNT, MAX_AMOUNT);
    }

    public int getAmount() {
        return amount;
    }

    public void setAdaptive(int adaptive) {
        this.adaptive = MathUtil.clamp(adaptive, MIN_ADAPTIVE, MAX_ADAPTIVE);
    }

    public int getAdaptive() {
        return adaptive;
    }

}
