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
import org.boblycat.blimp.BitmapSize;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.jiuops.LazyCrop;

/**
 * Crop layer.
 *
 * All values (left, right, top, bottom) are positive integers given in
 * pixels of the original image data, even if an earlier layer has resized
 * the image.
 *
 * @author Knut Arild Erstad
 */
public class CropLayer extends DimensionAdjustmentLayer {
    int left, right, top, bottom;

    @Override
    public Bitmap applyLayer(Bitmap source) {
        double factor = source.getPixelScaleFactor();
        int cleft = (int) (left / factor);
        int cright = (int) (right / factor);
        int ctop = (int) (top / factor);
        int cbottom = (int) (bottom / factor);
        int x1 = cleft;
        int x2 = source.getWidth() - cright - 1;
        int y1 = ctop;
        int y2 = source.getHeight() - cbottom - 1;
        LazyCrop crop = new LazyCrop();
        try {
            crop.setBounds(x1, y1, x2, y2);
        }
        catch (IllegalArgumentException e) {
            Util.err(e.getMessage());
            return new Bitmap(source.getImage());
        }
        return new Bitmap(applyJiuOperation(source.getImage(), crop));
    }

    @Override
    public String getDescription() {
        return "Crop";
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeft() {
        return left;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getTop() {
        return top;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    @Override
    public BitmapSize calculateSize(BitmapSize inputSize) {
        double factor = inputSize.pixelScaleFactor;
        int cleft = (int) (left / factor);
        int cright = (int) (right / factor);
        int ctop = (int) (top / factor);
        int cbottom = (int) (bottom / factor);
        return new BitmapSize(
                Math.max(0, inputSize.width - cleft - cright),
                Math.max(0, inputSize.height - ctop - cbottom),
                factor);
    }
}
